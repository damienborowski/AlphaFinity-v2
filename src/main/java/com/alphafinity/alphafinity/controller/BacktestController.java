package com.alphafinity.alphafinity.controller;

import com.alphafinity.alphafinity.model.*;
import com.alphafinity.alphafinity.service.BacktestService;
import com.alphafinity.alphafinity.service.Strategy;
import com.alphafinity.alphafinity.strategy.BuyAndHold;
import com.alphafinity.alphafinity.strategy.EMAStrategy;
import com.alphafinity.alphafinity.strategy.RSIStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/api/v1/backtest")
public class BacktestController {

    private final BacktestService backtestService;
    private final BuyAndHold buyAndHold;
    private final EMAStrategy ema;
    private final RSIStrategy rsiStrategy;
    private final ObjectMapper mapper;

    public BacktestController(BacktestService backtestService,
                              BuyAndHold buyAndHold,
                              EMAStrategy ema,
                              RSIStrategy rsiStrategy,
                              ObjectMapper mapper) {
        this.backtestService = backtestService;
        this.buyAndHold = buyAndHold;
        this.ema = ema;
        this.rsiStrategy = rsiStrategy;
        this.mapper = mapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> startBacktest(@RequestParam("benchmarkData") MultipartFile benchmark,
                                           @RequestParam("strategyData") MultipartFile data) throws IOException {
        Context context = new Context.Builder()
                .build();

        List<TimeSeriesEntry> benchmarkEntries = mapper.readValue(benchmark.getInputStream(), new TypeReference<>() {});
        List<TimeSeriesEntry> strategyEntries = mapper.readValue(data.getInputStream(), new TypeReference<>() {});

        TimeSeriesData benchmarkTimeSeriesData = new TimeSeriesData(benchmarkEntries);
        TimeSeriesData strategyTimeSeriesData = new TimeSeriesData(strategyEntries);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(backtestService.executeStrategy(context, rsiStrategy, benchmarkTimeSeriesData, strategyTimeSeriesData));
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) throws IOException {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        InputStream inputStreamBenchmark = classloader.getResourceAsStream("spy_hourly.json");
        InputStream inputStreamStrategy = classloader.getResourceAsStream("spy_hourly.json");

        Context context = new Context.Builder()
                .build();
        List<TimeSeriesEntry> benchmarkEntries = mapper.readValue(inputStreamBenchmark, new TypeReference<>() {});
        List<TimeSeriesEntry> strategyEntries = mapper.readValue(inputStreamStrategy, new TypeReference<>() {});

        TimeSeriesData benchmarkTimeSeriesData = new TimeSeriesData(benchmarkEntries);
        TimeSeriesData strategyTimeSeriesData = new TimeSeriesData(strategyEntries);

        Context response = backtestService.executeStrategy(context, rsiStrategy, benchmarkTimeSeriesData, strategyTimeSeriesData);

        // Get initial values
        double initialAccountValue = response.states.get(0).currentAccountValue;
        double initialBenchmarkValue = benchmarkTimeSeriesData.entries.get(0).close;

        // Normalize states
        List<State> normalizedStates = response.states.stream()
                .map(state -> new State.Builder(state)
                        .currentAccountValue(((state.currentAccountValue / initialAccountValue) - 1) * 100)
                        .build())
                .collect(Collectors.toList());

        // Normalize benchmark entries
        List<TimeSeriesEntry> normalizedBenchmarkEntries = benchmarkTimeSeriesData.entries.stream()
                .map(entry -> new TimeSeriesEntry(entry.datetime, 0.00, ((entry.close / initialBenchmarkValue) - 1) * 100, 0.00, 0.00, entry.volume, 0.00, 0.00))
                .collect(Collectors.toList());

        TimeSeriesData normalizedBenchmarkTimeSeriesData = new TimeSeriesData(normalizedBenchmarkEntries);

        model.addAttribute("context", response);
        model.addAttribute("states", normalizedStates);
        model.addAttribute("benchmark", normalizedBenchmarkTimeSeriesData.entries);

        return "index";
    }
}
