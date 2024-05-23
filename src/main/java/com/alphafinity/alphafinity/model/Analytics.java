package com.alphafinity.alphafinity.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Analytics {
    public final Double startingCapital;
    public final Double endingCapital;
    public final LocalDate startDate;
    public final LocalDate endDate;
    public final Double totalReturn;
    public final Double totalReturnMultiplier;
    public final Double totalReturnAsPercentage;
    public final Double sharpeRatio;
    public final Double maxDrawdown;
    public final Double winRate;
    public final Double averageWin;
    public final Double averageLoss;
    public final Integer totalTrades;
    public final Double alpha;
    public final Double standardDeviation;
    public final List<Transaction> transactions;

    public Analytics(Builder builder) {
        this.startingCapital = builder.startingCapital;
        this.endingCapital = builder.endingCapital;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.totalReturn = builder.totalReturn;
        this.totalReturnMultiplier = builder.totalReturnMultiplier;
        this.totalReturnAsPercentage = builder.totalReturnAsPercentage;
        this.sharpeRatio = builder.sharpeRatio;
        this.maxDrawdown = builder.maxDrawdown;
        this.winRate = builder.winRate;
        this.averageWin = builder.averageWin;
        this.averageLoss = builder.averageLoss;
        this.totalTrades = builder.totalTrades;
        this.alpha = builder.alpha;
        this.standardDeviation = builder.standardDeviation;
        this.transactions = builder.transactions;
    }

    public static class Builder {

        private Double startingCapital;
        private Double endingCapital;
        private LocalDate startDate;
        private LocalDate endDate;
        private Double totalReturn;
        private Double totalReturnMultiplier;
        private Double totalReturnAsPercentage;
        private Double sharpeRatio;
        private Double maxDrawdown;
        private Double winRate;
        private Double averageWin;
        private Double averageLoss;
        private Integer totalTrades;
        private Double alpha;
        private Double standardDeviation;
        private final List<Transaction> transactions;

        public Builder() {
            this.transactions = new ArrayList<>();
        }

        public Builder(Context context) {
            this.startDate = context.analytics.startDate;
            this.endDate = context.analytics.endDate;
            this.transactions = context.analytics.transactions;

            this.startingCapital = context.account.startingCapital;
        }

        public Builder addTransaction (Transaction transaction) {
            this.transactions.add(transaction);
            return this;
        }

        public Builder endingCapital (Double endingCapital) {
            this.endingCapital = endingCapital;
            return this;
        }

        public Builder startDate (LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate (LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder totalReturn (Double totalReturn) {
            this.totalReturn = totalReturn;
            return this;
        }

        public Builder totalReturnMultiplier(Double totalReturnMultiplier) {
            this.totalReturnMultiplier = totalReturnMultiplier;
            return this;
        }

        public Builder totalReturnAsPercentage (Double totalReturnAsPercentage) {
            this.totalReturnAsPercentage = totalReturnAsPercentage;
            return this;
        }

        public Builder sharpeRatio (Double sharpeRatio) {
            this.sharpeRatio = sharpeRatio;
            return this;
        }

        public Builder maxDrawdown (Double maxDrawdown) {
            this.maxDrawdown = maxDrawdown;
            return this;
        }

        public Builder winRate (Double winRate) {
            this.winRate = winRate;
            return this;
        }

        public Builder averageWin (Double averageWin) {
            this.averageWin = endingCapital;
            return this;
        }

        public Builder averageLoss (Double averageLoss) {
            this.averageLoss = averageLoss;
            return this;
        }

        public Builder totalTrades (Integer totalTrades) {
            this.totalTrades = totalTrades;
            return this;
        }

        public Builder alpha (Double alpha) {
            this.alpha = alpha;
            return this;
        }

        public Builder standardDeviation (Double standardDeviation) {
            this.standardDeviation = standardDeviation;
            return this;
        }

        public Analytics build() {
            return new Analytics(this);
        }

    }
}
