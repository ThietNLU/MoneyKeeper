package ood.application.moneykeeper.model;

import java.time.LocalDateTime;
import java.util.List;

public class IncomeTransactionStrategy implements ITransactionStrategy{
    @Override
    public void process(Wallet wallet, double amount, LocalDateTime dateTime, boolean isExpense, Category category) {
        wallet.setBalance(wallet.getBalance() + amount);
    }
}
