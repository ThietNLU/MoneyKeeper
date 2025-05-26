package ood.application.moneykeeper.main;

import ood.application.moneykeeper.dao.*;
import ood.application.moneykeeper.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DBTest {

    public static void main(String[] args) {
        try {
            // Initialize DAOs
            BudgetDAO budgetDAO = new BudgetDAO();
            CategoryDAO categoryDAO = new CategoryDAO();
            UserDAO userDAO = new UserDAO();
            WalletDAO walletDAO = new WalletDAO();
            TransactionDAO transactionDAO = new TransactionDAO();

            // Create test user
            User testUser = new User("Budget Test User");
            userDAO.save(testUser);
            System.out.println("Created user: " + testUser.getName());

            // Create wallet
            Wallet testWallet = new Wallet("Budget Test Wallet", 5000.0, testUser);
            walletDAO.save(testWallet);
            System.out.println("Created wallet: " + testWallet.getName() + " with balance: " + testWallet.getBalance());

            // Create expense categories
            Category foodCategory = new Category("Food Budget", true);
            Category transportCategory = new Category("Transport Budget", true);
            Category entertainmentCategory = new Category("Entertainment Budget", true);

            categoryDAO.save(foodCategory);
            categoryDAO.save(transportCategory);
            categoryDAO.save(entertainmentCategory);

            System.out.println("Created expense categories for budgeting");

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

            // Create transactions for different categories
            Transaction foodTrans1 = new Transaction(testWallet, 150.0, foodCategory, "Grocery shopping");
            foodTrans1.setDateTime(now.plusDays(1));
            foodTrans1.setStrategy(new ExpenseTransactionStrategy());

            Transaction foodTrans2 = new Transaction(testWallet, 80.0, foodCategory, "Restaurant dinner");
            foodTrans2.setDateTime(now.plusDays(2));
            foodTrans2.setStrategy(new ExpenseTransactionStrategy());

            Transaction transportTrans = new Transaction(testWallet, 200.0, transportCategory, "Monthly bus pass");
            transportTrans.setDateTime(now.plusDays(3));
            transportTrans.setStrategy(new ExpenseTransactionStrategy());

            Transaction entertainmentTrans = new Transaction(testWallet, 150.0, entertainmentCategory, "Movie tickets");
            entertainmentTrans.setDateTime(now.plusDays(4));
            entertainmentTrans.setStrategy(new ExpenseTransactionStrategy());

            // Save transactions
            transactionDAO.save(foodTrans1);
            transactionDAO.save(foodTrans2);
            transactionDAO.save(transportTrans);
            transactionDAO.save(entertainmentTrans);

            System.out.println("Saved transactions to database");

            // Process wallet balances
            foodTrans1.processWallet();
            foodTrans2.processWallet();
            transportTrans.processWallet();
            entertainmentTrans.processWallet();

            // Update wallet
            walletDAO.update(testWallet);
            System.out.println("Updated wallet balance: " + testWallet.getBalance());

            // Process budget transactions
            // We'll add the transactions to the budget and update budget spending
            foodBudget.addTransaction(foodTrans1);
            foodBudget.addTransaction(foodTrans2);
            transportBudget.addTransaction(transportTrans);
            entertainmentBudget.addTransaction(entertainmentTrans);

            // Update budgets in database
            budgetDAO.update(foodBudget);
            budgetDAO.update(transportBudget);
            budgetDAO.update(entertainmentBudget);

            System.out.println("Updated budgets with transactions");

            // Retrieve budgets and check status
            Budget retrievedFoodBudget = budgetDAO.get(foodBudget.getId());
            Budget retrievedTransportBudget = budgetDAO.get(transportBudget.getId());
            Budget retrievedEntertainmentBudget = budgetDAO.get(entertainmentBudget.getId());

            System.out.println("\nBudget Status:");
            System.out.println("Food Budget: " + retrievedFoodBudget.getSpent() + " / " + retrievedFoodBudget.getLimit()
                    + " (" + (retrievedFoodBudget.getSpent() / retrievedFoodBudget.getLimit() * 100) + "%)");
            System.out.println("Transport Budget: " + retrievedTransportBudget.getSpent() + " / " + retrievedTransportBudget.getLimit()
                    + " (" + (retrievedTransportBudget.getSpent() / retrievedTransportBudget.getLimit() * 100) + "%)");
            System.out.println("Entertainment Budget: " + retrievedEntertainmentBudget.getSpent() + " / " + retrievedEntertainmentBudget.getLimit()
                    + " (" + (retrievedEntertainmentBudget.getSpent() / retrievedEntertainmentBudget.getLimit() * 100) + "%)");

            // Check for over limit budgets
            List<Budget> allBudgets = budgetDAO.getAll();
            System.out.println("\nChecking for over-limit budgets:");
            for (Budget budget : allBudgets) {
                if (budget.isOverLimit()) {
                    System.out.println("ALERT: " + budget.getName() + " is over limit!");
                }
            }

            System.out.println("\n=== OBSERVER PATTERN DEMO ===");
            System.out.println("Setting up observers for budget and wallet...");

            // Add another transaction that would push the budget over limit
            Transaction overLimitTrans = new Transaction(testWallet, 800.0, foodCategory, "Expensive dinner");
            overLimitTrans.setDateTime(now.plusDays(5));
            overLimitTrans.setStrategy(new ExpenseTransactionStrategy());

            System.out.println("\nAdding transaction that will trigger budget over-limit...");
            transactionDAO.save(overLimitTrans);
            
            // Process transaction (this will trigger observers)
            testWallet.addTransaction(overLimitTrans);  // This will notify wallet observers
            walletDAO.update(testWallet);

            retrievedFoodBudget.addTransaction(overLimitTrans);  // This will notify budget observers
            budgetDAO.update(retrievedFoodBudget);

            // Test different notification types
            System.out.println("\n=== Testing different notification types ===");
            
            // Test wallet balance update
            System.out.println("Testing wallet balance update notification...");
            testWallet.updateBalance(testWallet.getBalance() - 100);
            
            // Test budget spending update
            System.out.println("Testing budget update notification...");
            retrievedFoodBudget.updateSpent(retrievedFoodBudget.getSpent() + 50);
            
            // Test low balance notification
            System.out.println("Testing low balance notification...");
            testWallet.updateBalance(500.0);  // Set to low balance

            System.out.println("\nFinal wallet balance: " + testWallet.getBalance());
            System.out.println("=== OBSERVER PATTERN DEMO COMPLETE ===");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }


}

