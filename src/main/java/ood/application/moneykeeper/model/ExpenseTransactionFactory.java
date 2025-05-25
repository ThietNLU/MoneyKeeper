package ood.application.moneykeeper.model;

public class ExpenseTransactionFactory implements ITransactionFactory {
    @Override
    public ITransactionStrategy createTransaction() {
        return new ExpenseTransactionStrategy();
    }

}
