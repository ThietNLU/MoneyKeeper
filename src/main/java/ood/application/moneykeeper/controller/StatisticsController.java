package ood.application.moneykeeper.controller;
import ood.application.moneykeeper.model.ATransaction;
import ood.application.moneykeeper.model.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class StatisticsController {
    private Map<Category, Double> generateExpenseReportByCategory(LocalDateTime start, LocalDateTime end) {
        Map<Category, Double> categoryTotals = new HashMap<>();

//        for (Wallet wallet : WalletManager.getWallets().subList()) {
//            for (ATransaction tx : wallet.getTransactions()) {
//                if (tx instanceof ExpenseTransaction) {
//                    LocalDateTime txDateTime = tx.getDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//
//                    if (!txDateTime.isBefore(start) && !txDateTime.isAfter(end)) {
//                        Category category = tx.getCategory();
//                        double amount = tx.getAmount();
//                        categoryTotals.merge(category, amount, Double::sum);
//                    }
//                }
//            }
//        }

        return categoryTotals;
    }


}
