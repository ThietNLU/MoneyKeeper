package ood.application.moneykeeper.dao;

import ood.application.moneykeeper.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements DAO<User, String> {
    DBConnection db = DBConnection.getInstance();

    public UserDAO() throws SQLException {}

    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        Connection con = db.getConnection();
        try {
            Statement stm = con.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM User");

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                users.add(new User(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    @Override
    public User get(String id) throws SQLException {
        User user = null;
        Connection con = db.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM User WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = new User(rs.getString("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return user;
    }

    @Override
    public boolean save(User user) throws SQLException {
        Connection con = db.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO User (id, name) VALUES (?, ?)");
            ps.setString(1, user.getId());
            ps.setString(2, user.getName());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            
        }
        return false;
    }

    @Override
    public boolean update(User user) throws SQLException {
        Connection con = db.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE User SET name = ? WHERE id = ?");
            ps.setString(1, user.getName());
            ps.setString(2, user.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            
        }
        return false;
    }

    @Override
    public boolean delete(User user) throws SQLException {
        Connection con = db.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM User WHERE id = ?");
            ps.setString(1, user.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            
        }
        return false;
    }

    @Override
    public boolean deleteById(String s) throws SQLException {
        Connection con = db.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM User WHERE id = ?");
            ps.setString(1, s);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }
}
