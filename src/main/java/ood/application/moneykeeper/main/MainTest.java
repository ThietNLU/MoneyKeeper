package ood.application.moneykeeper.main;

import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.model.User;
import ood.application.moneykeeper.model.Wallet;
import ood.application.moneykeeper.model.WalletManager;

import java.time.LocalDateTime;

public class MainTest {
    public static void main(String[] args) {
        User user = new User("Thirvo");
        user.createWallet("Cash", 500000.0);
        user.createWallet("Vietel Money", 200000.0);
        user.createWallet("Momo", 1500000.0);

        user.printWalletsInfo();

        user.createBudget("Launch", 200000, LocalDateTime.now(),
                LocalDateTime.now(), new Category("Banh", true));

        System.out.println("====*====");
        user.printBudgetsInfo();

        Wallet wallet = user.getWallets().getWallets().get(0);
        user.createTransaction(15000.0, new Category("Banh", true), "Banh mi hai trung", wallet);

        System.out.println("====*====");
        System.out.println(wallet.getInfoAllTrans());
        System.out.println("====*====");
        user.printBudgetsInfo();
    }
}
