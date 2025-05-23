package ood.application.moneykeeper.main;

import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.model.User;

import java.time.LocalDateTime;

public class MainTest {
    public static void main(String[] args) {
        User user = new User("Thirvo");
        user.createWallet("Cash", 500000.0);
        user.createWallet("Vietel Money", 200000.0);
        user.createWallet("Momo", 1500000.0);

        user.printWalletsInfo();

        user.createBudget("Launch", 200000, LocalDateTime.now(),
                LocalDateTime.now(), new Category("Banh mi", true));

        System.out.println("====*====");
        user.printBudgetsInfo();
    }
}
