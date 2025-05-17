package ood.application.moneykeeper.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Budget extends ASubject {
    private int id;
    private String name;
    private double limit;
    private double spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Category category;

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

}
