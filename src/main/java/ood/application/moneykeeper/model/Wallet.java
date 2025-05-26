package ood.application.moneykeeper.model;

import java.util.*;
import java.util.stream.Collectors;

import ood.application.moneykeeper.utils.UUIDUtils;

public class Wallet {
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
    }

    public void income(double amount) {
        this.balance += amount;
    }

    public void expense(double amount) {
        this.balance -= amount;
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        transaction.processWallet();
    }

    public String printTransactions() {
        return this.transactions.stream()
                .map(t -> t.toString()).collect(Collectors.joining("\n===========*===========\n"));
    }

    public int countTransactions() {
        return this.transactions.size();
    }

    /**
     * Check if current balance is considered low
     */
    public boolean isLowBalance() {
        return this.balance < 1000.0; // Default threshold
    }
    
    /**
     * Check if previous balance was low
     */
    private boolean wasLowBalance(double previousBalance) {
        return previousBalance < 1000.0; // Default threshold
    }
    
    /**
     * Update wallet balance directly
     */
    public void updateBalance(double newBalance) {
        this.balance = newBalance;
    }

    public boolean isId(String id) {
        return this.id.equals(id);
    }

    public boolean removeTransaction(String id) {
        Iterator<Transaction> iterator = this.transactions.iterator();
        while (iterator.hasNext()) {
            Transaction trans = iterator.next();
            if(trans.getTId().equals(id)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public String getInfo(){
        return this.id + "\t" + this.name + "\t" + this.balance;
    }

    public String getInfoAllTrans(){
        return this.transactions.stream().map(t -> t.toString()).collect(Collectors.joining("\n"));
    }

    @Override
    public String toString(){
        return "Id: " + id + "\nName: " + name + "\nBalance: " + balance;
    }

    // Explicit getter methods to fix Lombok compilation issues
    public String getId() { return id; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }
    public User getOwner() { return owner; }
    public java.time.LocalDateTime getCreationDate() { return creationDate; }
    
    // Explicit setter methods to fix Lombok compilation issues
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBalance(double balance) { this.balance = balance; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public void setOwner(User owner) { this.owner = owner; }
    public void setCreationDate(java.time.LocalDateTime creationDate) { this.creationDate = creationDate; }
}
