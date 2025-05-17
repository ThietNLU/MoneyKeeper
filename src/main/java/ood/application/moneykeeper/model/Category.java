package ood.application.moneykeeper.model;

import lombok.Data;

@Data
public class Category {
    private String id;
    private String name;

    public Category(String name) {
        this.name = name;
    }

    public boolean equals(Category category) {
        return this.name.equalsIgnoreCase(category.getName());
    }

    public String toString() {
        return name;
    }

}