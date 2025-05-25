package ood.application.moneykeeper.report;

import ood.application.moneykeeper.model.Transaction;

import java.sql.SQLException;
import java.util.*;

public class CategoryReport extends Report {
    public CategoryReport() throws SQLException {
        super();
    }

    @Override
    protected ReportData processData(List<Transaction> transactions) throws SQLException {
        Map<String, Double> categoryExpenses = new HashMap<>();
        Map<String, Double> categoryIncome = new HashMap<>();

        for (Transaction transaction : transactions) {
            String categoryName = transaction.getCategory().getName();
            double amount = transaction.getAmount();

            if (transaction.isExpense()) {
                categoryExpenses.put(categoryName,
                        categoryExpenses.getOrDefault(categoryName, 0.0) + amount);
            } else {
                categoryIncome.put(categoryName,
                        categoryIncome.getOrDefault(categoryName, 0.0) + amount);
            }
        }

        return new ReportDataBuilder()
                .withTitle("Category Report")
                .withPeriod(timeStrategy.calculateReportPeriod())
                .withExpensesByCategory(categoryExpenses)
                .withIncomeByCategory(categoryIncome)
                .build();
    }
}