package ood.application.moneykeeper.model;

public class IncomeTransactionStrategy implements ITransactionStrategy {    @Override
    public void processWallet(Wallet wallet, double amount) {
        wallet.income(amount);
    }

    @Override
    public void processBudget(BudgetManager budgets, Transaction trans) {

    }

    @Override
    public boolean isExpense() {
        return false;
    }
}
