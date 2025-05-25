package ood.application.moneykeeper.report;

import ood.application.moneykeeper.dao.BudgetDAO;
import ood.application.moneykeeper.dao.CategoryDAO;
import ood.application.moneykeeper.dao.TransactionDAO;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.model.Transaction;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base class for reports using Template Method pattern.
 */
public abstract class Report {
    protected TransactionDAO transactionDAO;
    protected CategoryDAO categoryDAO;
    protected BudgetDAO budgetDAO;
    protected ReportTimeStrategy timeStrategy;

    public Report() throws SQLException {
        this.transactionDAO = new TransactionDAO();
        this.categoryDAO = new CategoryDAO();
        this.budgetDAO = new BudgetDAO();
    }

    // Template method
    public final ReportData generateReport() throws SQLException {
        LocalDateTime[] period = timeStrategy.calculateReportPeriod();
        List<Transaction> transactions = fetchTransactions(period[0], period[1]);
        return processData(transactions);
    }

    protected List<Transaction> fetchTransactions(LocalDateTime start, LocalDateTime end) throws SQLException {
        return transactionDAO.getTransactionsByDateRange(start, end);
    }

    // Different for each report type
    protected abstract ReportData processData(List<Transaction> transactions) throws SQLException;

    // Set the time strategy for the report
    public void setTimeStrategy(ReportTimeStrategy timeStrategy) {
        this.timeStrategy = timeStrategy;
    }
}