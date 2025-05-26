package ood.application.moneykeeper.model;

import java.time.LocalDateTime;

import lombok.*;
import ood.application.moneykeeper.utils.UUIDUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String name;
    private WalletManager wallets;
    private BudgetManager budgets;
    
    // Explicit getter methods to fix Lombok compilation issues
    public String getId() { return this.id; }
    public String getName() { return this.name; }
    public WalletManager getWallets() { return this.wallets; }
    public BudgetManager getBudgets() { return this.budgets; }
    
    // Explicit setter methods to fix Lombok compilation issues
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setWallets(WalletManager wallets) { this.wallets = wallets; }
    public void setBudgets(BudgetManager budgets) { this.budgets = budgets; }

    public User(String name) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.wallets = new WalletManager();
        this.budgets = new BudgetManager();
    }

    public User(String uid, String uname) {
        this.id = uid;
        this.name = uname;
    }

    public void addWallet(Wallet wallet) {
        this.wallets.addWallet(wallet);
    }

    public void removeWallet(String id) {
        this.wallets.removeWallet(id);
    }

    public Wallet createWallet(String name, double balance) {
        Wallet re = new Wallet(name, balance, this);
        this.wallets.addWallet(re);
        return re;
    }

    public void addBudget(Budget budget) {
        this.budgets.addBudget(budget);
    }

    public void removeBudget(String id) {
        this.budgets.removeBudget(id);
    }

    public Budget createBudget(String name, double limit, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        Budget re = new Budget(name, limit, startDate, endDate, category);
        this.budgets.addBudget(re);
        return re;
    }

    public void addTransaction(Transaction transaction) {
        transaction.processWallet();
        transaction.processBudget(budgets);
    }

    public Transaction createTransaction(Wallet wallet, double amount, Category category, String description) {
        Transaction re = new Transaction(wallet, amount, category, description);
        if (category.isExpense()) {
            re.setStrategy(new ExpenseTransactionStrategy());
        } else {
            re.setStrategy(new IncomTransactionStrategy());
        }
        re.processWallet();
        re.processBudget(budgets);
        return re;
    }
}
