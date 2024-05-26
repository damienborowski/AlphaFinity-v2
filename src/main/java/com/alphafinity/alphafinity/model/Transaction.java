package com.alphafinity.alphafinity.model;

import com.alphafinity.alphafinity.model.enumerations.Quantity;
import com.alphafinity.alphafinity.model.enumerations.TransactionOperation;
import com.alphafinity.alphafinity.model.enumerations.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class Transaction {

    public final LocalDateTime time;
    @Min(1)
    @NotNull
    public final Double price;
    public final Double totalCost;
    public final Double profit;
    @Min(1)
    @NotNull
    public final Integer quantity;
    public final Quantity quantityEnum;
    @NotNull
    public final TransactionType type;
    @NotNull
    public final TransactionOperation operation;

    public Transaction(Builder builder) {
        this.price = builder.price;
        this.quantity = builder.quantity;
        this.type = builder.type;
        this.operation = builder.operation;
        this.time = builder.time;
        this.totalCost = builder.totalCost;
        this.profit = builder.profit;
        this.quantityEnum = builder.quantityEnum;
    }

    public static class Builder {
        public LocalDateTime time;
        private Double price;
        private Integer quantity;
        private TransactionType type;
        private TransactionOperation operation;
        private Double totalCost;
        private Double profit;
        private Quantity quantityEnum;

        public Builder (){
            this.quantity = 0;
            this.quantityEnum = Quantity.NOT_SET;
        }

        public Builder (Transaction transaction){
            this.time = transaction.time;
            this.price = transaction.price;
            this.quantity = transaction.quantity;
            this.totalCost = transaction.totalCost;
            this.profit = transaction.profit;
            this.type = transaction.type;
            this.operation = transaction.operation;
            this.quantityEnum = transaction.quantityEnum;
        }

        public Builder price(Double price){
            this.price = price;
            return this;
        }

        public Builder profit(Double profit){
            this.profit = profit;
            return this;
        }

        public Builder quantity(Quantity quantityEnum){
            this.quantityEnum = quantityEnum;
            return this;
        }

        public Builder time(LocalDateTime time){
            this.time = time;
            return this;
        }

        public Builder quantity(Integer quantity){
            this.quantity = quantity;
            return this;
        }

        public Builder status(TransactionOperation status){
            this.operation = status;
            return this;
        }

        public Builder type(TransactionType type){
            this.type = type;
            return this;
        }

        public Transaction build() {
            this.totalCost = this.price * this.quantity;
            return new Transaction(this);
        }
    }
}
