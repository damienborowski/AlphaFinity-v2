package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.*;
import com.alphafinity.alphafinity.model.enumerations.TransactionOperation;
import com.alphafinity.alphafinity.strategy.BuyAndHold;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.MockitoAnnotations.openMocks;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BacktestServiceTest {
    @Mock
    private BacktestValidationService validationService;
    @Mock
    private AnalyticsService analyticsService;
    @Mock
    private BacktestTradeExecutor tradeExecutor;
    @Mock
    private IndicatorService indicatorService;

    public BacktestService backtestService;
    public ObjectMapper mapper;
    BuyAndHold buyAndHold;


    @BeforeAll
    public void setUp() {
        openMocks(this);
        backtestService = new BacktestService(tradeExecutor, analyticsService, validationService, indicatorService);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        buyAndHold = new BuyAndHold(new BacktestTradeExecutor());
    }

    @Test
    public void testExecuteBacktest_BuyAndHold_NoIndicators() throws IOException {
        Account account = new Account.Builder()
                .initialCapital(100.00)
                .build();

        Analytics analytics = new Analytics.Builder()
                .build();

        Context context = new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();

        Analytics updatedAnalytics = new Analytics.Builder()
                .addTransaction(new Transaction.Builder().quantity(10).price(20.0).status(TransactionOperation.CLOSE).build())
                .build();

        Account updatedAccount = new Account.Builder(context)
                .currentCapital(110.00)
                .build();

        List<TimeSeriesEntry> backtestEntries = mapper.readValue(new File("src/test/resources/sample-data/sample01.json"), new TypeReference<>() {
        });
        List<TimeSeriesEntry> strategyEntries = mapper.readValue(new File("src/test/resources/sample-data/sample01.json"), new TypeReference<>() {
        });

        TimeSeriesData backtestData = new TimeSeriesData(backtestEntries);
        TimeSeriesData strategyData = new TimeSeriesData(strategyEntries);

        Mockito.doNothing().when(validationService).validateTimeframes(any(TimeSeriesData.class), any(TimeSeriesData.class));
        Mockito.when(indicatorService.populateDataWithIndicators(any(TimeSeriesData.class), anyList()))
                        .thenReturn(strategyData);
        Mockito.when(tradeExecutor.buy(any(Context.class), any(Transaction.class)))
                .thenReturn(new Context.Builder(context).build());
        Mockito.when(tradeExecutor.close(any(Context.class), any(Transaction.class), any(Transaction.class)))
                .thenReturn(new Context.Builder(context).account(updatedAccount).analytics(updatedAnalytics).build());

        Context response = backtestService.executeStrategy(context, buyAndHold, backtestData, strategyData);

        Assertions.assertEquals(100, response.account.initialCapital);
        Assertions.assertEquals(110, response.account.currentCapital);
        Assertions.assertEquals(0, response.account.activeTrades.size());
        Assertions.assertEquals(0, response.getActiveTransactions().size());
        Assertions.assertEquals(1, response.getClosedTransactions().size());
    }

    @Test
    public void testExecuteBacktest_BuyAndHold_ReversedData() throws IOException {
        Account account = new Account.Builder()
                .initialCapital(100.00)
                .build();

        Analytics analytics = new Analytics.Builder()
                .build();

        Context context = new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();

        Analytics updatedAnalytics = new Analytics.Builder()
                .addTransaction(new Transaction.Builder().quantity(10).price(20.0).status(TransactionOperation.CLOSE).build())
                .build();

        Account updatedAccount = new Account.Builder(context)
                .currentCapital(110.00)
                .build();

        List<TimeSeriesEntry> backtestEntries = mapper.readValue(new File("src/test/resources/sample-data/sample02.json"), new TypeReference<>() {
        });
        List<TimeSeriesEntry> strategyEntries = mapper.readValue(new File("src/test/resources/sample-data/sample02.json"), new TypeReference<>() {
        });

        TimeSeriesData backtestData = new TimeSeriesData(backtestEntries);
        TimeSeriesData strategyData = new TimeSeriesData(strategyEntries);

        Mockito.doNothing().when(validationService).validateTimeframes(any(TimeSeriesData.class), any(TimeSeriesData.class));
        Mockito.when(indicatorService.populateDataWithIndicators(any(TimeSeriesData.class), anyList()))
                .thenReturn(strategyData);
        Mockito.when(tradeExecutor.buy(any(Context.class), any(Transaction.class)))
                .thenReturn(new Context.Builder(context).build());
        Mockito.when(tradeExecutor.close(any(Context.class), any(Transaction.class), any(Transaction.class)))
                .thenReturn(new Context.Builder(context).account(updatedAccount).analytics(updatedAnalytics).build());

        Context response = backtestService.executeStrategy(context, buyAndHold, backtestData, strategyData);

        Assertions.assertEquals(100, response.account.initialCapital);
        Assertions.assertEquals(110, response.account.currentCapital);
        Assertions.assertEquals(0, response.account.activeTrades.size());
        Assertions.assertEquals(0, response.getActiveTransactions().size());
        Assertions.assertEquals(1, response.getClosedTransactions().size());
    }
}
