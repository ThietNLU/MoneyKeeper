package ood.application.moneykeeper.model;

public class ExpenseTransactionFactory implements ITransactionFactory {
    @Override
    public ATransaction createTransaction(Wallet wallet, double amount, Category category, String description) {
        return new ExpenseTransaction(wallet, amount, category, description);
    }

}
