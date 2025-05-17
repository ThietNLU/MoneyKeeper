package ood.application.moneykeeper.model;

public class IncomeTransaction extends ATransaction {

    public IncomeTransaction(Wallet wallet, double amount, Category category, String description) {
        super(wallet, amount, category, description);
    }

    public String toString() {
        return "\ttype: income," + "\n\tid: " + tId + ",\n\tamount: +" + amount + ",\n\ttime: " + dateTime
                + ",\n\tcategory: " + category + ",\n\tdescription: " + description;
    }

    @Override
    public void processTrans() {
        wallet.setBalance(wallet.getBalance() + amount);
    }

    @Override
    public boolean isExpense() {
        return false;
    }

}
