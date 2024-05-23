package com.alphafinity.alphafinity.model;

import java.util.Comparator;
import java.util.List;

public class TimeSeriesData {
    private static final String NO_ENTRIES = "No entries in time series data";
    public final List<TimeSeriesEntry> entries;

    public TimeSeriesData(List<TimeSeriesEntry> entries) {
        this.entries = entries.stream()
                .sorted(Comparator.comparing(entry -> entry.datetime))
                .toList();;
    }

    public TimeSeriesEntry getFirstEntry() {
        if (entries.isEmpty()) {
            throw new IllegalStateException(NO_ENTRIES);
        }
        return entries.get(0);
    }

    public TimeSeriesEntry getLastEntry(){
        if (entries.isEmpty()) {
            throw new IllegalStateException(NO_ENTRIES);
        }

        return entries.get(entries.size()-1);
    }
}
