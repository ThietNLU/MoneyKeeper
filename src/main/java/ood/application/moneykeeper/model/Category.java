package ood.application.moneykeeper.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private String id;
    private String name;
    private boolean isExpense;

    public Category(String name, boolean isExpense) {
        this.name = name;
        this.isExpense = isExpense;
    }

    public boolean equals(Category category) {
        return this.name.equalsIgnoreCase(category.getName()) && this.isExpense == category.isExpense();
    }

    public String toString() {
        return name;
    }

    public boolean isExpense() {
        return isExpense;
    }

}