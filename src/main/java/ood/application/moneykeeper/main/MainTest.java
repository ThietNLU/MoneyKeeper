package ood.application.moneykeeper.main;

import ood.application.moneykeeper.model.*;

import java.time.LocalDateTime;

public class MainTest {
    public static void main(String[] args) {
        User user = new User("Thirvo");
        Wallet cash = user.createWallet("Cash", 500000.0);
        System.out.println(cash.toString());
        System.out.println("====***====");

        Budget budget = user.createBudget(
                "Launch",
                200000.0,
                LocalDateTime.now(),
                LocalDateTime.parse("2025-06-27T00:00:00"),
                new Category("Launch", true)
        );

        Transaction trans = user.createTransaction(cash, 15000.0,
                new Category("Launch", true), "Bánh mì 2 trứng");
        System.out.println(trans.toString());
        System.out.println("====***====");
        System.out.println(budget.toString());
        System.out.println("====***====");
        System.out.println(cash.toString());

        User user1 = new User("John Doe");

        Wallet wallet = user.createWallet("Main Wallet", 1000000.0);

        Category foodCategory = new Category("Food", true);

        Transaction trans1 = user.createTransaction(wallet, 150000.0, foodCategory, "Lunch");


    }
}
