package com.alphafinity.alphafinity.model;

import java.util.ArrayList;
import java.util.List;

public class Account {

    public final Double startingCapital;
    public Double currentCapital;
    public final List<Transaction> activeTrades;

    public Account(Builder builder) {
        this.startingCapital = builder.startingCapital;
        this.currentCapital = builder.currentCapital;
        this.activeTrades = builder.activeTrades;
    }

    public static class Builder {
        private Double startingCapital;
        private Double currentCapital;
        private List<Transaction> activeTrades;

        public Builder() {
            this.activeTrades = new ArrayList<>();
        }

        public Builder(Context context) {
            this.startingCapital = context.account.startingCapital;
            this.activeTrades = context.account.activeTrades;
        }

        public Builder startingCapital(Double startingCapital){
            this.startingCapital = startingCapital;
            return this;
        }

        public Builder currentCapital(Double currentCapital){
            this.currentCapital = currentCapital;
            return this;
        }

        public Builder addTrade(Transaction activeTrades){
            this.activeTrades.add(activeTrades);
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
