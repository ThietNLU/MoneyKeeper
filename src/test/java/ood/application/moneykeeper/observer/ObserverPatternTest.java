package ood.application.moneykeeper.observer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Observer pattern integration
 */
public class ObserverPatternTest {
    
    private ObserverManager observerManager;
    private ObservableList<Transaction> transactionList;
    private ObservableList<Budget> budgetList;
    private ObservableList<String> notificationHistory;
    
    @BeforeEach
    void setUp() {
        // Reset singleton instance for testing
        ObserverManager.getInstance();
        observerManager = ObserverManager.getInstance();
        
        // Initialize test data
        transactionList = FXCollections.observableArrayList();
        budgetList = FXCollections.observableArrayList();
        notificationHistory = FXCollections.observableArrayList();
        
        // Initialize observers
        observerManager.initializeObservers(transactionList, budgetList, notificationHistory);
    }
    
    @Test
    void testBudgetObserverRegistration() {
        // Create test budget
        Category testCategory = new Category("1", "Test Category", "#FF0000");
        Budget testBudget = new Budget("Test Budget", 1000.0, 
            LocalDateTime.now(), LocalDateTime.now().plusMonths(1), testCategory);
        
        // Register observers
        observerManager.registerBudgetObservers(testBudget);
        
        // Verify observers are registered (check observer count)
        assertNotNull(observerManager.getNotificationObserver());
        assertNotNull(observerManager.getUIUpdateObserver());
        
        // Test budget limit checking (this should trigger observer notifications)
        testBudget.updateSpent(1200.0); // Over limit
        
        // Verify notification was added (in real scenario this would check UI updates)
        assertTrue(notificationHistory.size() > 0);
    }
    
    @Test
    void testBudgetObserverUnregistration() {
        // Create test budget
        Category testCategory = new Category("1", "Test Category", "#FF0000");
        Budget testBudget = new Budget("Test Budget", 1000.0, 
            LocalDateTime.now(), LocalDateTime.now().plusMonths(1), testCategory);
        
        // Register then unregister observers
        observerManager.registerBudgetObservers(testBudget);
        observerManager.unregisterBudgetObservers(testBudget);
        
        // Clear notification history
        notificationHistory.clear();
        
        // Test budget limit checking (should not trigger notifications after unregistering)
        testBudget.updateSpent(1200.0); // Over limit
        
        // Verify no notification was added
        assertEquals(0, notificationHistory.size());
    }
    
    @Test
    void testTransactionNotifications() {
        // Create test transaction
        Category testCategory = new Category("1", "Test Category", "#FF0000");
        Transaction testTransaction = new Transaction("1", "Test Transaction", 
            100.0, LocalDateTime.now(), testCategory, null);
        
        // Clear notification history
        notificationHistory.clear();
        
        // Notify transaction added
        observerManager.notifyTransactionAdded(testTransaction);
        
        // Verify notification was added
        assertTrue(notificationHistory.size() > 0);
        assertTrue(notificationHistory.get(0).contains("giao dịch mới"));
    }
    
    @Test
    void testBudgetWarningThreshold() {
        // Create test budget
        Category testCategory = new Category("1", "Test Category", "#FF0000");
        Budget testBudget = new Budget("Test Budget", 1000.0, 
            LocalDateTime.now(), LocalDateTime.now().plusMonths(1), testCategory);
        
        // Register observers
        observerManager.registerBudgetObservers(testBudget);
        
        // Clear notification history
        notificationHistory.clear();
        
        // Test budget warning (80% of limit)
        testBudget.updateSpent(850.0); // Should trigger warning
        
        // Verify warning notification was added
        assertTrue(notificationHistory.size() > 0);
        assertTrue(notificationHistory.get(0).contains("cảnh báo"));
    }
}
