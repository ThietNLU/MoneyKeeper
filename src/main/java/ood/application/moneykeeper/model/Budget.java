package ood.application.moneykeeper.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Budget implements ISubject {
    private int id;
    private String name;
    private double limit;
    private double spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Category category;
    private List<IObserver> observers = new ArrayList<>();

    public Budget(String name, double limit, LocalDateTime startDate, LocalDateTime endDate, Category category) {
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
}
