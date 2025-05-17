package ood.application.moneykeeper.model;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class Wallet extends ASubject {
    private String wId;
    private String name;
    private double balance;
    private List<ATransaction> transactions;
    private User owner;

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

}
