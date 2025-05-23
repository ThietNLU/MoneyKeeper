package ood.application.moneykeeper.model;

import java.util.*;

public class ExpenseTransaction extends ATransaction {

    public ExpenseTransaction(Wallet wallet, double amount, Category category, String description) {
        super(wallet, amount, category, description);
    }

    @Override
    public boolean isExpense() {
        return true;
    }

    @Override
    public String toString() {
        return "\ttype: Expense," + "\n\tid: " + tId + ",\n\tamount: -" + amount + ",\n\ttime: " + dateTime
                + ",\n\tcategory: " + category + ",\n\tdescription: " + description;
    }

}
