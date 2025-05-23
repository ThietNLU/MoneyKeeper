package ood.application.moneykeeper.model;

import java.util.*;

public class ExpenseTransaction extends ATransaction {

    public ExpenseTransaction(Wallet wallet, double amount, Category category, String description) {
        super(wallet, amount, category, description);
    }

    @Override
    public void processTrans() {
        setStrategy(new ExpenseTransactionStrategy());
        this.strategy.process(this.wallet, this.amount, this.dateTime, true, this.category);
    }

    @Override
    public boolean isExpense() {
        return true;
    }

    @Override
    public String toString() {
        return "\ttype: expense," + "\n\tid: " + tId + ",\n\tamount: -" + amount + ",\n\ttime: " + dateTime
                + ",\n\tcategory: " + category + ",\n\tdescription: " + description;
    }

}
