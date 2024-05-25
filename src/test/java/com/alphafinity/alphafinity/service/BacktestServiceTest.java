package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.*;
import com.alphafinity.alphafinity.service.strategy.BuyAndHold;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BacktestServiceTest {

    public BacktestService backtestService;
    public ObjectMapper mapper;

    @Mock
    private BacktestValidationService validationService;
    @Mock
    private AnalyticsService analyticsService;
    @Mock
    private BacktesterTradeExecutor tradeExecutor;

    BuyAndHold buyAndHold;


    // TODO These should get mocked eventually
    @BeforeAll
    public void setUp() {
        backtestService = new BacktestService(tradeExecutor, analyticsService, validationService);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        buyAndHold = new BuyAndHold(new BacktesterTradeExecutor());
    }

    @Test
    public void testExecuteBacktest_BuyAndHold() throws IOException {
        Account account = new Account.Builder()
                .startingCapital(100.00)
                .build();

        Analytics analytics = new Analytics.Builder()
                .build();

        Context context = new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();

        List<TimeSeriesEntry> backtestEntries = mapper.readValue(new File("src/test/resources/sample-data/sample01.json"), new TypeReference<>() {
        });
        List<TimeSeriesEntry> strategyEntries = mapper.readValue(new File("src/test/resources/sample-data/sample01.json"), new TypeReference<>() {
        });

        TimeSeriesData backtestData = new TimeSeriesData(backtestEntries);
        TimeSeriesData strategyData = new TimeSeriesData(strategyEntries);

        Mockito.when(tradeExecutor.buy(any(Context.class), any(Transaction.class)))
                .thenReturn(new Context.Builder()
                        .build());

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
                .startingCapital(100.00)
                .build();

        Analytics analytics = new Analytics.Builder()
                .build();

        Context context = new Context.Builder()
                .account(account)
                .analytics(analytics)
                .build();

        List<TimeSeriesEntry> backtestEntries = mapper.readValue(new File("src/test/resources/sample-data/sample02.json"), new TypeReference<>() {
        });
        List<TimeSeriesEntry> strategyEntries = mapper.readValue(new File("src/test/resources/sample-data/sample02.json"), new TypeReference<>() {
        });

        TimeSeriesData backtestData = new TimeSeriesData(backtestEntries);
        TimeSeriesData strategyData = new TimeSeriesData(strategyEntries);

        Context response = backtestService.executeStrategy(context, buyAndHold, backtestData, strategyData);

        Assertions.assertEquals(100, response.account.initialCapital);
        Assertions.assertEquals(110, response.account.currentCapital);
        Assertions.assertEquals(0, response.account.activeTrades.size());
        Assertions.assertEquals(0, response.getActiveTransactions().size());
        Assertions.assertEquals(1, response.getClosedTransactions().size());
    }
}
