package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.TimeSeriesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BacktestValidationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BacktestValidationService.class);
    public void validateTimeframes(TimeSeriesData benchmarkTimeSeriesData, TimeSeriesData strategyTimeSeriesData) {
        LOGGER.info("[Validation] Starting validations for timeframes");

        // Check if both benchmark and strategy time series data are not null
        if (benchmarkTimeSeriesData == null || strategyTimeSeriesData == null) {
            throw new IllegalArgumentException("Benchmark and strategy time series data must not be null");
        }

        // Check if both time series data have entries
        if (benchmarkTimeSeriesData.entries.isEmpty() || strategyTimeSeriesData.entries.isEmpty()) {
            throw new IllegalArgumentException("Benchmark and strategy time series data must have entries");
        }

        // Check if the timeframes match (first and last timestamps)
        if (!benchmarkTimeSeriesData.getFirstEntry().datetime.equals(strategyTimeSeriesData.getFirstEntry().datetime) ||
                !benchmarkTimeSeriesData.getLastEntry().datetime.equals(strategyTimeSeriesData.getLastEntry().datetime)) {
            throw new IllegalArgumentException("Benchmark and strategy time series data must have the same timeframes");
        }
        LOGGER.info("[Validation] Successfully completed validations for timeframes");
    }
}
