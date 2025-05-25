package ood.application.moneykeeper.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ood.application.moneykeeper.utils.UUIDUtils;

@Data
@AllArgsConstructor
public class Category {
    private String id;
    private String name;
    private boolean isExpense;

    public Category(String name, boolean isExpense) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.isExpense = isExpense;
    }

    public boolean equals(Category category) {
        return this.name.equals(category.getName()) && this.isExpense == category.isExpense();
    }

    public boolean isExpense() {
        return isExpense;
    }

    public String toString() {
        return "id: " + id + ", name: " + name + ", isExpense: " + isExpense;
    }

}