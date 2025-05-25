package ood.application.moneykeeper.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import ood.application.moneykeeper.utils.UUIDUtils;

@Data
@AllArgsConstructor
public class Transaction {
    private String tId;
    private Wallet wallet;
    private ITransactionStrategy strategy;
    private double amount;
    private LocalDateTime dateTime;
    private Category category;
    private String description;

    public Transaction(Wallet wallet, ITransactionStrategy strategy, double amount, Category category, String description) {
        this.tId = UUIDUtils.generateShortUUID();
        this.wallet = wallet;
        this.strategy = strategy;
        this.amount = amount;
        this.category = category;
        this.description = description;
    }

    public Transaction(Wallet wallet, double amount, Category category, String description) {
        this.tId = UUIDUtils.generateShortUUID();
        this.wallet = wallet;
        this.amount = amount;
        this.category = category;
        this.description = description;
    }

    public void setStrategy(ITransactionStrategy strategy) {
        this.strategy = strategy;
    }

    public void processWallet() {
        strategy.processWallet(wallet, amount);
    }

    public void processBudget(BudgetManager budgetManager) {
        strategy.processBudget(budgetManager, this);
    }

    public boolean isExpense() {
        return strategy.isExpense();
    }

    public String toString() {
        return "Id: " + tId + "\nWallet: " + wallet.getName() + "\nAmount: " + amount
                + "\nCategory: " + category.getName() + "\nDescription: " + description
                + "\nIs expense: " + isExpense();
    }

}
