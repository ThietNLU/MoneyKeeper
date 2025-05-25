package ood.application.moneykeeper.report;

import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Transaction;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class BudgetReport extends Report {
    public BudgetReport() throws SQLException {
        super();
    }

    @Override
    protected ReportData processData(List<Transaction> transactions) throws SQLException {
        Map<String, Double> budgetStatus = new HashMap<>();
        Map<String, Double> budgetLimits = new HashMap<>();

        List<Budget> activeBudgets = budgetDAO.getActiveBudgets();

        // Group by category for easier processing
        Map<String, List<Transaction>> transactionsByCategory = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getId()));

        // Calculate budget usage
        for (Budget budget : activeBudgets) {
            String budgetName = budget.getName();
            double limit = budget.getLimit();
            double spent = budget.getSpent();

            budgetLimits.put(budgetName, limit);
            budgetStatus.put(budgetName, spent);
        }

        return new ReportDataBuilder()
                .withTitle("Budget Report")
                .withPeriod(timeStrategy.calculateReportPeriod())
                .withBudgetStatus(budgetStatus)
                .withBudgetLimits(budgetLimits)
                .build();
    }
}