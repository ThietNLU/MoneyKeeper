package ood.application.moneykeeper.dao;

import ood.application.moneykeeper.model.*;
import ood.application.moneykeeper.utils.DateTimeUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO implements DAO<Transaction, String> {
    private final DBConnection db = DBConnection.getInstance();
    private final WalletDAO walletDAO;
    private final CategoryDAO categoryDAO;

    public TransactionDAO() throws SQLException {
        this.walletDAO = new WalletDAO();
        this.categoryDAO = new CategoryDAO();
    }

    @Override
    public List<Transaction> getAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        Connection con = db.getConnection();

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Transactions");

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    @Override
    public Transaction get(String id) throws SQLException {
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Transactions WHERE id = ?");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractTransactionFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean save(Transaction transaction) throws SQLException {
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO Transactions (id, amount, dateTime, category_id, description, wallet_id, isExpense) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)"
            );

            stmt.setString(1, transaction.getTId());
            stmt.setDouble(2, transaction.getAmount());
            stmt.setString(3, DateTimeUtils.formatDefault(transaction.getDateTime()));
            stmt.setString(4, transaction.getCategory().getId());
            stmt.setString(5, transaction.getDescription());
            stmt.setString(6, transaction.getWallet().getId());
            stmt.setBoolean(7, transaction.getStrategy().isExpense());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Transaction transaction) throws SQLException {
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement(
                    "UPDATE Transactions SET amount = ?, dateTime = ?, category_id = ?, " +
                            "description = ?, wallet_id = ?, isExpense = ? WHERE id = ?"
            );

            stmt.setDouble(1, transaction.getAmount());
            stmt.setString(2, DateTimeUtils.formatDefault(transaction.getDateTime()));
            stmt.setString(3, transaction.getCategory().getId());
            stmt.setString(4, transaction.getDescription());
            stmt.setString(5, transaction.getWallet().getId());
            stmt.setBoolean(6, transaction.getStrategy().isExpense());
            stmt.setString(7, transaction.getTId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(Transaction transaction) throws SQLException {
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Transactions WHERE id = ?");
            stmt.setString(1, transaction.getTId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteById(String id) throws SQLException {
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Transactions WHERE id = ?");
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public List<Transaction> getTransactionsByWallet(String walletId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Transactions WHERE wallet_id = ?");
            stmt.setString(1, walletId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<Transaction> getTransactionsByCategory(String categoryId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Transactions WHERE category_id = ?");
            stmt.setString(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT * FROM Transactions WHERE dateTime BETWEEN ? AND ?"
            );
            stmt.setString(1, DateTimeUtils.formatDefault(start));
            stmt.setString(2, DateTimeUtils.formatDefault(end));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<Transaction> getTransactionsByWalletId(String walletId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        Connection con = db.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Transactions WHERE wallet_id = ?");
            stmt.setString(1, walletId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByWalletAndDateRange(String walletId, LocalDateTime start, LocalDateTime end) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        Connection con = db.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement(
                "SELECT * FROM Transactions WHERE wallet_id = ? AND dateTime BETWEEN ? AND ?"
            );
            stmt.setString(1, walletId);
            stmt.setString(2, DateTimeUtils.formatDefault(start));
            stmt.setString(3, DateTimeUtils.formatDefault(end));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByWalletAndType(String walletId, boolean isExpense) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        Connection con = db.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement(
                "SELECT * FROM Transactions WHERE wallet_id = ? AND isExpense = ?"
            );
            stmt.setString(1, walletId);
            stmt.setBoolean(2, isExpense);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }    private Transaction extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        double amount = rs.getDouble("amount");
        String dateTimeStr = rs.getString("dateTime");
        String categoryId = rs.getString("category_id");
        String description = rs.getString("description");
        String walletId = rs.getString("wallet_id");
        boolean isExpense = rs.getBoolean("isExpense");

        // Get related objects
        Wallet wallet = walletDAO.get(walletId);
        Category category = categoryDAO.get(categoryId);
        LocalDateTime dateTime = DateTimeUtils.parse(dateTimeStr, DateTimeUtils.DEFAULT_DATE_TIME_FORMAT);

        // Log warning if wallet not found
        if (wallet == null) {
            System.err.println("Warning: Wallet with ID '" + walletId + "' not found for transaction '" + id + "'");
        }

        // Create transaction with appropriate strategy
        Transaction transaction = new Transaction(id, wallet, null , amount, dateTime, category, description);

        if (isExpense) {
            transaction.setStrategy(new ExpenseTransactionStrategy());
        } else {
            transaction.setStrategy(new IncomTransactionStrategy());
        }

        return transaction;
    }
}

