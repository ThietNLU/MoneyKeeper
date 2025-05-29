package ood.application.moneykeeper.model;

public class ExpenseTransactionStrategy implements ITransactionStrategy {    @Override
    public void processWallet(Wallet wallet, double amount) {
        wallet.expense(amount);
    }

    @Override
    public void processBudget(BudgetManager budgets, Transaction trans) {
        budgets.processTrans(trans);
    }

    @Override
    public boolean isExpense() {
        return true;
    }
}
