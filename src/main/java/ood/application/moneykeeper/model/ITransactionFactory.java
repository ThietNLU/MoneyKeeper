package ood.application.moneykeeper.model;

public interface ITransactionFactory {
    public ATransaction createTransaction(Wallet wallet, double amount, Category category, String description);
}
