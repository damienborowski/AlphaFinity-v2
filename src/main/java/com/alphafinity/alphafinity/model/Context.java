package com.alphafinity.alphafinity.model;

import com.alphafinity.alphafinity.model.enumerations.TransactionOperation;

import java.util.ArrayList;
import java.util.List;

public class Context {
    
    public final Account account;
    public final Analytics analytics;
    public final List<State> states;

    public Context(Builder builder) {
        this.account = builder.account;
        this.analytics = builder.analytics;
        this.states = builder.states;
    }

    public List<Transaction> getTransactions(){
        return account.activeTrades;
    }

    public List<Transaction> getActiveTransactions(){
        return account.activeTrades.stream()
                .filter(transaction -> TransactionOperation.OPEN.equals(transaction.operation))
                .toList();
    }

    public List<Transaction> getClosedTransactions(){
        return analytics.transactions.stream()
                .filter(transaction -> TransactionOperation.CLOSE.equals(transaction.operation))
                .toList();
    }

    public static class Builder {
        private Account account;
        private Analytics analytics;
        private List<State> states;

        public Builder(){
            this.account = new Account.Builder()
                    .initialCapital(1000.00)
                    .build();

            this.analytics = new Analytics.Builder()
                    .build();

            this.states = new ArrayList<>();
        }

        public Builder(Context context){
            this.analytics = context.analytics;
            this.account = context.account;
            this.states = context.states;
        }

        public Builder account(Account account){
            this.account = account;
            return this;
        }

        public Builder analytics(Analytics analytics){
            this.analytics = analytics;
            return this;
        }

        public Builder addState(State state){
            states.add(state);
            return this;
        }

        public Context build() {
            return new Context(this);
        }
    }
}
