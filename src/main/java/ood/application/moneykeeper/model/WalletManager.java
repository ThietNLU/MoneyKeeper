package ood.application.moneykeeper.model;

import java.util.*;

public class WalletManager {
    private List<Wallet> wallets;

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

}
