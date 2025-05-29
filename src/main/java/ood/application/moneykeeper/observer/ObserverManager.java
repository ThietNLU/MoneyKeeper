package ood.application.moneykeeper.observer;

import javafx.collections.ObservableList;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.Wallet;

public class ObserverManager {
    private static ObserverManager instance;
    private NotificationObserver notificationObserver;
    private UIUpdateObserver uiUpdateObserver;

    private ObserverManager() {
    }

    public static ObserverManager getInstance() {
        if (instance == null) {
            instance = new ObserverManager();
        }
        return instance;
    }

    public void initializeObservers(ObservableList<Transaction> transactionList,
                                    ObservableList<Budget> budgetList,
                                    ObservableList<String> notificationHistory) {
        notificationObserver = new NotificationObserver("System Notification");

        uiUpdateObserver = new UIUpdateObserver(transactionList, budgetList, notificationHistory);
    }

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

    public void notifyTransactionUpdated(Transaction transaction) {
        if (notificationObserver != null) {
            notificationObserver.update("TRANSACTION_UPDATED", transaction);
        }
        if (uiUpdateObserver != null) {
            uiUpdateObserver.update("TRANSACTION_UPDATED", transaction);
        }
    }

    public void notifyTransactionDeleted(Transaction transaction) {
        if (notificationObserver != null) {
            notificationObserver.update("TRANSACTION_DELETED", transaction);
        }
        if (uiUpdateObserver != null) {
            uiUpdateObserver.update("TRANSACTION_DELETED", transaction);
        }
    }

    public void notifyWalletBalanceUpdated(Object walletData) {
        if (notificationObserver != null) {
            notificationObserver.update("WALLET_BALANCE_UPDATED", walletData);
        }
        if (uiUpdateObserver != null) {
            uiUpdateObserver.update("WALLET_BALANCE_UPDATED", walletData);
        }
    }

    public NotificationObserver getNotificationObserver() {
        return notificationObserver;
    }

    public UIUpdateObserver getUIUpdateObserver() {
        return uiUpdateObserver;
    }
}
