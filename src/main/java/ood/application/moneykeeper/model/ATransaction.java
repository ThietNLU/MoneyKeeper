package ood.application.moneykeeper.model;

import java.time.LocalDateTime;
import java.util.*;

import lombok.Data;

@Data
public abstract class ATransaction {
    protected Wallet wallet;
    protected String tId;
    protected double amount;
    protected LocalDateTime dateTime;
    protected Category category;
    protected String description;
    protected ITransactionStrategy strategy;

    public ATransaction(Wallet wallet, double amount, Category category,
                        String description) {
        this.tId = UUID.randomUUID().toString();
        this.wallet = wallet;
        this.amount = amount;
        this.dateTime = LocalDateTime.now();
        this.category = category;
        this.description = description;

        this.wallet.addTransaction(this);

        processTrans();
    }
    public void setStrategy(ITransactionStrategy strategy) {
        this.strategy = strategy;
    }

    public abstract void processTrans();

    public abstract boolean isExpense();

    public abstract String toString();

    public boolean isId(String id){
        return this.tId.equals(id);
    }
}
