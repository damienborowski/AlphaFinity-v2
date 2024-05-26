package com.alphafinity.alphafinity.strategy.indicator;

import com.alphafinity.alphafinity.model.TimeSeriesData;
import com.alphafinity.alphafinity.model.TimeSeriesEntry;

public interface Indicator<T> {

    String name();
    Double calculate(TimeSeriesData historicalData, TimeSeriesEntry currentEntry); // Generic method to calculate the indicator
}
