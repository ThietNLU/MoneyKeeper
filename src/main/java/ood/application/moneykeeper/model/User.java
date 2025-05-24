package ood.application.moneykeeper.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String name;
    private WalletManager wallets;
    private BudgetManager budgets;

    public User(String name) {
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

    public void createWallet(String name, double balance) {
        this.wallets.addWallet(new Wallet(name, balance, this));
    }

    public void printWalletsInfo() {
        System.out.println(this.wallets.getAllInfo());
    }

    public void createBudget(String name, double limit, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        this.budgets.addBudget(new Budget(name, limit, startDate, endDate, category));
    }

    public void printBudgetsInfo() {
        System.out.println(this.budgets.getAllInfo());
    }

    public void createTransaction(double amount, Category category, String description, Wallet wallet) {
        ITransactionFactory transFactory;
        if (category.isExpense()){
            transFactory = new ExpenseTransactionFactory();
            ATransaction trans = transFactory.createTransaction(wallet, amount, category, description);
            wallet.addTransaction(trans);
            budgets.processTrans(trans, category);
        }
        else {
            transFactory = new IncomeTransactionFactory();
            ATransaction trans = transFactory.createTransaction(wallet, amount, category, description);
            wallet.addTransaction(trans);
        }
    }
}
