package com.alphafinity.alphafinity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

import static com.alphafinity.alphafinity.utility.IndicatorConstants.*;

public class TimeSeriesEntry {
    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    public final LocalDateTime datetime;
    @JsonProperty("open")
    public final Double open;
    @JsonProperty("close")
    public final Double close;
    @JsonProperty("high")
    public final Double high;
    @JsonProperty("low")
    public final Double low;
    @JsonProperty("volume")
    public final Double volume;
    @JsonProperty(RSI_NAME)
    public final Double rsi;
    @JsonProperty(EMA_NAME)
    public final Double ema;

    public TimeSeriesEntry(@JsonProperty("date") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss") LocalDateTime datetime,
                           @JsonProperty("open") Double open,
                           @JsonProperty("close") Double close,
                           @JsonProperty("high") Double high,
                           @JsonProperty("low") Double low,
                           @JsonProperty("volume") Double volume,
                           @JsonProperty(RSI_NAME) Double rsi,
                           @JsonProperty(EMA_NAME) Double ema) {
        this.datetime = datetime;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.rsi = rsi;
        this.ema = ema;
    }

    public TimeSeriesEntry(Builder builder){
        this.datetime = builder.datetime;
        this.open = builder.open;
        this.close = builder.close;
        this.high = builder.high;
        this.low = builder.low;
        this.volume = builder.volume;
        this.rsi = builder.rsi;
        this.ema = builder.ema;
    }

    public static class Builder {
        private LocalDateTime datetime;
        private Double open;
        private Double close;
        private Double high;
        private Double low;
        private Double volume;
        private Double rsi;
        private Double ema;

        public Builder(){

        }

        public Builder(TimeSeriesEntry entry){
            this.datetime = entry.datetime;
            this.open = entry.open;
            this.close = entry.close;
            this.high = entry.high;
            this.low = entry.low;
            this.volume = entry.volume;
            this.rsi = entry.rsi;
        }

        public Builder datetime(LocalDateTime datetime){
            this.datetime = datetime;
            return this;
        }

        public Builder open(Double open){
            this.open = open;
            return this;
        }

        public Builder close(Double close){
            this.close = close;
            return this;
        }

        public Builder high(Double high){
            this.high = high;
            return this;
        }

        public Builder low(Double low){
            this.low = low;
            return this;
        }

        public Builder volume(Double volume){
            this.volume = volume;
            return this;
        }

        public Builder rsi(Double rsi){
            this.rsi = rsi;
            return this;
        }

        public Builder ema(Double ema){
            this.ema = ema;
            return this;
        }

        public TimeSeriesEntry build() {
            return new TimeSeriesEntry(this);
        }
    }
}
