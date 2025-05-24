package ood.application.moneykeeper.model;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Wallet implements ISubject {
    private String id;
    private String name;
    private double balance;
    private List<ATransaction> transactions;
    private User owner;
    private List<IObserver> observers = new ArrayList<>();

    public Wallet(String name, double balance, User owner) {
        this.id = UUID.randomUUID().toString();
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

    public boolean isId(String id) {
        return this.id.equals(id);
    }

    public boolean removeTransaction(String id) {
        Iterator<ATransaction> iterator = this.transactions.iterator();
        while (iterator.hasNext()) {
            ATransaction trans = iterator.next();
            if(trans.isId(id)){
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
}
