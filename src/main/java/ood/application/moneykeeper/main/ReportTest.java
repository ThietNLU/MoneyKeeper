package ood.application.moneykeeper.main;

import ood.application.moneykeeper.dao.*;
import ood.application.moneykeeper.model.*;
import ood.application.moneykeeper.report.*;
import ood.application.moneykeeper.utils.DateTimeUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ReportTest {

    public static void main(String[] args) {
        try {
            // Initialize DAOs
            BudgetDAO budgetDAO = new BudgetDAO();
            CategoryDAO categoryDAO = new CategoryDAO();
            UserDAO userDAO = new UserDAO();
            WalletDAO walletDAO = new WalletDAO();
            TransactionDAO transactionDAO = new TransactionDAO();

            // Create test user
            User testUser = new User("Report Test User");
            userDAO.save(testUser);
            System.out.println("Created user: " + testUser.getName());

            // Create wallet
            Wallet testWallet = new Wallet("Report Test Wallet", 10000.0, testUser);
            walletDAO.save(testWallet);
            System.out.println("Created wallet: " + testWallet.getName() + " with balance: " + testWallet.getBalance());

            // Create categories
            Category foodCategory = new Category("Food", true);
            Category transportCategory = new Category("Transport", true);
            Category entertainmentCategory = new Category("Entertainment", true);
            Category salaryCategory = new Category("Salary", false);

            categoryDAO.save(foodCategory);
            categoryDAO.save(transportCategory);
            categoryDAO.save(entertainmentCategory);
            categoryDAO.save(salaryCategory);

            System.out.println("Created categories for reporting");

            // Create budgets
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime monthEnd = now.plusMonths(1);

            Budget foodBudget = new Budget(
                    "Monthly Food",
                    1000.0,
                    now,
                    monthEnd,
                    foodCategory
            );

            Budget transportBudget = new Budget(
                    "Monthly Transport",
                    500.0,
                    now,
                    monthEnd,
                    transportCategory
            );

            Budget entertainmentBudget = new Budget(
                    "Monthly Entertainment",
                    300.0,
                    now,
                    monthEnd,
                    entertainmentCategory
            );

            // Save budgets to database
            budgetDAO.save(foodBudget);
            budgetDAO.save(transportBudget);
            budgetDAO.save(entertainmentBudget);

            System.out.println("Saved budgets to database");

            // Create transactions for the past month to test reports
            // Food transactions
            createTransaction(transactionDAO, testWallet, 150.0, foodCategory, "Grocery shopping", now.minusDays(1), true);
            createTransaction(transactionDAO, testWallet, 80.0, foodCategory, "Restaurant dinner", now.minusDays(2), true);
            createTransaction(transactionDAO, testWallet, 45.0, foodCategory, "Lunch", now.minusDays(3), true);

            // Transport transactions
            createTransaction(transactionDAO, testWallet, 200.0, transportCategory, "Monthly bus pass", now.minusDays(3), true);
            createTransaction(transactionDAO, testWallet, 50.0, transportCategory, "Taxi ride", now.minusDays(5), true);

            // Entertainment transactions
            createTransaction(transactionDAO, testWallet, 150.0, entertainmentCategory, "Movie tickets", now.minusDays(4), true);
            createTransaction(transactionDAO, testWallet, 120.0, entertainmentCategory, "Dinner with colleagues", now.minusDays(5), true);

            // Income transaction
            createTransaction(transactionDAO, testWallet, 5000.0, salaryCategory, "Monthly salary", now.minusDays(7), false);

            System.out.println("Created transactions for reports");

            // Update wallet with the new balance after transactions
            walletDAO.update(testWallet);

            // Process budget transactions
            processBudgetTransactions(budgetDAO, transactionDAO, foodBudget, foodCategory);
            processBudgetTransactions(budgetDAO, transactionDAO, transportBudget, transportCategory);
            processBudgetTransactions(budgetDAO, transactionDAO, entertainmentBudget, entertainmentCategory);

            // Generate Monthly Report
            System.out.println("\n===== MONTHLY REPORT =====");
            generateMonthlyReport();

            // Generate Weekly Report
            System.out.println("\n===== WEEKLY REPORT =====");
            generateWeeklyReport();

            // Generate Custom Period Report (last 10 days)
            System.out.println("\n===== CUSTOM PERIOD REPORT (Last 10 Days) =====");
            generateCustomReport(now.minusDays(10), now);

            // Generate Budget Report
            System.out.println("\n===== BUDGET REPORT =====");
            generateBudgetReport();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTransaction(TransactionDAO transactionDAO, Wallet wallet,
                                          double amount, Category category, String description,
                                          LocalDateTime dateTime, boolean isExpense) throws SQLException {

        Transaction transaction = new Transaction(wallet, amount, category, description);
        transaction.setDateTime(dateTime);

        if (isExpense) {
            transaction.setStrategy(new ExpenseTransactionStrategy());
        } else {
            transaction.setStrategy(new IncomTransactionStrategy());
        }

        transactionDAO.save(transaction);
        transaction.processWallet();
    }

    private static void processBudgetTransactions(BudgetDAO budgetDAO, TransactionDAO transactionDAO,
                                                  Budget budget, Category category) throws SQLException {

        List<Transaction> categoryTransactions = transactionDAO.getTransactionsByCategory(category.getId());

        for (Transaction transaction : categoryTransactions) {
            if (transaction.isExpense() && isInPeriod(transaction.getDateTime(), budget.getStartDate(), budget.getEndDate())) {
                budget.addTransaction(transaction);
            }
        }

        budgetDAO.update(budget);
    }

    private static boolean isInPeriod(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return (dateTime.isEqual(start) || dateTime.isAfter(start)) &&
                (dateTime.isEqual(end) || dateTime.isBefore(end));
    }

    private static void generateMonthlyReport() throws SQLException {
        Report categoryReport = ReportFactory.createReport(ReportFactory.ReportType.CATEGORY);
        categoryReport.setTimeStrategy(new MonthlyTimeStrategy());
        ReportData reportData = categoryReport.generateReport();

        printReportData(reportData, "Monthly Category Report");
    }

    private static void generateWeeklyReport() throws SQLException {
        Report categoryReport = ReportFactory.createReport(ReportFactory.ReportType.CATEGORY);
        categoryReport.setTimeStrategy(new WeeklyTimeStrategy());
        ReportData reportData = categoryReport.generateReport();

        printReportData(reportData, "Weekly Category Report");
    }

    private static void generateCustomReport(LocalDateTime start, LocalDateTime end) throws SQLException {
        Report categoryReport = ReportFactory.createReport(ReportFactory.ReportType.CATEGORY);
        categoryReport.setTimeStrategy(new CustomTimeStrategy(start, end));
        ReportData reportData = categoryReport.generateReport();

        printReportData(reportData, "Custom Period Category Report");
    }

    private static void generateBudgetReport() throws SQLException {
        Report budgetReport = ReportFactory.createReport(ReportFactory.ReportType.BUDGET);
        budgetReport.setTimeStrategy(new MonthlyTimeStrategy());
        ReportData reportData = budgetReport.generateReport();

        System.out.println("Budget Report Period: " +
                DateTimeUtils.formatDefault(reportData.getStartDate()) + " to " +
                DateTimeUtils.formatDefault(reportData.getEndDate()));

        System.out.println("\nBudget Status:");
        Map<String, Double> budgetStatus = reportData.getBudgetStatus();
        Map<String, Double> budgetLimits = reportData.getBudgetLimits();

        for (Map.Entry<String, Double> entry : budgetStatus.entrySet()) {
            String budgetName = entry.getKey();
            Double spent = entry.getValue();
            Double limit = budgetLimits.get(budgetName);

            double percentage = limit > 0 ? (spent / limit) * 100 : 0;
            System.out.println(budgetName + ": " + spent + " / " + limit +
                    " (" + String.format("%.2f", percentage) + "%)");
        }
    }

    private static void printReportData(ReportData reportData, String title) {
        System.out.println(title + " Period: " +
                DateTimeUtils.formatDefault(reportData.getStartDate()) + " to " +
                DateTimeUtils.formatDefault(reportData.getEndDate()));

        System.out.println("\nExpenses by Category:");
        Map<String, Double> expenses = reportData.getExpensesByCategory();
        if (expenses != null) {
            for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }

        System.out.println("\nIncome by Category:");
        Map<String, Double> income = reportData.getIncomeByCategory();
        if (income != null) {
            for (Map.Entry<String, Double> entry : income.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }

        // Calculate totals
        double totalExpense = expenses != null ?
                expenses.values().stream().mapToDouble(Double::doubleValue).sum() : 0;
        double totalIncome = income != null ?
                income.values().stream().mapToDouble(Double::doubleValue).sum() : 0;

        System.out.println("\nSummary:");
        System.out.println("Total Income: " + totalIncome);
        System.out.println("Total Expenses: " + totalExpense);
        System.out.println("Net: " + (totalIncome - totalExpense));
    }
}