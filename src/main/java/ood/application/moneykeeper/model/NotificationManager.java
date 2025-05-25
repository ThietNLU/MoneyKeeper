package ood.application.moneykeeper.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to manage all notifications in the application
 */
public class NotificationManager {
    private static NotificationManager instance;
    private List<IObserver> globalObservers;
    private ObservableList<String> notificationHistory;
    private UINotificationObserver uiObserver;
    private NotificationObserver consoleObserver;

    private NotificationManager() {
        this.globalObservers = new ArrayList<>();
        this.notificationHistory = FXCollections.observableArrayList();
        this.consoleObserver = new NotificationObserver("GlobalConsole");
        this.globalObservers.add(consoleObserver);
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Initialize UI observer with status label
     */
    public void initializeUIObserver(Label statusLabel) {
        if (uiObserver == null) {
            uiObserver = new UINotificationObserver(statusLabel, notificationHistory);
            globalObservers.add(uiObserver);
        }
    }

    /**
     * Add a global observer that receives all notifications
     */
    public void addGlobalObserver(IObserver observer) {
        globalObservers.add(observer);
    }

    /**
     * Remove a global observer
     */
    public void removeGlobalObserver(IObserver observer) {
        globalObservers.remove(observer);
    }

    /**
     * Broadcast a notification to all global observers
     */
    public void broadcast(NotificationData notificationData) {
        for (IObserver observer : globalObservers) {
            observer.update(notificationData);
        }
    }

    /**
     * Convenience method to broadcast simple message
     */
    public void broadcast(NotificationType type, String message, String source) {
        broadcast(new NotificationData(type, message, source));
    }

    /**
     * Setup observers for a budget
     */
    public void setupBudgetObservers(Budget budget) {
        // Add a specialized budget alert observer
        BudgetAlertObserver budgetObserver = new BudgetAlertObserver(
            // On budget over limit
            overLimitBudget -> broadcast(
                NotificationType.BUDGET_ALERT,
                String.format("ALERT: Budget '%s' exceeded limit! %.2f / %.2f", 
                             overLimitBudget.getName(), 
                             overLimitBudget.getSpent(), 
                             overLimitBudget.getLimit()),
                "BudgetManager"
            ),
            // On budget near limit
            nearLimitBudget -> broadcast(
                NotificationType.WARNING,
                String.format("WARNING: Budget '%s' is near limit (%.1f%%)", 
                             nearLimitBudget.getName(),
                             (nearLimitBudget.getSpent() / nearLimitBudget.getLimit()) * 100),
                "BudgetManager"
            ),
            // On budget updated
            updatedBudget -> broadcast(
                NotificationType.INFO,
                String.format("Budget '%s' updated", updatedBudget.getName()),
                "BudgetManager"
            )
        );
        
        budget.addObserver(budgetObserver);
        
        // Also add global observers to receive all budget notifications
        for (IObserver globalObserver : globalObservers) {
            budget.addObserver(globalObserver);
        }
    }

    /**
     * Setup observers for a wallet
     */
    public void setupWalletObservers(Wallet wallet) {
        // Add a specialized wallet update observer
        WalletUpdateObserver walletObserver = new WalletUpdateObserver(
            // On balance changed
            changedWallet -> broadcast(
                NotificationType.WALLET_UPDATE,
                String.format("Wallet '%s' balance updated: %.2f", 
                             changedWallet.getName(), 
                             changedWallet.getBalance()),
                "WalletManager"
            ),
            // On low balance
            lowBalanceWallet -> broadcast(
                NotificationType.LOW_BALANCE,
                String.format("LOW BALANCE: Wallet '%s' has low balance: %.2f", 
                             lowBalanceWallet.getName(), 
                             lowBalanceWallet.getBalance()),
                "WalletManager"
            ),
            // On transaction added
            transaction -> broadcast(
                NotificationType.TRANSACTION_ADDED,
                String.format("Transaction added: %.2f - %s", 
                             transaction.getAmount(), 
                             transaction.getDescription()),
                "TransactionManager"
            )
        );
        
        wallet.addObserver(walletObserver);
        
        // Also add global observers to receive all wallet notifications
        for (IObserver globalObserver : globalObservers) {
            wallet.addObserver(globalObserver);
        }
    }

    /**
     * Get notification history for UI display
     */
    public ObservableList<String> getNotificationHistory() {
        return notificationHistory;
    }

    /**
     * Clear notification history
     */
    public void clearHistory() {
        notificationHistory.clear();
    }

    /**
     * Enable or disable UI alerts
     */
    public void setUIAlertsEnabled(boolean enabled) {
        if (uiObserver != null) {
            uiObserver.setShowAlerts(enabled);
        }
    }
}
