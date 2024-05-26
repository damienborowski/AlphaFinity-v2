package com.alphafinity.alphafinity.strategy.indicator;

import com.alphafinity.alphafinity.model.TimeSeriesData;
import com.alphafinity.alphafinity.model.TimeSeriesEntry;

import java.util.List;
import java.util.stream.IntStream;

import static com.alphafinity.alphafinity.utility.IndicatorConstants.RSI_NAME;

public class RSI implements Indicator<Double> {

    public final Integer period;

    public RSI(Builder builder) {
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
        double[] priceChanges = IntStream.range(1, entries.size())
                .mapToDouble(i -> entries.get(i).close - entries.get(i - 1).close)
                .toArray();

        // Separate gains and losses
        double[] gains = new double[priceChanges.length];
        double[] losses = new double[priceChanges.length];
        for (int i = 0; i < priceChanges.length; i++) {
            if (priceChanges[i] > 0) {
                gains[i] = priceChanges[i];
                losses[i] = 0;
            } else {
                gains[i] = 0;
                losses[i] = -priceChanges[i];
            }
        }

        // Calculate initial average gain and loss
        double averageGain = 0.0;
        double averageLoss = 0.0;
        for (int i = 0; i < period; i++) {
            averageGain += gains[i];
            averageLoss += losses[i];
        }
        averageGain /= period;
        averageLoss /= period;

        // Apply Wilder's smoothing method
        for (int i = period; i < currentIndex; i++) {
            averageGain = (averageGain * (period - 1) + gains[i]) / period;
            averageLoss = (averageLoss * (period - 1) + losses[i]) / period;
        }

        // Calculate RS and RSI
        double rs = (averageLoss == 0) ? 0.0 : averageGain / averageLoss;
        return 100.0 - (100.0 / (1 + rs));
    }

    public static class Builder {
        private Integer period;

        public Builder() {
        }

        public Builder period(Integer period) {
            this.period = period;
            return this;
        }

        public RSI build() {
            return new RSI(this);
        }
    }
}
