package ood.application.moneykeeper.model;

public class ExpenseTransactionStrategy implements ITransactionStrategy {

    @Override
    public void processWallet(Wallet wallet, double amount) {
        wallet.setBalance(wallet.getBalance() - amount);
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
