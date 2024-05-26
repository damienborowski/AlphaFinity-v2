package com.alphafinity.alphafinity.service;

import com.alphafinity.alphafinity.model.TimeSeriesData;
import com.alphafinity.alphafinity.model.TimeSeriesEntry;
import com.alphafinity.alphafinity.strategy.indicator.Indicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.alphafinity.alphafinity.utility.IndicatorConstants.*;

@Service
public class IndicatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndicatorService.class);

    public TimeSeriesData populateDataWithIndicators(TimeSeriesData historicalData, List<Indicator<?>> indicators) {
        LOGGER.info("[Indicators] Starting enrichment of time-series data for the following indicators: " + getIndicatorNames(indicators));

        List<TimeSeriesEntry> timeSeriesEntries = historicalData.entries.stream()
                .map(entry -> {
                    TimeSeriesEntry.Builder newEntry = new TimeSeriesEntry.Builder(entry);


                    indicators.stream()
                            .map(indicator ->
                                    switch (indicator.name()) {
                                        case RSI_NAME -> newEntry.rsi(indicator.calculate(historicalData, entry));
                                        case EMA_NAME -> newEntry.ema(indicator.calculate(historicalData, entry));
                                        default -> throw new IllegalStateException("Unexpected value: " + indicator.name());
                                    })
                                    .collect(Collectors.toSet());

                    return newEntry.build();
                })
                .toList();

        TimeSeriesData timeSeriesData = new TimeSeriesData(timeSeriesEntries);

        LOGGER.info("[Indicators] Completed enrichment of time-series data");
        return timeSeriesData;
    }

    private List<String> getIndicatorNames(List<Indicator<?>> indicators) {
        return indicators.stream()
                .map(Indicator::name)
                .toList();
    }
}
