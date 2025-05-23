package ood.application.moneykeeper.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Budget implements ISubject {
    private String id;
    private String name;
    private double limit;
    private double spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Category category;
    private List<IObserver> observers = new ArrayList<>();
    private List<ATransaction> transactions = new ArrayList<>();

    public Budget(String name, double limit, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.limit = limit;
        this.spent = 0.0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
    }

    public void addExpense(double amount) {
        this.spent += amount;
    }

    public boolean isOverLimit() {
        return this.spent > this.limit;
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

    public String getInfo(){
        return this.name + "\t"+ this.limit + "\t" + this.spent + "\t" + this.startDate
                + "\t" + this.endDate + "\t" + this.category.getName();
    }

    public void addTransaction(ATransaction trans) {
        spent += trans.getAmount();
        this.transactions.add(trans);
    }
}
