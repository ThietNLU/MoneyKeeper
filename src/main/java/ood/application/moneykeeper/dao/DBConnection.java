package ood.application.moneykeeper.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String URL = "jdbc:sqlite:moneykeeper.db";
    private static DBConnection instance;
    private Connection connection = null;

    private DBConnection() {
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL);
                System.out.println("Successful.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        createTablesIfNotExist();
        return connection;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void createTablesIfNotExist() {
        String createUser = """
                    CREATE TABLE IF NOT EXISTS User (
                            id TEXT PRIMARY KEY,
                        name TEXT NOT NULL
                    );
                """;

        String createWallet = """
                    CREATE TABLE IF NOT EXISTS Wallet (
                        id TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        balance REAL NOT NULL,
                        owner_id TEXT,
                        FOREIGN KEY (owner_id) REFERENCES User(id)
                    );
                """;

        String createCategory = """
                    CREATE TABLE IF NOT EXISTS Category (
                        cid TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        isExpense INTEGER NOT NULL CHECK (isExpense IN (0, 1))
                    );
                """;

        String createTransaction = """
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
                """;


        String createBudget = """
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
                """;

        String createBudgetTransaction = """
                    CREATE TABLE IF NOT EXISTS "Budget_Transaction" (
                        budget_id TEXT,
                        transaction_id TEXT,
                        PRIMARY KEY (budget_id, transaction_id),
                        FOREIGN KEY (budget_id) REFERENCES Budget(id),
                        FOREIGN KEY (transaction_id) REFERENCES Transactions(id)
                    );
                """;

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(createUser);
            stmt.execute(createWallet);
            stmt.execute(createCategory);
            stmt.execute(createTransaction);
            stmt.execute(createBudget);
            stmt.execute(createBudgetTransaction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
