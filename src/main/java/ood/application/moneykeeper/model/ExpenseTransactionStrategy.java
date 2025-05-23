package ood.application.moneykeeper.model;

import java.time.LocalDateTime;
import java.util.List;

public class ExpenseTransactionStrategy implements ITransactionStrategy {
    @Override
    public void process(Wallet wallet, double amount, LocalDateTime dateTime, boolean isExpense, Category category) {
        wallet.setBalance(wallet.getBalance() - amount);

//        if (isExpense) {
//            User owner = wallet.getOwner();
//            List<Budget> budgets = owner.getBudgetsWithCategory(category);
//            for (Budget budget : budgets) {
//                if (dateTime.isAfter(budget.getStartDate()) && dateTime.isBefore(budget.getEndDate())) {
//                    budget.addExpense(amount);
//                    if (budget.isOverLimit()) {
//
//                    }
//                }
//            }
//        }
    }
}
