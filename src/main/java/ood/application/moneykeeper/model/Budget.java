package ood.application.moneykeeper.model;

import lombok.Data;
import ood.application.moneykeeper.utils.DateTimeUtils;
import ood.application.moneykeeper.utils.UUIDUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private List<Transaction> transactions = new ArrayList<>();

    public Budget(String name, double limit, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.limit = limit;
        this.spent = 0.0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
    }

    public Budget(String id, String name, double limit, double spent, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        this.id = id;
        this.name = name;
        this.limit = limit;
        this.spent = spent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.observers = new ArrayList<>();
        this.transactions = new ArrayList<>();
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

    public boolean isOverLimit() {
        return this.spent > this.limit;
    }

    public String getInfo() {
        return this.name + "\n" + this.limit + "\n" + this.spent + "\n" + this.startDate
                + "\n" + this.endDate + "\n" + this.category.getName();
    }

    public void addTransaction(Transaction trans) {
        this.transactions.add(trans);
        processTrans(trans);
    }

    public void processTrans(Transaction trans) {
        spent += trans.getAmount();
    }

    public String toString() {
        return "Id: " + id + "\nName: " + name + "\nLimit: " + limit + "\nSpent: "
                + spent + "\nStart: " + DateTimeUtils.formatDefault(startDate) + "\nEnd: "
                + DateTimeUtils.formatDefault(endDate) + "\nCategory: " + category.getName();
    }
}
