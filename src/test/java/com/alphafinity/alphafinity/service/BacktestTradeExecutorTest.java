package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.Account;
import com.alphafinity.alphafinity.model.Analytics;
import com.alphafinity.alphafinity.model.Context;
import com.alphafinity.alphafinity.model.Transaction;
import com.alphafinity.alphafinity.model.enumerations.Quantity;
import com.alphafinity.alphafinity.model.enumerations.TransactionOperation;
import com.alphafinity.alphafinity.model.enumerations.TransactionType;
import com.alphafinity.alphafinity.strategy.BuyAndHold;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BacktestTradeExecutorTest {

    public BacktestTradeExecutor backtestTradeExecutor;
    public ObjectMapper mapper;
    BuyAndHold buyAndHold;

    @BeforeAll
    public void setUp() {
        backtestTradeExecutor = new BacktestTradeExecutor();
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        buyAndHold = new BuyAndHold(new BacktestTradeExecutor());
    }

    @Test
    public void testBuy(){
        Account account = new Account.Builder()
                .initialCapital(100.00)
                .currentCapital(100.00)
                .build();

        Analytics analytics = new Analytics.Builder()
                .build();

        Context context = new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();

        Transaction order = new Transaction.Builder()
                .type(TransactionType.LONG_OPEN)
                .status(TransactionOperation.OPEN)
                .time(LocalDate.of(2020, 1, 8).atStartOfDay())
                .price(10.00)
                .quantity(Quantity.MAX)
                .build();

        Context response = backtestTradeExecutor.buy(context, order);

        Assertions.assertEquals(1, response.getActiveTransactions().size());
        Assertions.assertEquals(0, response.getClosedTransactions().size());
        Assertions.assertEquals(10, response.getActiveTransactions().getFirst().quantity);
        Assertions.assertEquals(0.00, response.getActiveTransactions().getFirst().profit);
        Assertions.assertEquals(TransactionType.LONG_OPEN, response.getActiveTransactions().getFirst().type);
        Assertions.assertEquals(Quantity.NOT_SET, response.getActiveTransactions().getFirst().quantityEnum);
        Assertions.assertEquals(100, response.getActiveTransactions().getFirst().totalCost);
        Assertions.assertEquals(TransactionOperation.OPEN, response.getActiveTransactions().getFirst().operation);
    }

    @Test
    public void testClose(){
        Transaction transaction = new Transaction.Builder()
                .type(TransactionType.LONG_OPEN)
                .status(TransactionOperation.OPEN)
                .time(LocalDate.of(2020, 1, 8).atStartOfDay())
                .price(10.00)
                .quantity(10)
                .build();

        Account account = new Account.Builder()
                .initialCapital(100.00)
                .currentCapital(0.00)
                .addTrade(transaction)
                .build();

        Analytics analytics = new Analytics.Builder()
                .build();

        Context context = new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();

        Transaction order = new Transaction.Builder(transaction)
                .time(LocalDate.of(2020, 1, 8).atStartOfDay())
                .price(20.00)
                .build();

        Context response = backtestTradeExecutor.close(context, transaction, order);

        Assertions.assertEquals(0, response.getActiveTransactions().size());
        Assertions.assertEquals(1, response.getClosedTransactions().size());
        Assertions.assertEquals(10, response.getClosedTransactions().getFirst().quantity);
        Assertions.assertEquals(100.00, response.getClosedTransactions().getFirst().profit);
        Assertions.assertEquals(TransactionType.LONG_CLOSE, response.getClosedTransactions().getFirst().type);
        Assertions.assertEquals(Quantity.NOT_SET, response.getClosedTransactions().getFirst().quantityEnum);
        Assertions.assertEquals(200, response.getClosedTransactions().getFirst().totalCost);
        Assertions.assertEquals(TransactionOperation.CLOSE, response.getClosedTransactions().getFirst().operation);
    }

    @Test
    public void testBulkClose(){
        Transaction transaction1 = new Transaction.Builder()
                .type(TransactionType.LONG_OPEN)
                .status(TransactionOperation.OPEN)
                .time(LocalDate.of(2020, 1, 8).atStartOfDay())
                .price(10.00)
                .quantity(9)
                .build();

        Transaction transaction2 = new Transaction.Builder()
                .type(TransactionType.LONG_OPEN)
                .status(TransactionOperation.OPEN)
                .time(LocalDate.of(2020, 1, 8).atStartOfDay())
                .price(5.0)
                .quantity(2)
                .build();

        List<Transaction> transactions = List.of(transaction1, transaction2);

        Account account = new Account.Builder()
                .initialCapital(100.00)
                .currentCapital(0.00)
                .addTrades(transactions)
                .build();

        Analytics analytics = new Analytics.Builder()
                .build();

        Context context = new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();

        Transaction order = new Transaction.Builder()
                .time(LocalDate.of(2020, 1, 8).atStartOfDay())
                .price(20.00)
                .quantity(Quantity.MAX)
                .build();

        Context response = backtestTradeExecutor.close(context, transactions, order);

        Assertions.assertEquals(0, response.getActiveTransactions().size());
        Assertions.assertEquals(2, response.getClosedTransactions().size());

        Assertions.assertEquals(9, response.getClosedTransactions().getFirst().quantity);
        Assertions.assertEquals(2, response.getClosedTransactions().getLast().quantity);

        Assertions.assertEquals(90, response.getClosedTransactions().getFirst().profit);
        Assertions.assertEquals(30, response.getClosedTransactions().getLast().profit);

        Assertions.assertEquals(TransactionType.LONG_CLOSE, response.getClosedTransactions().getFirst().type);
        Assertions.assertEquals(TransactionType.LONG_CLOSE, response.getClosedTransactions().getLast().type);

        Assertions.assertEquals(Quantity.NOT_SET, response.getClosedTransactions().getFirst().quantityEnum);
        Assertions.assertEquals(Quantity.NOT_SET, response.getClosedTransactions().getLast().quantityEnum);

        Assertions.assertEquals(180, response.getClosedTransactions().getFirst().totalCost);
        Assertions.assertEquals(40, response.getClosedTransactions().getLast().totalCost);

        Assertions.assertEquals(TransactionOperation.CLOSE, response.getClosedTransactions().getFirst().operation);
        Assertions.assertEquals(TransactionOperation.CLOSE, response.getClosedTransactions().getLast().operation);
    }
}
