package com.alphafinity.alphafinity.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class TimeSeriesEntry {
    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    public final LocalDate datetime;
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

    public TimeSeriesEntry(@JsonProperty("date") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy") LocalDate datetime,
                           @JsonProperty("open") Double open,
                           @JsonProperty("close") Double close,
                           @JsonProperty("high") Double high,
                           @JsonProperty("low") Double low,
                           @JsonProperty("volume") Double volume) {
        this.datetime = datetime;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }
}
