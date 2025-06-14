package ood.application.moneykeeper.model;

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class WalletManager {
    private List<Wallet> wallets;

    public WalletManager() {
        this.wallets = new ArrayList<>();
    }

    public WalletManager(List<Wallet> wallets) {
        this.wallets = wallets;
    }    public void addWallet(Wallet wallet) {
        this.wallets.add(wallet);
    }

    public boolean removeWallet(String id) {
        Iterator<Wallet> iterator = this.wallets.iterator();
        while (iterator.hasNext()) {
            Wallet wallet = iterator.next();            if (wallet.getId().equals(id)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public Wallet getWallet(String id) {
        return this.wallets.stream().filter(w -> w.isId(id)).findFirst().orElse(null);
    }

    public String getAllInfo(){
        return this.wallets.stream().map(Wallet::getInfo).collect(Collectors.joining("\n"));
    }
}
