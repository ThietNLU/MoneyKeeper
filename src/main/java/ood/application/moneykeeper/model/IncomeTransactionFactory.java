package ood.application.moneykeeper.model;

public class IncomeTransactionFactory implements ITransactionFactory {
    @Override
    public ITransactionStrategy createTransaction() {
        return new IncomeTransactionStrategy();
    }

}
