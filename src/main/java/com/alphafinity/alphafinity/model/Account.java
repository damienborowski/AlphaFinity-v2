package com.alphafinity.alphafinity.model;

import java.util.ArrayList;
import java.util.List;

public class Account {

    public final Double initialCapital;
    public Double currentCapital;
    public final List<Transaction> activeTrades;

    public Account(Builder builder) {
        this.initialCapital = builder.initialCapital;
        this.currentCapital = builder.currentCapital;
        this.activeTrades = builder.activeTrades;
    }

    public static class Builder {
        private Double initialCapital;
        private Double currentCapital;
        private List<Transaction> activeTrades;

        public Builder() {
            this.activeTrades = new ArrayList<>();
        }

        public Builder(Context context) {
            this.initialCapital = context.account.initialCapital;
            this.activeTrades = context.account.activeTrades;
        }

        public Builder initialCapital(Double initialCapital){
            this.initialCapital = initialCapital;
            return this;
        }

        public Builder currentCapital(Double currentCapital){
            this.currentCapital = currentCapital;
            return this;
        }

        public Builder addTrade(Transaction activeTrade){
            this.activeTrades.add(activeTrade);
            return this;
        }

        public Builder addTrades(List<Transaction> activeTrades){
            this.activeTrades.addAll(activeTrades);
            return this;
        }

        public Builder removeTrade(Transaction activeTrades){
            this.activeTrades.remove(activeTrades);
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}
