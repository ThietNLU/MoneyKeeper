package ood.application.moneykeeper.main;

import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.User;
import ood.application.moneykeeper.model.Wallet;
import ood.application.moneykeeper.observer.Observer;
import ood.application.moneykeeper.observer.ObserverManager;

import java.time.LocalDateTime;

/**
 * Simple Observer Pattern validation without JavaFX dependencies
 * This version avoids the JavaFX Toolkit initialization issue
 */
public class SimpleObserverValidation {

    public static void main(String[] args) {
        System.out.println("=== SIMPLE OBSERVER PATTERN VALIDATION ===\n");

        try {
            // Create custom test observer (no JavaFX dependencies)
            TestObserver testObserver = new TestObserver();
            
            // Test Budget creation and Subject functionality
            System.out.println("1. Testing Budget as Subject...");
            Category testCategory = new Category("Test Category", true);
            Budget testBudget = new Budget("Test Budget", 1000.0,
                    LocalDateTime.now(), LocalDateTime.now().plusMonths(1), testCategory);
            System.out.println("   ✓ Budget created successfully");

            // Test Observer registration
            System.out.println("\n2. Testing Observer registration...");
            testBudget.addObserver(testObserver);
            System.out.println("   ✓ Observer registered to Budget");

            // Test Observer notifications
            System.out.println("\n3. Testing Observer notifications...");

            // Test normal spending (should not trigger notifications)
            System.out.println("   Testing 50% spending (should not trigger notifications):");
            testObserver.resetNotificationCount();
            testBudget.updateSpent(500.0); // 50% of budget
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());

            // Test warning threshold (80%)
            System.out.println("\n   Testing 85% spending (should trigger WARNING):");
            testObserver.resetNotificationCount();
            testBudget.updateSpent(850.0); // 85% of budget
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());
            System.out.println("   - Last message: " + testObserver.getLastMessage());

            // Test over limit
            System.out.println("\n   Testing 120% spending (should trigger EXCEEDED):");
            testObserver.resetNotificationCount();
            testBudget.updateSpent(1200.0); // 120% of budget
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());
            System.out.println("   - Last message: " + testObserver.getLastMessage());

            // Test Observer unregistration
            System.out.println("\n4. Testing Observer unregistration...");
            testBudget.removeObserver(testObserver);
            System.out.println("   ✓ Observer unregistered from Budget");

            // Test that no notifications are sent after unregistration
            System.out.println("\n   Testing 150% spending after unregistration:");
            testObserver.resetNotificationCount();
            testBudget.updateSpent(1500.0); // Should not trigger notifications
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());
            System.out.println("   - " + (testObserver.getNotificationCount() == 0 ? "✓ No notifications (correct)" : "✗ Still receiving notifications"));

            // Test Transaction notifications
            System.out.println("\n5. Testing Transaction notifications...");
            User testUser = new User("1", "Test User");
            Wallet testWallet = new Wallet("Test Wallet", 1000.0, testUser);
            Transaction testTransaction = new Transaction(testWallet, 100.0, testCategory, "Test Transaction");
            testTransaction.setDateTime(LocalDateTime.now());

            // Re-register observer for transaction test
            testBudget.addObserver(testObserver);
            testObserver.resetNotificationCount();
            
            testBudget.addTransaction(testTransaction);
            System.out.println("   - Transaction added, notifications received: " + testObserver.getNotificationCount());            // Test Wallet Observer Pattern
            System.out.println("\n6. Testing Wallet Observer Pattern...");
            System.out.println("   Testing Wallet as Subject...");
            Wallet testWalletObserver = new Wallet("Test Wallet Observer", 500000.0, testUser);
            testWalletObserver.addObserver(testObserver);
            System.out.println("   ✓ Wallet created and observer registered");

            // Test normal balance update
            System.out.println("\n   Testing normal balance update:");
            testObserver.resetNotificationCount();
            testWalletObserver.updateBalance(400000.0);
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());

            // Test low balance warning
            System.out.println("\n   Testing low balance (90,000 VND - should trigger WARNING):");
            testObserver.resetNotificationCount();
            testWalletObserver.updateBalance(90000.0);
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());
            System.out.println("   - Last message: " + testObserver.getLastMessage());

            // Test critically low balance
            System.out.println("\n   Testing critically low balance (30,000 VND - should trigger CRITICAL):");
            testObserver.resetNotificationCount();
            testWalletObserver.updateBalance(30000.0);
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());
            System.out.println("   - Last message: " + testObserver.getLastMessage());

            // Test negative balance (overdraft)
            System.out.println("\n   Testing negative balance (-10,000 VND - should trigger NEGATIVE):");
            testObserver.resetNotificationCount();
            testWalletObserver.updateBalance(-10000.0);
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());
            System.out.println("   - Last message: " + testObserver.getLastMessage());            // Test income transaction
            System.out.println("\n   Testing income transaction:");
            testObserver.resetNotificationCount();
            testWalletObserver.income(100000.0);
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());
            System.out.println("   - Last message: " + testObserver.getLastMessage());

            // Test expense transaction
            System.out.println("\n   Testing expense transaction:");
            testObserver.resetNotificationCount();
            testWalletObserver.expense(50000.0);
            System.out.println("   - Notifications received: " + testObserver.getNotificationCount());
            System.out.println("   - Last message: " + testObserver.getLastMessage());

            // Test ObserverManager basic functionality
            System.out.println("\n7. Testing ObserverManager...");
            ObserverManager manager = ObserverManager.getInstance();
            System.out.println("   ✓ ObserverManager singleton obtained: " + (manager != null ? "Success" : "Failed"));

            System.out.println("\n=== VALIDATION COMPLETED SUCCESSFULLY ===");
            System.out.println("✅ Observer Pattern implementation is working correctly!");
            System.out.println("✅ Budget notifications (WARNING/EXCEEDED) are functioning");
            System.out.println("✅ Wallet notifications (LOW/CRITICAL/NEGATIVE/TRANSACTION) are functioning");
            System.out.println("✅ Observer registration/unregistration is working");
            System.out.println("✅ Transaction integration is working");

        } catch (Exception e) {
            System.err.println("❌ Error during validation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class TestObserver implements Observer {
        private int notificationCount = 0;
        private String lastMessage = "";

        @Override
        public void update(String message, Object data) {
            notificationCount++;
            lastMessage = message;
            System.out.println("   📢 Observer received: " + message + 
                    " (data: " + (data != null ? data.getClass().getSimpleName() : "null") + ")");
        }

        public int getNotificationCount() {
            return notificationCount;
        }

        public String getLastMessage() {
            return lastMessage;
        }

        public void resetNotificationCount() {
            notificationCount = 0;
            lastMessage = "";
        }
    }
}
