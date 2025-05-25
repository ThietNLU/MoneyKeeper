package ood.application.moneykeeper.model;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ood.application.moneykeeper.utils.UUIDUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wallet implements ISubject {
    private String id;
    private String name;
    private double balance;
    private List<Transaction> transactions;
    private User owner;
    private List<IObserver> observers = new ArrayList<>();
    private java.time.LocalDateTime creationDate;

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
        notifyBalanceChanged(oldBalance, this.balance, "income");
    }

    public void expense(double amount) {
        double oldBalance = this.balance;
        this.balance -= amount;
        notifyBalanceChanged(oldBalance, this.balance, "expense");
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        transaction.processWallet();
        
        // Notify about transaction addition
        NotificationData transactionNotification = new NotificationData(
            NotificationType.TRANSACTION_ADDED,
            String.format("Transaction added to wallet '%s': %.2f", 
                         name, transaction.getAmount()),
            "Wallet",
            transaction
        );
        notifyObservers(transactionNotification);
    }

    public String printTransactions() {
        return this.transactions.stream()
                .map(t -> t.toString()).collect(Collectors.joining("\n===========*===========\n"));
    }

    public int countTransactions() {
        return this.transactions.size();
    }

    @Override
    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        this.observers.remove(observer);
    }    @Override
    public void notifyObservers(String message) {
        for (IObserver observer : observers) {
            observer.update(message);
        }
    }

    @Override
    public void notifyObservers(NotificationData notificationData) {
        for (IObserver observer : observers) {
            observer.update(notificationData);
        }
    }
    
    /**
     * Notify observers about balance changes
     */
    private void notifyBalanceChanged(double oldBalance, double newBalance, String type) {
        // General balance update notification
        NotificationData balanceNotification = new NotificationData(
            NotificationType.WALLET_UPDATE,
            String.format("Wallet '%s' balance changed from %.2f to %.2f (%s)", 
                         name, oldBalance, newBalance, type),
            "Wallet",
            this
        );
        notifyObservers(balanceNotification);
        
        // Check for low balance
        if (isLowBalance() && !wasLowBalance(oldBalance)) {
            NotificationData lowBalanceNotification = new NotificationData(
                NotificationType.LOW_BALANCE,
                String.format("Wallet '%s' has low balance: %.2f", name, newBalance),
                "Wallet",
                this
            );
            notifyObservers(lowBalanceNotification);
        }
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
     * Update wallet balance directly and notify observers
     */
    public void updateBalance(double newBalance) {
        double oldBalance = this.balance;
        this.balance = newBalance;
        notifyBalanceChanged(oldBalance, newBalance, "update");
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

    public String toString(){
        return "Id: " + id + "\nName: " + name + "\nBalance: " + balance;
    }
}
