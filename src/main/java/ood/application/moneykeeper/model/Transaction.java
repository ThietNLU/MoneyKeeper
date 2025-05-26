package ood.application.moneykeeper.model;

import java.time.LocalDateTime;

import ood.application.moneykeeper.utils.UUIDUtils;

public class Transaction {
    private String tId;
    private Wallet wallet;
    private ITransactionStrategy strategy;
    private double amount;
    private LocalDateTime dateTime;
    private Category category;
    private String description;    public Transaction(Wallet wallet, ITransactionStrategy strategy, double amount, Category category, String description) {
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

    // Default constructor
    public Transaction() {
    }

    // All-args constructor
    public Transaction(String tId, Wallet wallet, ITransactionStrategy strategy, double amount, LocalDateTime dateTime, Category category, String description) {
        this.tId = tId;
        this.wallet = wallet;
        this.strategy = strategy;
        this.amount = amount;
        this.dateTime = dateTime;
        this.category = category;
        this.description = description;
    }    public void setStrategy(ITransactionStrategy strategy) {
        this.strategy = strategy;
    }

    // Getter and setter methods
    public String getTId() {
        return tId;
    }

    public void setTId(String tId) {
        this.tId = tId;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public ITransactionStrategy getStrategy() {
        return strategy;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return "Id: " + tId + "\nWallet: " + wallet.getName() + "\nAmount: " + amount
                + "\nCategory: " + category.getName() + "\nDescription: " + description
                + "\nIs expense: " + isExpense();
    }

}
