package ood.application.moneykeeper.model;

public interface ITransactionStrategy {
    public void processWallet(Wallet wallet, double amount);

    public void processBudget(BudgetManager budgets, Transaction trans);

    public boolean isExpense();
}
