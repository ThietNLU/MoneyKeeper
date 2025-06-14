package ood.application.moneykeeper.dao;

import ood.application.moneykeeper.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WalletDAO implements DAO<Wallet, String> {
    DBConnection db = DBConnection.getInstance();

    public WalletDAO() throws SQLException {
    }    @Override
    public List<Wallet> getAll() throws SQLException {
        List<Wallet> wallets = new ArrayList<>();
        Connection con = db.getConnection();
        try (Statement stm = con.createStatement();
             ResultSet rs = stm.executeQuery("SELECT * FROM Wallet")) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                double balance = rs.getDouble("balance");
                String owner_id = rs.getString("owner_id");

                User user = new UserDAO().get(owner_id);
                Wallet wallet = new Wallet(id, name, balance, user);
                wallets.add(wallet);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all wallets: " + e.getMessage());
            throw e;
        }

        return wallets;
    }    @Override
    public Wallet get(String id) throws SQLException {
        Wallet wallet = null;
        Connection con = db.getConnection();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM Wallet WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    double balance = rs.getDouble("balance");
                    String owner_id = rs.getString("owner_id");

                    User user = new UserDAO().get(owner_id);
                    wallet = new Wallet(id, name, balance, user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting wallet: " + e.getMessage());
            throw e;
        }
        return wallet;
    }    @Override
    public boolean save(Wallet wallet) throws SQLException {
        Connection con = db.getConnection();
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO Wallet (id, name, balance, owner_id) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, wallet.getId());
            ps.setString(2, wallet.getName());
            ps.setDouble(3, wallet.getBalance());
            ps.setString(4, wallet.getOwner().getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error saving wallet: " + e.getMessage());
            throw e;
        }
    }    @Override
    public boolean update(Wallet wallet) throws SQLException {
        Connection con = db.getConnection();
        try (PreparedStatement ps = con.prepareStatement("UPDATE Wallet SET name = ?, balance = ?, owner_id = ? WHERE id = ?")) {
            ps.setString(1, wallet.getName());
            ps.setDouble(2, wallet.getBalance());
            ps.setString(3, wallet.getOwner().getId());
            ps.setString(4, wallet.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating wallet: " + e.getMessage());
            throw e;
        }
    }    @Override
    public boolean delete(Wallet wallet) throws SQLException {
        Connection con = db.getConnection();
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM Wallet WHERE id = ?")) {
            ps.setString(1, wallet.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting wallet: " + e.getMessage());
            throw e;
        }
    }@Override
    public boolean deleteById(String s) throws SQLException {
        Connection con = db.getConnection();
        boolean success = false;
        
        try {
            // Start transaction
            con.setAutoCommit(false);
            
            // First, delete Budget_Transaction entries for transactions in this wallet
            PreparedStatement ps1 = con.prepareStatement(
                "DELETE FROM Budget_Transaction WHERE transaction_id IN " +
                "(SELECT id FROM Transactions WHERE wallet_id = ?)"
            );
            ps1.setString(1, s);
            ps1.executeUpdate();
            ps1.close();
            
            // Then delete all transactions for this wallet
            PreparedStatement ps2 = con.prepareStatement("DELETE FROM Transactions WHERE wallet_id = ?");
            ps2.setString(1, s);
            ps2.executeUpdate();
            ps2.close();
            
            // Finally delete the wallet
            PreparedStatement ps3 = con.prepareStatement("DELETE FROM Wallet WHERE id = ?");
            ps3.setString(1, s);
            int rows = ps3.executeUpdate();
            ps3.close();
            
            // Commit transaction
            con.commit();
            success = rows > 0;
            
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error deleting wallet: " + e.getMessage());
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
        
        return success;
    }
}

