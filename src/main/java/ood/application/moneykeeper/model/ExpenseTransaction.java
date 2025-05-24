package ood.application.moneykeeper.model;

import java.time.LocalDateTime;
import java.util.*;

public class ExpenseTransaction extends ATransaction {

    public ExpenseTransaction(Wallet wallet, double amount, Category category, String description) {
        super(wallet, amount, category, description);
    }

    public ExpenseTransaction(String id, Wallet wallet, double amount, Category category, String description) {
        super(wallet, amount, category, description);
        this.tId = id;
    }

    public ExpenseTransaction(String id, Wallet wallet, double amount, LocalDateTime dateTime, Category category, String description) {
        super(id, wallet, amount, dateTime, category, description);
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
