package ood.application.moneykeeper.model;

public class IncomeTransactionFactory implements ITransactionFactory {
    @Override
    public ATransaction createTransaction(Wallet wallet, double amount, Category category, String description) {
        return new IncomeTransaction(wallet, amount, category, description);
    }

}
