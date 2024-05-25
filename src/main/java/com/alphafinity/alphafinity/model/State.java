package com.alphafinity.alphafinity.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class State {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    public final LocalDate currentTime;
    public final Double currentAccountValue;
    public final Double currentProfit;
    public final Double currentProfitPercentage;

    public State(Builder builder) {
        this.currentTime = builder.currentTime;
        this.currentAccountValue = builder.currentAccountValue;
        this.currentProfit = builder.currentProfit;
        this.currentProfitPercentage = builder.currentProfitPercentage;
    }

    public static class Builder {
        private LocalDate currentTime;
        private Double currentAccountValue;
        private Double currentProfit;
        private Double currentProfitPercentage;

        public Builder(){

        }

        public Builder(State state){
            this.currentTime = state.currentTime;
            this.currentAccountValue = state.currentAccountValue;
            this.currentProfit = state.currentProfit;
            this.currentProfitPercentage = state.currentProfitPercentage;
        }

        public Builder currentTime(LocalDate time){
            this.currentTime = time;
            return this;
        }

        public Builder currentAccountValue(Double currentAccountValue){
            this.currentAccountValue = currentAccountValue;
            return this;
        }

        public Builder currentProfit(Double currentProfit){
            this.currentProfit = currentProfit;
            return this;
        }

        public Builder currentProfitPercentage(Double currentProfitPercentage){
            this.currentProfitPercentage = currentProfitPercentage;
            return this;
        }

        public State build() {
            return new State(this);
        }
    }
}
