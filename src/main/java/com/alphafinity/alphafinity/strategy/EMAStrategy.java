package com.alphafinity.alphafinity.strategy;

import com.alphafinity.alphafinity.model.Context;
import com.alphafinity.alphafinity.model.TimeSeriesEntry;
import com.alphafinity.alphafinity.model.Transaction;
import com.alphafinity.alphafinity.model.enumerations.Quantity;
import com.alphafinity.alphafinity.model.enumerations.TransactionType;
import com.alphafinity.alphafinity.service.BacktestTradeExecutor;
import com.alphafinity.alphafinity.service.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EMAStrategy extends Strategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(EMAStrategy.class);

    private final BacktestTradeExecutor tradeExecutor;
    private final double emaThreshold = 0.0; // Threshold for determining buy/sell signals

    public EMAStrategy(BacktestTradeExecutor tradeExecutor) {
        this.tradeExecutor = tradeExecutor;
    }

    @Override
    public Context execute(Context context, TimeSeriesEntry data) {
        double currentPrice = data.close;
        double ema = data.ema;

        if(ema == 0.00){
            return context;
        }

        if (currentPrice > ema + emaThreshold) {
            if (context.getActiveTransactions().isEmpty()) {
                return buy(context, data);
            }
        } else if (currentPrice < ema - emaThreshold) {
            if (!context.getActiveTransactions().isEmpty()) {
                return close(context, data, context.getActiveTransactions());
            }
        }

        return context;
    }

    private Context buy(Context context, TimeSeriesEntry data) {
        Transaction order = new Transaction.Builder()
                .type(TransactionType.LONG_OPEN)
                .price(data.close)
                .quantity(Quantity.MAX)
                .time(data.datetime.atStartOfDay())
                .build();
        return tradeExecutor.buy(context, order);
    }

    private Context close(Context context, TimeSeriesEntry data, List<Transaction> transactions) {
        Transaction order = new Transaction.Builder()
                .type(TransactionType.LONG_CLOSE)
                .price(data.close)
                .quantity(Quantity.MAX)
                .time(data.datetime.atStartOfDay())
                .build();
        return tradeExecutor.close(context, transactions, order);
    }

    @Override
    public String strategyName() {
        return "EMA Strategy";
    }
}
