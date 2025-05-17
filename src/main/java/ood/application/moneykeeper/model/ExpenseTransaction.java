package ood.application.moneykeeper.model;

import java.util.*;

public class ExpenseTransaction extends ATransaction {

    public ExpenseTransaction(Wallet wallet, double amount, Category category, String description) {
        super(wallet, amount, category, description);
    }

    @Override
    public void processTrans() {
        wallet.setBalance(wallet.getBalance() - amount);

        if (isExpense()) {
            User owner = wallet.getOwner();
            List<Budget> budgets = owner.getBudgetsWithCategory(category);
            for (Budget budget : budgets) {
                if (dateTime.isAfter(budget.getStartDate()) && dateTime.isBefore(budget.getEndDate())) {
                    budget.addExpense(amount);
                    if (budget.isOverLimit()) {

                    }
                }
            }
        }
    }

    @Override
    public boolean isExpense() {
        return true;
    }

    @Override
    public String toString() {
        return "\ttype: expense," + "\n\tid: " + tId + ",\n\tamount: -" + amount + ",\n\ttime: " + dateTime
                + ",\n\tcategory: " + category + ",\n\tdescription: " + description;
    }

}
