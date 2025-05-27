package ood.application.moneykeeper.model;

import java.util.*;
import java.util.stream.Collectors;

import ood.application.moneykeeper.utils.UUIDUtils;
import ood.application.moneykeeper.observer.AbstractSubject;

public class Wallet extends AbstractSubject {
    private String id;
    private String name;
    private double balance;
    private List<Transaction> transactions;
    private User owner;
    private java.time.LocalDateTime creationDate;

    // Default constructor
    public Wallet() {
        this.id = UUIDUtils.generateShortUUID();
        this.transactions = new ArrayList<>();
    }

    public Wallet(String name, double balance, User owner) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        this.owner = owner;
    }

    public Wallet(String id, String name, double balance, User user) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        this.owner = user;
    }

    public Wallet(String name, double balance, User owner, java.time.LocalDateTime creationDate) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        this.owner = owner;
        this.creationDate = creationDate;
    }

    public Wallet(String id, String name, double balance, User user, java.time.LocalDateTime creationDate) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        this.owner = user;
        this.creationDate = creationDate;
    }    public void income(double amount) {
        double oldBalance = this.balance;
        this.balance += amount;
        checkBalanceConditions(oldBalance);
    }

    public void expense(double amount) {
        double oldBalance = this.balance;
        this.balance -= amount;
        checkBalanceConditions(oldBalance);
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        double oldBalance = this.balance;
        transaction.processWallet();
        
        // Thông báo transaction được thêm vào ví
        notifyObservers("TRANSACTION_ADDED_TO_WALLET", transaction);
        
        // Kiểm tra số dư sau khi thêm transaction
        checkBalanceConditions(oldBalance);
    }

    public String printTransactions() {
        return this.transactions.stream()
                .map(t -> t.toString()).collect(Collectors.joining("\n===========*===========\n"));
    }

    public int countTransactions() {
        return this.transactions.size();
    }    /**
     * Check if current balance is considered low (below 10% of initial balance or below 100,000 VND)
     */
    public boolean isLowBalance() {
        double lowThreshold = Math.max(100000.0, this.balance * 0.1); // Minimum 100k VND or 10% of current balance
        return this.balance < lowThreshold;
    }

    /**
     * Check if current balance is critically low (below 50,000 VND)
     */
    public boolean isCriticallyLowBalance() {
        return this.balance < 50000.0;
    }

    /**
     * Update wallet balance directly and notify observers
     */
    public void updateBalance(double newBalance) {
        double oldBalance = this.balance;
        this.balance = newBalance;
        
        // Check balance conditions and notify observers
        checkBalanceConditions(oldBalance);
    }

    /**
     * Kiểm tra điều kiện số dư và thông báo cho observers
     */
    private void checkBalanceConditions(double oldBalance) {
        // Thông báo khi số dư thay đổi
        notifyObservers("WALLET_BALANCE_UPDATED", this);
        
        // Thông báo khi số dư thấp
        if (isCriticallyLowBalance()) {
            notifyObservers("WALLET_CRITICALLY_LOW", this);
        } else if (isLowBalance() && oldBalance >= 100000.0) {
            // Chỉ thông báo khi chuyển từ số dư bình thường sang thấp
            notifyObservers("WALLET_LOW_BALANCE", this);
        }
        
        // Thông báo khi số dư âm (thấu chi)
        if (this.balance < 0) {
            notifyObservers("WALLET_NEGATIVE_BALANCE", this);
        }
    }

    public boolean isId(String id) {
        return this.id.equals(id);
    }

    public boolean removeTransaction(String id) {
        Iterator<Transaction> iterator = this.transactions.iterator();
        while (iterator.hasNext()) {
            Transaction trans = iterator.next();
            if (trans.getTId().equals(id)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public String getInfo() {
        return this.id + "\t" + this.name + "\t" + this.balance;
    }

    public String getInfoAllTrans() {
        return this.transactions.stream().map(t -> t.toString()).collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() {
        return "Id: " + id + "\nName: " + name + "\nBalance: " + balance;
    }

    // Explicit getter methods to fix Lombok compilation issues
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public User getOwner() {
        return owner;
    }

    public java.time.LocalDateTime getCreationDate() {
        return creationDate;
    }

    // Explicit setter methods to fix Lombok compilation issues
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setCreationDate(java.time.LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
