package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.*;
import com.alphafinity.alphafinity.model.enumerations.TransactionType;
import com.alphafinity.alphafinity.strategy.indicator.EMA;
import com.alphafinity.alphafinity.strategy.indicator.Indicator;
import com.alphafinity.alphafinity.strategy.indicator.RSI;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class BacktestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BacktestService.class);

    private final BacktestTradeExecutor tradeExecutor;
    private final BacktestValidationService validationService;
    private final AnalyticsService analyticsService;
    private final IndicatorService indicatorService;

    public BacktestService(BacktestTradeExecutor tradeExecutor,
                           AnalyticsService analyticsService,
                           BacktestValidationService validationService,
                           IndicatorService indicatorService) {
        this.tradeExecutor = tradeExecutor;
        this.analyticsService = analyticsService;
        this.validationService = validationService;
        this.indicatorService = indicatorService;
    }

    public Context executeStrategy(Context context, Strategy strategy, TimeSeriesData benchmarkTimeSeriesData, TimeSeriesData rawStrategyTimeSeriesData) {
        LOGGER.info("[Backtest] Starting backtesting of: " + strategy.strategyName());
        validationService.validateTimeframes(benchmarkTimeSeriesData, rawStrategyTimeSeriesData);

        TimeSeriesData strategyTimeSeriesData = indicatorService.populateDataWithIndicators(rawStrategyTimeSeriesData, initializeIndicators());

        Account account = new Account.Builder()
                .initialCapital(context.account.initialCapital)
                .currentCapital(context.account.initialCapital)
                .build();

        Analytics analytics = new Analytics.Builder(context)
                .startDate(strategyTimeSeriesData.getFirstEntry().datetime)
                .endDate(strategyTimeSeriesData.getLastEntry().datetime)
                .build();

        Context initialContext = new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();

        // Running the backtest on the strategy using reduce
        Context updatedContext = strategyTimeSeriesData.entries.stream()
                .reduce(
                        initialContext,
                        (ctx, entry) -> {
                            Context afterStrategy = strategy.execute(ctx, entry);
                            return updateState(afterStrategy, entry);
                        },
                        (ctx1, ctx2) -> ctx2 // combiner is not used but required for the reduce method
                );

        // Force close any open trades and enhance analytics
        Context finalContext = enhanceAnalytics(closeOutOpenTrades(updatedContext, strategyTimeSeriesData.getLastEntry()), benchmarkTimeSeriesData);

        LOGGER.info("[Backtest] Completed backtesting of strategy");
        return finalContext;
    }

    /**
     * This method is used to keep track of statistics throughout the life-cycle of the backtest. For each entry in the time-series,
     * we will have statistics about the current state of the account for that given time.
     *
     * @param context: context
     * @param entry:   time-series entry
     * @return context
     */
    private Context updateState(Context context, TimeSeriesEntry entry) {

        // This is the total value of our open positions using the current asset price
        Double currentOpenTransactionValue = context.getActiveTransactions().stream()
                .mapToDouble(transaction -> transaction.quantity * entry.close)
                .sum();

        // Calculate the current account value
        Double currentAccountValue = currentOpenTransactionValue + context.account.currentCapital;

        // Calculate the current profit
        Double currentProfit = currentAccountValue - context.account.initialCapital;

        // Calculate the current profit percentage
        Double currentProfitPercentage = (currentProfit / context.account.initialCapital) * 100;


        State state = new State.Builder()
                .currentTime(entry.datetime)
                .currentAccountValue(currentAccountValue)
                .currentProfit(currentProfit)
                .currentProfitPercentage(currentProfitPercentage)
                .build();

        return new Context.Builder(context)
                .addState(state)
                .build();
    }

    private List<Indicator<?>> initializeIndicators() {
        Indicator<Double> rsi = new RSI.Builder()
                .period(14)  // Could maybe make this a config value?
                .build();

        Indicator<Double> ema = new EMA.Builder()
                .period(100)
                .build();

        return List.of(
                rsi,
                ema
        );
    }

    private Context enhanceAnalytics(Context context, TimeSeriesData benchmarkTimeSeriesData) {

        Analytics analytics = new Analytics.Builder(context)
                .endingCapital(context.account.currentCapital)
                .totalReturnMultiplier(analyticsService.calculateTotalReturnMultiplier(context))
                .totalReturn(analyticsService.calculateTotalReturn(context))
                .totalReturnAsPercentage(analyticsService.calculateTotalReturnAsPercentage(context))
                .totalTrades(analyticsService.calculateTotalTrades(context))
                .totalOpeningTrades(analyticsService.calculateTotalOpenTrades(context))
                .totalClosingTrades(analyticsService.calculateTotalClosedTrades(context))
                .winRate(analyticsService.calculateWinRate(context))
                .averageReturn(analyticsService.calculateAverageReturnPerTrade(context))
                .averageProfit(analyticsService.calculateAverageProfitPerTrade(context))
                .averageLoss(analyticsService.calculateAverageLossPerTrade(context))
                .maxDrawdown(analyticsService.calculateMaxDrawdown(context))
                .alpha(analyticsService.calculateAlpha(context, benchmarkTimeSeriesData))
                .sharpeRatio(analyticsService.calculateSharpeRatio(context))
                .build();

        return new Context.Builder(context)
                .analytics(analytics)
                .build();
    }

    private Context closeOutOpenTrades(Context currentContext, TimeSeriesEntry data) {
        // If there are no open transactions, return
        if (CollectionUtils.isEmpty(currentContext.getActiveTransactions())) {
            return currentContext;
        }

        // Individually close out open trades by reducing the active transactions into a new context
        Context updatedContext = currentContext.getActiveTransactions().stream()
                .reduce(
                        currentContext,
                        (ctx, transaction) -> {
                            Transaction order = new Transaction.Builder()
                                    .type(TransactionType.LONG_CLOSE)
                                    .price(data.close)
                                    .time(data.datetime.atStartOfDay())
                                    .quantity(transaction.quantity)
                                    .build();

                            Context c = tradeExecutor.close(ctx, transaction, order);
                            return c;
                        },
                        (ctx1, ctx2) -> ctx2 // combiner is not used but required for the reduce method
                );
        return updatedContext;
    }
}
