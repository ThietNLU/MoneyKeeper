package ood.application.moneykeeper.model;

import java.util.*;

public class WalletManager {
    private List<Wallet> wallets;

    private static WalletManager instance;

    public static WalletManager getInstance() {
        if (instance == null) {
            instance = new WalletManager();
        }
        return instance;
    }

    public WalletManager() {
        this.wallets = new ArrayList<>();
    }

    public WalletManager(List<Wallet> wallets) {
        this.wallets = wallets;
    }

    public void addWallet(Wallet wallet) {
        this.wallets.add(wallet);
    }

    public void removeWallet(Wallet wallet) {
        this.wallets.remove(wallet);
    }

    public List<Wallet> getWallets() {
        return wallets;
    }
}
