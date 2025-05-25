package ood.application.moneykeeper.controller;
import ood.application.moneykeeper.model.ATransaction;
import ood.application.moneykeeper.model.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;

public class StatisticsController implements IStatisticsController{
    private final WalletManager walletManager;

    public StatisticsController(WalletManager walletManager) {
        this.walletManager = walletManager;
    }

    @Override
    public Map<Category, Double> generateExpenseReportByCategory(LocalDateTime start, LocalDateTime end) {
        Map<Category, Double> categoryTotals = new EnumMap<>(Category.class);

        for (Wallet wallet : walletManager.getWallets().subList()) {
            for (ATransaction tx : wallet.getTransactions()) {
                if (tx instanceof ExpenseTransaction) {
                    LocalDateTime txDateTime = tx.getDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

                    if (!txDateTime.isBefore(start) && !txDateTime.isAfter(end)) {
                        Category category = tx.getCategory();
                        double amount = tx.getAmount();
                        categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
                    }
                }
            }
        }

        return categoryTotals;
    }

}
