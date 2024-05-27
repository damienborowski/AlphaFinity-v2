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

import java.util.ArrayList;
import java.util.List;

@Service
public class RSIStrategy extends Strategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSIStrategy.class);

    private final BacktestTradeExecutor tradeExecutor;
    private final double rsiBuyThreshold = 25;  // RSI value below which to buy
    private final double rsiSellThreshold = 75; // RSI value above which to sell
    private final double takeProfitsThreshold = 2.00; // Open profit percentage threshold to take profits


    public RSIStrategy(BacktestTradeExecutor tradeExecutor) {
        this.tradeExecutor = tradeExecutor;
    }

    @Override
    public Context execute(Context context, TimeSeriesEntry data) {
        double rsi = data.rsi;

        if (rsi == 0.00) {
            return context;
        }

        if (rsi < rsiBuyThreshold) {
            if (context.getActiveTransactions().isEmpty()) {
                return buy(context, data);
            }
        } else if (rsi > rsiSellThreshold) {
            if (!context.getActiveTransactions().isEmpty()) {
                return close(context, data, context.getActiveTransactions());
            }
        }

        Transaction order = new Transaction.Builder()
                .type(TransactionType.LONG_CLOSE)
                .price(data.close)
                .quantity(Quantity.MAX)
                .time(data.datetime)
                .build();

        return tradeExecutor.takeProfits(context, data, takeProfitsThreshold, order);
    }

    private Context buy(Context context, TimeSeriesEntry data) {
        Transaction order = new Transaction.Builder()
                .type(TransactionType.LONG_OPEN)
                .price(data.close)
                .quantity(Quantity.MAX)
                .time(data.datetime)
                .build();
        return tradeExecutor.buy(context, order);
    }

    private Context close(Context context, TimeSeriesEntry data, List<Transaction> transactions) {
        Transaction order = new Transaction.Builder()
                .type(TransactionType.LONG_CLOSE)
                .price(data.close)
                .quantity(Quantity.MAX)
                .time(data.datetime)
                .build();
        return tradeExecutor.close(context, transactions, order);
    }

    @Override
    public String strategyName() {
        return "RSI Strategy";
    }
}
