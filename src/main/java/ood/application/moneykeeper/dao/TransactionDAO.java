package ood.application.moneykeeper.dao;

import ood.application.moneykeeper.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO implements DAO<ATransaction, String> {
    DBConnection db = DBConnection.getInstance();

    public TransactionDAO() {
    }

    @Override
    public List<ATransaction> getAll() {
        List<ATransaction> list = new ArrayList<>();
        try {
            Connection con = db.getConnection();
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM Transactions");
            while (rs.next()) {
                ATransaction transaction = buildTransactionFromResultSet(rs);
                if (transaction != null) list.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ATransaction get(String trans_id) {
        ATransaction result = null;
        String query = "SELECT * FROM Transactions WHERE id = ?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, trans_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = buildTransactionFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean save(ATransaction t) {
        String query = "INSERT INTO Transactions (id, amount, dateTime, category_id, description, wallet_id, isExpense) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            // Lấy thời gian hiện tại nếu transaction không có thời gian
            LocalDateTime transactionTime = (t.getDateTime() != null)
                    ? t.getDateTime()
                    : LocalDateTime.now();

            ps.setString(1, t.getTId());
            ps.setDouble(2, t.getAmount());
            ps.setTimestamp(3, Timestamp.valueOf(transactionTime)); // Chuyển LocalDateTime → Timestamp
            ps.setString(4, t.getCategory().getId());
            ps.setString(5, t.getDescription());
            ps.setString(6, t.getWallet().getId());
            ps.setBoolean(7, t instanceof ExpenseTransaction);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(ATransaction t) {
        String query = "UPDATE Transactions SET amount=?, category_id=?, description=?, wallet_id=?, isExpense=? WHERE id=?";
        try {
            Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setDouble(1, t.getAmount());
            ps.setString(2, t.getCategory().getId());
            ps.setString(3, t.getDescription());
            ps.setString(4, t.getWallet().getId());
            ps.setBoolean(5, t instanceof ExpenseTransaction);
            ps.setString(6, t.getTId());

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(ATransaction t) {
        String query = "DELETE FROM Transactions WHERE id=?";
        try {
            Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, t.getTId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ATransaction buildTransactionFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        double amount = rs.getDouble("amount");

        // Đọc Timestamp và chuyển thành LocalDateTime
        Timestamp timestamp = rs.getTimestamp("dateTime");
        LocalDateTime dateTime = timestamp != null ? timestamp.toLocalDateTime() : null;

        String category_id = rs.getString("category_id");
        String description = rs.getString("description");
        String wallet_id = rs.getString("wallet_id");
        boolean isExpense = rs.getBoolean("isExpense");

        Category category = new CategoryDAO().get(category_id);
        Wallet wallet = new WalletDAO().get(wallet_id);

        if (category == null || wallet == null) return null;

        if (isExpense)
            return new ExpenseTransaction(id, wallet, amount, dateTime, category, description);
        else
            return new IncomeTransaction(id, wallet, amount, dateTime, category, description);
    }

    private Category getCategoryById(String id) {
        String query = "SELECT * FROM Category WHERE cid=?";
        try {
            Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getString("cid"), rs.getString("name"), rs.getBoolean("isExpense"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Wallet getWalletById(String id) {
        String query = "SELECT * FROM Wallet WHERE id=?";
        try {
            Connection con = db.getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    double balance = rs.getDouble("balance");
                    String owner_id = rs.getString("owner_id");

                    User user = new UserDAO().get(owner_id);
                    return new Wallet(id, name, balance, user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
