package ood.application.moneykeeper.dao;

import ood.application.moneykeeper.model.*;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO implements DAO<ATransaction, String> {
    private final DBConnection db = DBConnection.getInstance();

    @Override
    public List<ATransaction> getAll() {
        List<ATransaction> list = new ArrayList<>();
        String query = "SELECT * FROM Transactions";
        try (Connection con = db.getConnection();
             Statement stm = con.createStatement();
             ResultSet rs = stm.executeQuery(query)) {
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

            LocalDateTime transactionTime = (t.getDateTime() != null)
                    ? t.getDateTime()
                    : LocalDateTime.now();

            // Sử dụng milliseconds từ epoch để lưu trữ
            long timestamp = transactionTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            ps.setString(1, t.getTId());
            ps.setDouble(2, t.getAmount());
            ps.setLong(3, timestamp); // Lưu dưới dạng số
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
        String query = "UPDATE Transactions SET amount=?, dateTime=?, category_id=?, description=?, wallet_id=?, isExpense=? WHERE id=?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            long timestamp = t.getDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            ps.setDouble(1, t.getAmount());
            ps.setLong(2, timestamp);
            ps.setString(3, t.getCategory().getId());
            ps.setString(4, t.getDescription());
            ps.setString(5, t.getWallet().getId());
            ps.setBoolean(6, t instanceof ExpenseTransaction);
            ps.setString(7, t.getTId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(ATransaction t) {
        String query = "DELETE FROM Transactions WHERE id=?";
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, t.getTId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ATransaction buildTransactionFromResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        double amount = rs.getDouble("amount");

        // Đọc timestamp dưới dạng milliseconds từ epoch
        long timestampMillis = rs.getLong("dateTime");
        LocalDateTime dateTime = Instant.ofEpochMilli(timestampMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        String category_id = rs.getString("category_id");
        String description = rs.getString("description");
        String wallet_id = rs.getString("wallet_id");
        boolean isExpense = rs.getBoolean("isExpense");

        Category category = new CategoryDAO().get(category_id);
        Wallet wallet = new WalletDAO().get(wallet_id);

        if (category == null || wallet == null) return null;

        return isExpense
                ? new ExpenseTransaction(id, wallet, amount, dateTime, category, description)
                : new IncomeTransaction(id, wallet, amount, dateTime, category, description);
    }
}