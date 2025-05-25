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

    public void removeBudget(String id) {
        Iterator<Budget> iterator = this.budgets.iterator();
        while (iterator.hasNext()) {
            Budget budget = iterator.next();
            if (budget.getId().equals(id)) {
                iterator.remove();
            }
        }
    }

    public List<Budget> getBudgetsWithCategory(Category category) {
        return this.budgets.stream()
                .filter(budget -> budget.getCategory().equals(category))
                .toList();
    }

    public String getAllInfo(){
        return this.budgets.stream().map(budget -> budget.getInfo()).collect(Collectors.joining("\n"));
    }

    public void processTrans(Transaction trans) {
        Category category = trans.getCategory();
        List<Budget> budgets = getBudgetsWithCategory(category);
        Iterator<Budget> iterator = budgets.iterator();
        while (iterator.hasNext()) {
            Budget budget = iterator.next();
            budget.addTransaction(trans);
        }
    }

}
