package ood.application.moneykeeper.model;

import java.time.LocalDateTime;

public interface ITransactionStrategy {
    public void process(Wallet wallet, double amount, LocalDateTime dateTime, boolean isExpense, Category category);

}
