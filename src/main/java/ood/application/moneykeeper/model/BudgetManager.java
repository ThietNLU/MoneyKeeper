package ood.application.moneykeeper.model;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class BudgetManager {
    private List<Budget> budgets;

    public BudgetManager() {
        this.budgets = new ArrayList<>();
    }

    public BudgetManager(List<Budget> budgets) {
        this.budgets = budgets;
    }

    public void addBudget(Budget budget) {
        this.budgets.add(budget);
    }

    public void removeBudget(Budget budget) {
        this.budgets.remove(budget);
    }

    public List<Budget> getBudgetsWithCategory(Category category) {
        return this.budgets.stream()
                .filter(budget -> budget.getCategory().equals(category))
                .toList();
    }

    public String getAllInfo(){
        return this.budgets.stream().map(budget -> budget.getInfo()).collect(Collectors.joining("\n"));
    }

}
