package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.*;
import com.alphafinity.alphafinity.model.enumerations.Quantity;
import com.alphafinity.alphafinity.model.enumerations.TransactionOperation;
import com.alphafinity.alphafinity.model.enumerations.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.alphafinity.alphafinity.utility.Constants.EXECUTE_ORDER;
import static com.alphafinity.alphafinity.utility.Constants.NOT_ENOUGH_ACCOUNT_BALANCE;

@Service
public class BacktesterTradeExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BacktesterTradeExecutor.class);

    public Context buy(Context context, Transaction rawOrder) {

        // Adds extra calculated fields
        Transaction order = enhancedOpenOrderDetails(context, rawOrder);

        Valid validation = isValidOrder(context, order);

        // If order is not valid we can end early
        if(validation.isNotValid){
            LOGGER.warn(NOT_ENOUGH_ACCOUNT_BALANCE);
            return new Context.Builder()
                    .account(context.account)
                    .analytics(context.analytics)
                    .build();
        }

        Transaction transaction = new Transaction.Builder(order)
                .status(TransactionOperation.OPEN)
                .price(order.price)
                .quantity(order.quantity)
                .type(TransactionType.LONG_OPEN)
                .time(order.time)
                .build();

        Account account = new Account.Builder(context)
                .currentCapital(context.account.currentCapital - order.totalCost)
                .addTrade(transaction)
                .build();

        Analytics analytics = new Analytics.Builder(context)
                .addTransaction(transaction)
                .build();

        LOGGER.info(String.format(EXECUTE_ORDER, TransactionOperation.OPEN, order.type, order.price, order.time));
        return new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();
    }

    public Context sell(Context context, Transaction order) {
        LOGGER.info(String.format(EXECUTE_ORDER, TransactionOperation.OPEN, order.type, order.price, order.time));
        return new Context.Builder()
                .account(context.account)
                .analytics(context.analytics)
                .build();
    }

    public Context close(Context context, Transaction transactionToClose, Transaction rawOrder){
        LOGGER.info(String.format(EXECUTE_ORDER, TransactionOperation.CLOSE, rawOrder.type, rawOrder.price, rawOrder.time));

        // If transaction is already closed, return
        if(TransactionOperation.CLOSE.equals(transactionToClose.operation)){
            LOGGER.warn("Transaction passed in is already closed... It shouldn't of ever happened... Please investigate!");
        }

        // Adds extra calculated fields
        Transaction order = enhancedCloseOrderDetails(context, transactionToClose, rawOrder);

        Account account = new Account.Builder(context)
                .currentCapital(context.account.currentCapital + order.totalCost)
                .removeTrade(transactionToClose) // TODO IMPROVE THIS LOGIC TO REMOVE A TRADE / TRANSACTION AND MAKE SURE TO HAVE SOME TRADE HISTORY IN ANALYTICS
                .build();

        Transaction newTransaction = new Transaction.Builder(order)
                .status(TransactionOperation.CLOSE)
                .type(order.type)
                .price(order.price)
                .quantity(order.quantity)
                .time(order.time)
                .build();

        Analytics analytics = new Analytics.Builder(context)
                .addTransaction(newTransaction)
                .build();

        return new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();
    }

    /**
     * Performs validations on the order to see if trade can ve executed. Here is an exhaustive list of validations:<br>
     * 1. Does the account currently hold enough funds to execute the transaction? <br>
     *
     * @param context: context
     * @param order: transaction order
     * @return Valid: object with boolean and error message of validation is any.
     */
    private Valid isValidOrder(Context context, Transaction order){
        // Cost of trade exceeds current account capital
        if(doesTradeOrderExceedAccountCapital(context.account, order)){
            LOGGER.warn(NOT_ENOUGH_ACCOUNT_BALANCE);
            return new Valid(false);
        }

        return new Valid(true);
    }


    /**
     * Adds calculated fields to the order such as <br>
     * 1. totalCost: Total cost of the order to credit or debit the account. <br>
     * 2. quantity: Updates the quantity field with an integer if not already set by the user. If the user specified a quantity enum, then that will
     *              take priority over the integer quantity provided by the user.<br>
     * 3. profit: For closing a trade, we want to get the profit of the trade. Will default to 0 if transaction type is set to open <br>
     *
     * @param context: context
     * @param order: transaction order
     * @return enhanced transaction
     */
    private Transaction enhancedOpenOrderDetails(Context context, Transaction order){
        Integer quantity = getOrderQuantity(context, order);
        Double totalCost = order.price * quantity;

        return new Transaction.Builder(order)
                .quantity(quantity)
                .quantity(Quantity.NOT_SET)
                .totalCost(totalCost)
                .profit(0.00)
                .build();
    }

    private Transaction enhancedCloseOrderDetails(Context context, Transaction orderToClose, Transaction rawOrder){
        Transaction order = enhancedOpenOrderDetails(context, rawOrder);
        Integer quantity = getOrderQuantity(context, order);
        Double totalCost = order.price * quantity;
        Double profit = order.totalCost - orderToClose.totalCost;

        return new Transaction.Builder(order)
                .quantity(quantity)
                .quantity(Quantity.NOT_SET)
                .totalCost(totalCost)
                .profit(profit)
                .build();
    }



    private Integer getOrderQuantity(Context context, Transaction order){
        if (order.quantityEnum == null) {
            return order.quantity;
        }

        return switch (order.quantityEnum){
            case MAX -> getMaxOrderQuantity(context, order);
            case MIN -> getMinOrderQuantity(context, order);
            default -> order.quantity;
        };
    }

    private Integer getMaxOrderQuantity(Context context, Transaction order){
        return Math.toIntExact(Math.round(Math.floor(context.account.currentCapital / order.price)));
    }

    private Integer getMinOrderQuantity(Context context, Transaction order){
        return context.account.currentCapital > order.price ? 1 : 0;
    }

    //=======================================================================//
    //                              VALIDATORS                               //
    //=======================================================================//

    private Boolean doesTradeOrderExceedAccountCapital(Account account, Transaction order){
        if(order.totalCost == 0){
            return true;
        }

        return order.totalCost > account.currentCapital;
    }
}
