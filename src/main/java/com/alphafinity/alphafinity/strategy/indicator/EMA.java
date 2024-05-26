package com.alphafinity.alphafinity.strategy.indicator;

import com.alphafinity.alphafinity.model.TimeSeriesData;
import com.alphafinity.alphafinity.model.TimeSeriesEntry;

import java.util.List;

import static com.alphafinity.alphafinity.utility.IndicatorConstants.EMA_NAME;


public class EMA implements Indicator<Double> {

    private final int period;

    public EMA(Builder builder) {
        this.period = builder.period;
    }


    @Override
    public String name() {
        return EMA_NAME;
    }

    @Override
    public Double calculate(TimeSeriesData historicalData, TimeSeriesEntry currentEntry) {
        List<TimeSeriesEntry> entries = historicalData.entries;
        int currentIndex = entries.indexOf(currentEntry);

        if (currentIndex < period) {
            return 0.0; // Return 0.00 if there's not enough historical data
        }

        double smoothingFactor = 2.0 / (period + 1);
        double ema = entries.get(currentIndex - period + 1).close; // Initial EMA value

        for (int i = currentIndex - period + 2; i <= currentIndex; i++) {
            double close = entries.get(i).close;
            ema = (close - ema) * smoothingFactor + ema;
        }

        return ema;
    }

    public static class Builder {
        private Integer period;

        public Builder(){

        }

        public EMA.Builder period(Integer period){
            this.period = period;
            return this;
        }

        public EMA build() {
            return new EMA(this);
        }

    }

}
