package ood.application.moneykeeper.report;

import java.time.LocalDateTime;
import java.util.*;

public class ReportData {
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Map<String, Double> expensesByCategory;
    private Map<String, Double> incomeByCategory;
    private Map<String, Double> budgetStatus;
    private Map<String, Double> budgetLimits;

    // Getters
    public String getTitle() { return title; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public Map<String, Double> getExpensesByCategory() { return expensesByCategory; }
    public Map<String, Double> getIncomeByCategory() { return incomeByCategory; }
    public Map<String, Double> getBudgetStatus() { return budgetStatus; }
    public Map<String, Double> getBudgetLimits() { return budgetLimits; }

    // Package private constructor for builder
    ReportData() {}

    void setTitle(String title) { this.title = title; }
    void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    void setExpensesByCategory(Map<String, Double> expensesByCategory) {
        this.expensesByCategory = expensesByCategory;
    }
    void setIncomeByCategory(Map<String, Double> incomeByCategory) {
        this.incomeByCategory = incomeByCategory;
    }
    void setBudgetStatus(Map<String, Double> budgetStatus) {
        this.budgetStatus = budgetStatus;
    }
    void setBudgetLimits(Map<String, Double> budgetLimits) {
        this.budgetLimits = budgetLimits;
    }
}
