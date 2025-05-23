package ood.application.moneykeeper.controller;
import ood.application.moneykeeper.model.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
public class HistoryController {
    public List<ATransaction> getTransactionsByTimeRange(Wallet wallet, LocalDateTime from, LocalDateTime to) {
        return wallet.getTransactions().stream()
                .filter(t -> !t.getDateTime().isBefore(from) && !t.getDateTime().isAfter(to))
                .collect(Collectors.toList());
    }

    public List<ATransaction> getTransactionsByType(Wallet wallet, boolean isExpense) {
        return wallet.getTransactions().stream()
                .filter(t -> isExpense ? t instanceof ExpenseTransaction : t instanceof IncomeTransaction)
                .collect(Collectors.toList());
    }

    public List<ATransaction> getTransactionsByCategory(Wallet wallet, Category category) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
}
