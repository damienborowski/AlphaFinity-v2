package com.alphafinity.alphafinity.controller;

import com.alphafinity.alphafinity.model.*;
import com.alphafinity.alphafinity.service.BacktestService;
import com.alphafinity.alphafinity.service.Strategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/api/v1/backtest")
public class BacktestController {

    private final BacktestService backtestService;
    private final Strategy buyAndHold;
    private final ObjectMapper mapper;

    public BacktestController(BacktestService backtestService,
                              Strategy buyAndHold,
                              ObjectMapper mapper) {
        this.backtestService = backtestService;
        this.buyAndHold = buyAndHold;
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
                .body(backtestService.executeStrategy(context, buyAndHold, benchmarkTimeSeriesData, strategyTimeSeriesData));
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) throws IOException {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        InputStream inputStreamBenchmark = classloader.getResourceAsStream("spy_daily.json");
        InputStream inputStreamStrategy = classloader.getResourceAsStream("spy_daily.json");

        Context context = new Context.Builder()
                .build();
        List<TimeSeriesEntry> benchmarkEntries = mapper.readValue(inputStreamBenchmark, new TypeReference<>() {});
        List<TimeSeriesEntry> strategyEntries = mapper.readValue(inputStreamStrategy, new TypeReference<>() {});

        TimeSeriesData benchmarkTimeSeriesData = new TimeSeriesData(benchmarkEntries);
        TimeSeriesData strategyTimeSeriesData = new TimeSeriesData(strategyEntries);

        Context response = backtestService.executeStrategy(context, buyAndHold, benchmarkTimeSeriesData, strategyTimeSeriesData);

        model.addAttribute("context", response);
        model.addAttribute("states", response.states);

        return "index";
    }
}
