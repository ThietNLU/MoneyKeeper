package ood.application.moneykeeper.dao;

import ood.application.moneykeeper.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO implements DAO<Category, String> {
    private final DBConnection db = DBConnection.getInstance();

    public CategoryDAO() throws SQLException {
    }

    @Override
    public List<Category> getAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        Connection con = db.getConnection();

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Category");

            while (rs.next()) {
                String id = rs.getString("cid");
                String name = rs.getString("name");
                boolean isExpense = rs.getBoolean("isExpense");

                categories.add(new Category(id, name, isExpense));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return categories;
    }

    @Override
    public Category get(String id) throws SQLException {
        Category category = null;
        Connection con = db.getConnection();

        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Category WHERE cid = ?");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                boolean isExpense = rs.getBoolean("isExpense");
                category = new Category(id, name, isExpense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return category;
    }

    @Override
    public boolean save(Category category) throws SQLException {
        Connection con = db.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement("INSERT INTO Category (cid, name, isExpense) VALUES (?, ?, ?)");
            stmt.setString(1, category.getId());
            stmt.setString(2, category.getName());
            stmt.setBoolean(3, category.isExpense());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }

    @Override
    public boolean update(Category category) throws SQLException {
        Connection con = db.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement("UPDATE Category SET name = ?, isExpense = ? WHERE cid = ?");
            stmt.setString(1, category.getName());
            stmt.setBoolean(2, category.isExpense());
            stmt.setString(3, category.getId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }
        return false;
    }

    @Override
    public boolean delete(Category category) throws SQLException {
        Connection con = db.getConnection();
        try {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Category WHERE cid = ?");
            stmt.setString(1, category.getId());

            int affected = stmt.executeUpdate();
            return affected > 0;
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
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Category WHERE cid = ?");
            stmt.setString(1, s);

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
