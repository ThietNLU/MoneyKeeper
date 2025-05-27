package ood.application.moneykeeper.observer;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.Wallet;

/**
 * UIUpdateObserver - cập nhật giao diện người dùng khi có thay đổi
 */
public class UIUpdateObserver implements Observer {
    private ObservableList<Transaction> transactionList;
    private ObservableList<Budget> budgetList;
    private ObservableList<String> notificationHistory;
    
    public UIUpdateObserver() {
        // Constructor mặc định
    }
    
    public UIUpdateObserver(ObservableList<Transaction> transactionList, 
                           ObservableList<Budget> budgetList,
                           ObservableList<String> notificationHistory) {
        this.transactionList = transactionList;
        this.budgetList = budgetList;
        this.notificationHistory = notificationHistory;
    }
    
    @Override
    public void update(String message, Object data) {
        Platform.runLater(() -> {
            switch (message) {
                case "TRANSACTION_ADDED":
                    updateTransactionList(data);
                    addNotification("Đã thêm giao dịch mới");
                    break;
                case "TRANSACTION_UPDATED":
                    refreshTransactionList();
                    addNotification("Đã cập nhật giao dịch");
                    break;
                case "TRANSACTION_DELETED":
                    refreshTransactionList();
                    addNotification("Đã xóa giao dịch");
                    break;
                case "BUDGET_EXCEEDED":
                    refreshBudgetList();
                    if (data instanceof Budget) {
                        Budget budget = (Budget) data;
                        addNotification("Cảnh báo: Ngân sách '" + budget.getName() + "' đã vượt quá giới hạn!");
                    }
                    break;                case "BUDGET_WARNING":
                    if (data instanceof Budget) {
                        Budget budget = (Budget) data;
                        addNotification("Chú ý: Ngân sách '" + budget.getName() + "' sắp đạt giới hạn!");
                    }
                    break;
                case "WALLET_BALANCE_UPDATED":
                    addNotification("Số dư ví đã được cập nhật");
                    break;
                case "WALLET_LOW_BALANCE":
                    if (data instanceof Wallet) {
                        Wallet wallet = (Wallet) data;
                        addNotification("Cảnh báo: Ví '" + wallet.getName() + "' có số dư thấp!");
                    }
                    break;
                case "WALLET_CRITICALLY_LOW":
                    if (data instanceof Wallet) {
                        Wallet wallet = (Wallet) data;
                        addNotification("Nguy hiểm: Ví '" + wallet.getName() + "' có số dư rất thấp!");
                    }
                    break;
                case "WALLET_NEGATIVE_BALANCE":
                    if (data instanceof Wallet) {
                        Wallet wallet = (Wallet) data;
                        addNotification("Cảnh báo: Ví '" + wallet.getName() + "' đã bị thấu chi!");
                    }
                    break;
                case "TRANSACTION_ADDED_TO_WALLET":
                    if (data instanceof Transaction) {
                        Transaction transaction = (Transaction) data;
                        addNotification("Đã thêm giao dịch vào ví '" + transaction.getWallet().getName() + "'");
                        updateTransactionList(data);
                    }
                    break;
                default:
                    addNotification(message);
                    break;
            }
        });
    }
    
    private void updateTransactionList(Object data) {
        if (data instanceof Transaction && transactionList != null) {
            Transaction transaction = (Transaction) data;
            if (!transactionList.contains(transaction)) {
                transactionList.add(0, transaction); // Thêm vào đầu danh sách
            }
        }
    }
    
    private void refreshTransactionList() {
        // Sẽ được implement để refresh danh sách transaction từ database
        System.out.println("Refreshing transaction list...");
    }
      private void refreshBudgetList() {
        if (budgetList != null) {
            // Sẽ được implement để refresh danh sách budget từ database
            System.out.println("Refreshing budget list... (" + budgetList.size() + " items)");
        }
    }
    
    private void addNotification(String notification) {
        if (notificationHistory != null) {
            String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            notificationHistory.add(0, "[" + timestamp + "] " + notification);
            
            // Giới hạn số lượng thông báo (chỉ giữ 50 thông báo gần nhất)
            if (notificationHistory.size() > 50) {
                notificationHistory.remove(notificationHistory.size() - 1);
            }
        }
    }
    
    // Setter methods để inject dependencies
    public void setTransactionList(ObservableList<Transaction> transactionList) {
        this.transactionList = transactionList;
    }
    
    public void setBudgetList(ObservableList<Budget> budgetList) {
        this.budgetList = budgetList;
    }
    
    public void setNotificationHistory(ObservableList<String> notificationHistory) {
        this.notificationHistory = notificationHistory;
    }
}
