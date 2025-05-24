package ood.application.moneykeeper.model;

import java.time.LocalDateTime;

public class IncomeTransaction extends ATransaction {

    public IncomeTransaction(Wallet wallet, double amount, Category category, String description) {
        super(wallet, amount, category, description);
    }

    public IncomeTransaction(String id, Wallet wallet, double amount, Category category, String description) {
        super(wallet, amount, category, description);
        this.tId = id;
    }

    public IncomeTransaction(String id, Wallet wallet, double amount, LocalDateTime dateTime, Category category, String description) {
        super(id, wallet, amount, dateTime, category, description);
    }

    public String toString() {
        return "\ttype: Income," + "\n\tid: " + tId + ",\n\tamount: +" + amount + ",\n\ttime: " + dateTime
                + ",\n\tcategory: " + category + ",\n\tdescription: " + description;
    }


    @Override
    public boolean isExpense() {
        return false;
    }

}
