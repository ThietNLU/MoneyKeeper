package ood.application.moneykeeper.model;

public interface ITransactionFactory {
    public ITransactionStrategy createTransaction();
}
