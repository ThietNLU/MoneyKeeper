package ood.application.moneykeeper.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ood.application.moneykeeper.utils.UUIDUtils;

@Data
@AllArgsConstructor
public class Category {
    private String id;
    private String name;
    private boolean isExpense;    public Category(String name, boolean isExpense) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.isExpense = isExpense;
    }// Explicit getter methods to fix Lombok compilation issues
    public String getId() { return id; }
    public String getName() { return name; }
    public boolean isExpense() { return isExpense; }

    // Explicit setter methods to fix Lombok compilation issues
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setExpense(boolean isExpense) { this.isExpense = isExpense; }

    public String toString() {
        return "id: " + id + ", name: " + name + ", isExpense: " + isExpense;
    }

}