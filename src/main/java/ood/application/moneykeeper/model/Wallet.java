package ood.application.moneykeeper.model;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class Wallet implements ISubject {
    private String wId;
    private String name;
    private double balance;
    private List<ATransaction> transactions;
    private User owner;
    private List<IObserver> observers = new ArrayList<>();

    public Wallet(String name, double balance, User owner) {
        this.name = name;
        this.balance = balance;
        this.transactions = new ArrayList<>();
        this.owner = owner;
        owner.addWallet(this);
    }

    public void income(double amount) {
        this.balance += amount;
    }

    public void expense(double amount) {
        this.balance -= amount;
    }

    public void addTransaction(ATransaction transaction) {
        this.transactions.add(transaction);
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
    }

    @Override
    public void notifyObservers(String message) {
        for (IObserver observer : observers) {
            observer.update(message);
        }
    }
}
