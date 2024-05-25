package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.*;
import com.alphafinity.alphafinity.model.enumerations.TransactionType;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class BacktestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BacktestService.class);

    private final BacktesterTradeExecutor tradeExecutor;
    private final BacktestValidationService validationService;
    private final AnalyticsService analyticsService;

    public BacktestService(BacktesterTradeExecutor tradeExecutor,
                           AnalyticsService analyticsService,
                           BacktestValidationService validationService) {
        this.tradeExecutor = tradeExecutor;
        this.analyticsService = analyticsService;
        this.validationService = validationService;
    }

    public Context executeStrategy(Context context, Strategy strategy, TimeSeriesData benchmarkTimeSeriesData, TimeSeriesData strategyTimeSeriesData){
        LOGGER.info("[Backtest] Starting backtesting of: " + strategy.strategyName());
        validationService.validateTimeframes(benchmarkTimeSeriesData, strategyTimeSeriesData);

        Account account = new Account.Builder()
                .startingCapital(context.account.initialCapital)
                .currentCapital(context.account.initialCapital)
                .build();

        Analytics analytics = new Analytics.Builder(context)
                .startDate(strategyTimeSeriesData.getFirstEntry().datetime)
                .endDate(strategyTimeSeriesData.getLastEntry().datetime)
                .build();

        AtomicReference<Context> aContext = new AtomicReference<>(new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build());

        // Running the backtest on the strategy
        strategyTimeSeriesData.entries
                .forEach(entry -> {
                    aContext.updateAndGet(
                            response -> strategy.execute(response, entry)
                    );

                    aContext.updateAndGet(
                            response -> updateState(response, entry)
                    );
                });

        // Force close any open trades and enhance analytics
        Context finalContext = enhanceAnalytics(closeOutOpenTrades(aContext, strategyTimeSeriesData.getLastEntry()), benchmarkTimeSeriesData);

        LOGGER.info("[Backtest] Completed backtesting of strategy");
        return finalContext;
    }

    /**
     * This method is used to keep track of statistics throughout the life-cycle of the backtest. For each entry in the time-series,
     * we will have statistics about the current state of the account for that given time.
     * @param context: context
     * @param entry: time-series entry
     * @return context
     */
    private Context updateState(Context context, TimeSeriesEntry entry){

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

    private Context enhanceAnalytics(Context context, TimeSeriesData benchmarkTimeSeriesData){

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

    private Context closeOutOpenTrades(AtomicReference<Context> aContext, TimeSeriesEntry data){
        Context context = aContext.get();

        // If there are no open transactions, return
        if(CollectionUtils.isEmpty(context.getActiveTransactions())){
            return context;
        }

        // Individually close out open trades
        context.getActiveTransactions()
                .forEach(transaction -> {
                    Transaction order = new Transaction.Builder()
                            .type(TransactionType.LONG_CLOSE)
                            .price(data.close)
                            .time(data.datetime.atStartOfDay())
                            .quantity(transaction.quantity)
                            .build();

                    aContext.updateAndGet(
                            response -> tradeExecutor.close(response, transaction, order));
                });

        return aContext.get();
    }
}
