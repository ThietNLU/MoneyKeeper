package ood.application.moneykeeper.controller;
import ood.application.moneykeeper.model.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
public class HistoryController {
    public List<Transaction> getTransactionsByTimeRange(Wallet wallet, LocalDateTime from, LocalDateTime to) {
        return wallet.getTransactions().stream()
                .filter(t -> !t.getDateTime().isBefore(from) && !t.getDateTime().isAfter(to))
                .collect(Collectors.toList());
    }

//    public List<Transaction> getTransactionsByType(Wallet wallet, boolean isExpense) {
//        return wallet.getTransactions().stream()
//                .filter(t -> isExpense ? t.set : t instanceof IncomeTransaction)
//                .collect(Collectors.toList());
//    }

    public List<Transaction> getTransactionsByCategory(Wallet wallet, Category category) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
    }
}
