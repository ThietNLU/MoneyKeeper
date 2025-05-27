package ood.application.moneykeeper.observer;

import javafx.collections.ObservableList;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.Wallet;

/**
 * ObserverManager - quản lý tập trung các observers trong ứng dụng
 */
public class ObserverManager {
    private static ObserverManager instance;
    
    private NotificationObserver notificationObserver;
    private UIUpdateObserver uiUpdateObserver;
    
    private ObserverManager() {
        // Private constructor for singleton
    }
    
    public static ObserverManager getInstance() {
        if (instance == null) {
            instance = new ObserverManager();
        }
        return instance;
    }
    
    /**
     * Khởi tạo các observers
     */
    public void initializeObservers(ObservableList<Transaction> transactionList,
                                   ObservableList<Budget> budgetList,
                                   ObservableList<String> notificationHistory) {
        // Khởi tạo NotificationObserver
        notificationObserver = new NotificationObserver("System Notification");
        
        // Khởi tạo UIUpdateObserver
        uiUpdateObserver = new UIUpdateObserver(transactionList, budgetList, notificationHistory);    }
      /**
     * Đăng ký observers cho một wallet
     */
    public void registerWalletObservers(Wallet wallet) {
        if (wallet != null) {
            try {
                if (notificationObserver != null) {
                    wallet.addObserver(notificationObserver);
                }
                if (uiUpdateObserver != null) {
                    wallet.addObserver(uiUpdateObserver);
                }
            } catch (Exception e) {
                System.err.println("Error registering observers for wallet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
      /**
     * Hủy đăng ký observers cho một wallet
     */
    public void unregisterWalletObservers(Wallet wallet) {
        if (wallet != null) {
            try {
                if (notificationObserver != null) {
                    wallet.removeObserver(notificationObserver);
                }
                if (uiUpdateObserver != null) {
                    wallet.removeObserver(uiUpdateObserver);
                }
            } catch (Exception e) {
                System.err.println("Error unregistering observers for wallet: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Đăng ký observers cho một budget
     */
    public void registerBudgetObservers(Budget budget) {
        if (budget != null) {
            try {
                if (notificationObserver != null) {
                    budget.addObserver(notificationObserver);
                }
                if (uiUpdateObserver != null) {
                    budget.addObserver(uiUpdateObserver);
                }
            } catch (Exception e) {
                System.err.println("Error registering observers for budget: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
      /**
     * Hủy đăng ký observers cho một budget
     */
    public void unregisterBudgetObservers(Budget budget) {
        if (budget != null) {
            try {
                if (notificationObserver != null) {
                    budget.removeObserver(notificationObserver);
                }
                if (uiUpdateObserver != null) {
                    budget.removeObserver(uiUpdateObserver);
                }
            } catch (Exception e) {
                System.err.println("Error unregistering observers for budget: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
      /**
     * Thông báo transaction mới được thêm
     */
    public void notifyTransactionAdded(Transaction transaction) {
        try {
            if (notificationObserver != null) {
                notificationObserver.update("TRANSACTION_ADDED", transaction);
            }
            if (uiUpdateObserver != null) {
                uiUpdateObserver.update("TRANSACTION_ADDED", transaction);
            }
        } catch (Exception e) {
            System.err.println("Error notifying transaction added: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Thông báo transaction được cập nhật
     */
    public void notifyTransactionUpdated(Transaction transaction) {
        if (notificationObserver != null) {
            notificationObserver.update("TRANSACTION_UPDATED", transaction);
        }
        if (uiUpdateObserver != null) {
            uiUpdateObserver.update("TRANSACTION_UPDATED", transaction);
        }
    }
    
    /**
     * Thông báo transaction được xóa
     */
    public void notifyTransactionDeleted(Transaction transaction) {
        if (notificationObserver != null) {
            notificationObserver.update("TRANSACTION_DELETED", transaction);
        }
        if (uiUpdateObserver != null) {
            uiUpdateObserver.update("TRANSACTION_DELETED", transaction);
        }
    }
    
    /**
     * Thông báo số dư ví được cập nhật
     */
    public void notifyWalletBalanceUpdated(Object walletData) {
        if (notificationObserver != null) {
            notificationObserver.update("WALLET_BALANCE_UPDATED", walletData);
        }
        if (uiUpdateObserver != null) {
            uiUpdateObserver.update("WALLET_BALANCE_UPDATED", walletData);
        }
    }
    
    // Getters for observers
    public NotificationObserver getNotificationObserver() {
        return notificationObserver;
    }
    
    public UIUpdateObserver getUIUpdateObserver() {
        return uiUpdateObserver;
    }
}
