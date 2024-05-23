package com.alphafinity.alphafinity.service.strategy;

import com.alphafinity.alphafinity.model.*;
import com.alphafinity.alphafinity.model.enumerations.Quantity;
import com.alphafinity.alphafinity.model.enumerations.TransactionType;
import com.alphafinity.alphafinity.service.BacktesterTradeExecutor;
import com.alphafinity.alphafinity.service.Strategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BuyAndHold extends Strategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(BuyAndHold.class);

    private final BacktesterTradeExecutor tradeExecutor;

    public BuyAndHold(BacktesterTradeExecutor tradeExecutor) {
        this.tradeExecutor = tradeExecutor;
    }

    public Context execute(Context context, TimeSeriesEntry data) {
        // Create only a buy order
        Transaction order = new Transaction.Builder()
                .type(TransactionType.LONG_OPEN)
                .price(data.close)
                .quantity(Quantity.MAX)
                .time(data.datetime.atStartOfDay()) //TODO CHANGE TO DATETIME
                .build();

        if(context.getActiveTransactions().isEmpty()){
            return tradeExecutor.buy(context, order);
        }

        LOGGER.info("Skipping for date: "+data.datetime);
        return context;
    }


    public String strategyName() {
        return "Buy and Hold strategy";
    }
}
