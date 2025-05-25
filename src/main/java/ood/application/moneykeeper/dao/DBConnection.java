package ood.application.moneykeeper.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String URL = "jdbc:sqlite:moneykeeper.db";
    private static volatile DBConnection instance;
    private volatile Connection connection = null;

    private DBConnection() {
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL);
                // Enable foreign key constraints for SQLite
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                System.out.println("Database connection established.");
            } catch (SQLException e) {
                System.err.println("Failed to establish database connection.");
                throw e;
            }
        }
        createTablesIfNotExist();
        return connection;
    }

    public synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close database connection.");
                e.printStackTrace();
            }
        }
    }

    private void createTablesIfNotExist() {
        String[] createStatements = {
                """
            CREATE TABLE IF NOT EXISTS User (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS Wallet (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                balance REAL NOT NULL,
                owner_id TEXT,
                FOREIGN KEY (owner_id) REFERENCES User(id)
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS Category (
                cid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                isExpense INTEGER NOT NULL CHECK (isExpense IN (0, 1))
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS "Transactions" (
                id TEXT PRIMARY KEY,
                amount REAL NOT NULL,
                dateTime TEXT NOT NULL,
                category_id TEXT,
                description TEXT,
                wallet_id TEXT,
                isExpense INTEGER NOT NULL CHECK (isExpense IN (0, 1)),
                FOREIGN KEY (category_id) REFERENCES Category(cid),
                FOREIGN KEY (wallet_id) REFERENCES Wallet(id)
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS Budget (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                category_id TEXT,
                limitAmount REAL NOT NULL,
                startDate TEXT NOT NULL,
                endDate TEXT NOT NULL,
                spent REAL DEFAULT 0,
                FOREIGN KEY (category_id) REFERENCES Category(cid)
            );
            """,
                """
            CREATE TABLE IF NOT EXISTS "Budget_Transaction" (
                budget_id TEXT,
                transaction_id TEXT,
                PRIMARY KEY (budget_id, transaction_id),
                FOREIGN KEY (budget_id) REFERENCES Budget(id),
                FOREIGN KEY (transaction_id) REFERENCES Transactions(id)
            );
            """
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : createStatements) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("Failed to create tables.");
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Failed to rollback.");
                ex.printStackTrace();
            }
        }
    }
}