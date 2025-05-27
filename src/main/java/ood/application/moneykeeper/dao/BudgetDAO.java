package ood.application.moneykeeper.dao;

import ood.application.moneykeeper.model.*;
import ood.application.moneykeeper.utils.DateTimeUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO implements DAO<Budget, String> {
    private final DBConnection db = DBConnection.getInstance();
    private final CategoryDAO categoryDAO;

    public BudgetDAO() throws SQLException {
        this.categoryDAO = new CategoryDAO();
    }

    @Override
    public List<Budget> getAll() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        try (Connection con = db.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Budget")) {
            while (rs.next()) {
                budgets.add(extractBudgetFromResultSet(rs));
            }
        }
        return budgets;
    }

    @Override
    public Budget get(String id) throws SQLException {
        Budget budget = null;
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Budget WHERE id = ?")) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    budget = extractBudgetFromResultSet(rs);
                    loadTransactionsForBudget(budget);
                }
            }
        }
        return budget;
    }

    @Override
    public boolean save(Budget budget) throws SQLException {
        String sql = "INSERT INTO Budget (id, name, category_id, limitAmount, startDate, endDate, spent) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, budget.getId());
            stmt.setString(2, budget.getName());
            stmt.setString(3, budget.getCategory().getId());
            stmt.setDouble(4, budget.getLimit());
            stmt.setString(5, DateTimeUtils.formatDefault(budget.getStartDate()));
            stmt.setString(6, DateTimeUtils.formatDefault(budget.getEndDate()));
            stmt.setDouble(7, budget.getSpent());
            int affected = stmt.executeUpdate();
            if (affected > 0 && budget.getTransactions() != null && !budget.getTransactions().isEmpty()) {
                saveBudgetTransactions(budget);
            }
            return affected > 0;
        }
    }

    @Override
    public boolean update(Budget budget) throws SQLException {
        String sql = "UPDATE Budget SET name = ?, category_id = ?, limitAmount = ?, startDate = ?, endDate = ?, spent = ? WHERE id = ?";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, budget.getName());
            stmt.setString(2, budget.getCategory().getId());
            stmt.setDouble(3, budget.getLimit());
            stmt.setString(4, DateTimeUtils.formatDefault(budget.getStartDate()));
            stmt.setString(5, DateTimeUtils.formatDefault(budget.getEndDate()));
            stmt.setDouble(6, budget.getSpent());
            stmt.setString(7, budget.getId());
            int affected = stmt.executeUpdate();
            if (affected > 0 && budget.getTransactions() != null) {
                clearBudgetTransactions(budget.getId());
                saveBudgetTransactions(budget);
            }
            return affected > 0;
        }
    }

    @Override
    public boolean delete(Budget budget) throws SQLException {
        return deleteById(budget.getId());
    }

    @Override
    public boolean deleteById(String id) throws SQLException {
        try (Connection con = db.getConnection()) {
            clearBudgetTransactions(id);
            try (PreparedStatement stmt = con.prepareStatement("DELETE FROM Budget WHERE id = ?")) {
                stmt.setString(1, id);
                int affected = stmt.executeUpdate();
                return affected > 0;
            }
        }
    }

    public List<Budget> getBudgetsByCategory(String categoryId) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM Budget WHERE category_id = ?";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    budgets.add(extractBudgetFromResultSet(rs));
                }
            }
        }
        return budgets;
    }

    public List<Budget> getActiveBudgets() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        String sql = "SELECT * FROM Budget WHERE startDate <= ? AND endDate >= ?";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, DateTimeUtils.formatDefault(now));
            stmt.setString(2, DateTimeUtils.formatDefault(now));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    budgets.add(extractBudgetFromResultSet(rs));
                }
            }
        }
        return budgets;
    }

    public List<Budget> getOverLimitBudgets() throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = "SELECT * FROM Budget WHERE spent > limitAmount";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                budgets.add(extractBudgetFromResultSet(rs));
            }
        }
        return budgets;
    }

    private Budget extractBudgetFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("name");
        double limitAmount = rs.getDouble("limitAmount");
        double spent = rs.getDouble("spent");
        String startDateStr = rs.getString("startDate");
        String endDateStr = rs.getString("endDate");
        String categoryId = rs.getString("category_id");
        Category category = categoryDAO.get(categoryId);
        LocalDateTime startDate = DateTimeUtils.parse(startDateStr, DateTimeUtils.DEFAULT_DATE_TIME_FORMAT);
        LocalDateTime endDate = DateTimeUtils.parse(endDateStr, DateTimeUtils.DEFAULT_DATE_TIME_FORMAT);
        return new Budget(id, name, limitAmount, spent, startDate, endDate, category);
    }

    private void loadTransactionsForBudget(Budget budget) throws SQLException {
        String sql = "SELECT t.* FROM Transactions t JOIN Budget_Transaction bt ON t.id = bt.transaction_id WHERE bt.budget_id = ?";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, budget.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                TransactionDAO transactionDAO = new TransactionDAO();
                while (rs.next()) {
                    String transId = rs.getString("id");
                    Transaction transaction = transactionDAO.get(transId);
                    if (transaction != null) {
                        budget.getTransactions().add(transaction);
                    }
                }
            }
        }
    }

    private void saveBudgetTransactions(Budget budget) throws SQLException {
        String sql = "INSERT INTO Budget_Transaction (budget_id, transaction_id) VALUES (?, ?)";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            for (Transaction transaction : budget.getTransactions()) {
                stmt.setString(1, budget.getId());
                stmt.setString(2, transaction.getTId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public void clearBudgetTransactions(String budgetId) throws SQLException {
        String sql = "DELETE FROM Budget_Transaction WHERE budget_id = ?";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, budgetId);
            stmt.executeUpdate();
        }
    }

    public void clearBudgetTransactionsByTransactionId(String transactionId) throws SQLException {
        String sql = "DELETE FROM Budget_Transaction WHERE transaction_id = ?";
        try (Connection con = db.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, transactionId);
            stmt.executeUpdate();
        }
    }
}
