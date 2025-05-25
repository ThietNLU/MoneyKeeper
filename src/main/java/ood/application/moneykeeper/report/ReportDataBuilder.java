package ood.application.moneykeeper.report;

import java.time.LocalDateTime;
import java.util.Map;

public class ReportDataBuilder {
    private ReportData reportData;

    public ReportDataBuilder() {
        reportData = new ReportData();
    }

    public ReportDataBuilder withTitle(String title) {
        reportData.setTitle(title);
        return this;
    }

    public ReportDataBuilder withPeriod(LocalDateTime[] period) {
        reportData.setStartDate(period[0]);
        reportData.setEndDate(period[1]);
        return this;
    }

    public ReportDataBuilder withExpensesByCategory(Map<String, Double> expenses) {
        reportData.setExpensesByCategory(expenses);
        return this;
    }

    public ReportDataBuilder withIncomeByCategory(Map<String, Double> income) {
        reportData.setIncomeByCategory(income);
        return this;
    }

    public ReportDataBuilder withBudgetStatus(Map<String, Double> budgetStatus) {
        reportData.setBudgetStatus(budgetStatus);
        return this;
    }

    public ReportDataBuilder withBudgetLimits(Map<String, Double> budgetLimits) {
        reportData.setBudgetLimits(budgetLimits);
        return this;
    }

    public ReportData build() {
        return reportData;
    }
}