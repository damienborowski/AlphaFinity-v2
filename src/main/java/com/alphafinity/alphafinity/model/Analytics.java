package com.alphafinity.alphafinity.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Analytics {
    public final Double startingCapital;
    public final Double endingCapital;
    public final LocalDateTime startDate;
    public final LocalDateTime endDate;
    public final Double totalReturn;
    public final Double totalReturnMultiplier;
    public final Double totalReturnAsPercentage;
    public final Double sharpeRatio;
    public final Double maxDrawdown;
    public final Double winRate;
    public final Double averageProfit;
    public final Double averageLoss;
    public final Double averageReturn;
    public final Integer totalTrades;
    public final Integer totalOpeningTrades;
    public final Integer totalClosingTrades;
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
        this.averageProfit = builder.averageProfit;
        this.averageLoss = builder.averageLoss;
        this.averageReturn = builder.averageReturn;
        this.totalTrades = builder.totalTrades;
        this.totalOpeningTrades = builder.totalOpeningTrades;
        this.totalClosingTrades = builder.totalClosingTrades;
        this.alpha = builder.alpha;
        this.standardDeviation = builder.standardDeviation;
        this.transactions = builder.transactions;
    }

    public static class Builder {

        private Double startingCapital;
        private Double endingCapital;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Double totalReturn;
        private Double totalReturnMultiplier;
        private Double totalReturnAsPercentage;
        private Double sharpeRatio;
        private Double maxDrawdown;
        private Double winRate;
        private Double averageProfit;
        private Double averageLoss;
        private Double averageReturn;
        private Integer totalTrades;
        private Integer totalOpeningTrades;
        private Integer totalClosingTrades;
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

            this.startingCapital = context.account.initialCapital;
        }

        public Builder addTransaction (Transaction transaction) {
            this.transactions.add(transaction);
            return this;
        }

        public Builder endingCapital (Double endingCapital) {
            this.endingCapital = endingCapital;
            return this;
        }

        public Builder startDate (LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate (LocalDateTime endDate) {
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

        public Builder averageProfit(Double averageProfit) {
            this.averageProfit = averageProfit;
            return this;
        }

        public Builder averageReturn(Double averageReturn) {
            this.averageReturn = averageReturn;
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

        public Builder totalOpeningTrades (Integer totalOpeningTrades) {
            this.totalOpeningTrades = totalOpeningTrades;
            return this;
        }

        public Builder totalClosingTrades (Integer totalClosingTrades) {
            this.totalClosingTrades = totalClosingTrades;
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
