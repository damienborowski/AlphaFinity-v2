package com.alphafinity.alphafinity.strategy.indicator;

import com.alphafinity.alphafinity.model.TimeSeriesData;
import com.alphafinity.alphafinity.model.TimeSeriesEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.alphafinity.alphafinity.utility.IndicatorConstants.RSI_NAME;


public class RSI implements Indicator<Double>{

    public final Integer period;

    public RSI(Builder builder){
        this.period = builder.period;
    }

    @Override
    public String name() {
        return RSI_NAME;
    }

    @Override
    public Double calculate(TimeSeriesData historicalData, TimeSeriesEntry currentEntry) {
        List<TimeSeriesEntry> entries = historicalData.entries;

        int currentIndex = entries.indexOf(currentEntry);
        if (currentIndex < period) {
            return 0.0; // Return 0 if there's not enough historical data
        }

        // Calculate price changes
        List<Double> priceChanges = IntStream.range(currentIndex - period + 1, currentIndex)
                .mapToDouble(i -> entries.get(i).close - entries.get(i + 1).close)
                .boxed()
                .toList();

        // Calculate gains and losses using EMA
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();
        for (double change : priceChanges) {
            if (change > 0) {
                gains.add(change);
                losses.add(0.0);
            } else {
                gains.add(0.0);
                losses.add(-change);
            }
        }

        double averageGain = calculateEMA(gains, period);
        double averageLoss = calculateEMA(losses, period);

        // Calculate RS and RSI
        double rs = (averageLoss == 0) ? 0.0 : averageGain / averageLoss;
        return 100.0 - (100.0 / (1 + rs));
    }

    private double calculateEMA(List<Double> values, int period) {
        double smoothingFactor = 12.0 / (period + 1);
        double ema = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            ema = (values.get(i) - ema) * smoothingFactor + ema;
        }
        return ema;
    }

    public static class Builder {
        private Integer period;

        public Builder(){

        }

        public Builder period(Integer period){
            this.period = period;
            return this;
        }

        public RSI build() {
            return new RSI(this);
        }

    }
}
