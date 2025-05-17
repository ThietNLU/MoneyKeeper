package ood.application.moneykeeper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.*;

@Data
@AllArgsConstructor
public class User {
    private String uId;
    private String uName;
    private Map<String, Wallet> wallets;
    private List<Budget> budgets;

    public User() {
        this.wallets = new HashMap<>();
        this.budgets = new ArrayList<>();
    }

    public User(String username) {
        this.uName = username;
        this.wallets = new HashMap<>();
        this.budgets = new ArrayList<>();
    }

    public User(String username, Map<String, Wallet> wallets, List<Budget> budgets) {
        this.uName = username;
        this.wallets = wallets;
        this.budgets = budgets;
    }

    public List<Budget> getBudgetsWithCategory(Category category) {
        return this.budgets.stream().filter(b -> b.getCategory().equals(category)).toList();
    }

    public Wallet getWalletById(String wId) {
        return this.wallets.get(wId) != null ? this.wallets.get(wId) : null;
    }

    public void addWallet(Wallet wallet) {
        this.wallets.put(wallet.getWId(), wallet);
    }

    public void addBudget(Budget budget) {
        this.budgets.add(budget);
    }

    public void removeWallet(String wId) {
        this.wallets.remove(wId);
    }

    public void removeBudget(Budget budget) {
        this.budgets.remove(budget);
    }
}
