package ood.application.moneykeeper.observer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.Wallet;

public class NotificationObserver implements Observer {
    private String observerName;

    public NotificationObserver(String name) {
        this.observerName = name;
    }

    @Override
    public void update(String message, Object data) {
        System.out.println("[" + observerName + "] Nhận thông báo: " + message);

        // Xử lý các loại thông báo khác nhau
        switch (message) {
            case "BUDGET_EXCEEDED":
                handleBudgetExceeded(data);
                break;
            case "BUDGET_WARNING":
                handleBudgetWarning(data);
                break;
            case "TRANSACTION_ADDED":
                handleTransactionAdded(data);
                break;
            case "WALLET_BALANCE_UPDATED":
                handleWalletBalanceUpdated(data);
                break;
            case "WALLET_LOW_BALANCE":
                handleWalletLowBalance(data);
                break;
            case "WALLET_CRITICALLY_LOW":
                handleWalletCriticallyLow(data);
                break;
            case "WALLET_NEGATIVE_BALANCE":
                handleWalletNegativeBalance(data);
                break;
            case "TRANSACTION_ADDED_TO_WALLET":
                handleTransactionAddedToWallet(data);
                break;
            default:
                handleGenericNotification(message, data);
                break;
        }
    }

    private void handleBudgetExceeded(Object data) {
        if (data instanceof Budget) {
            Budget budget = (Budget) data;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo ngân sách");
                alert.setHeaderText("Ngân sách đã vượt quá giới hạn!");
                alert.setContentText(String.format(
                        "Ngân sách '%s' đã vượt quá giới hạn.\n" +
                                "Giới hạn: %,.0f VND\n" +
                                "Đã chi: %,.0f VND\n" +
                                "Vượt quá: %,.0f VND",
                        budget.getName(),
                        budget.getLimit(),
                        budget.getSpent(),
                        budget.getSpent() - budget.getLimit()
                ));
                alert.showAndWait();
            });
        }
    }

    private void handleBudgetWarning(Object data) {
        if (data instanceof Budget) {
            Budget budget = (Budget) data;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cảnh báo ngân sách");
                alert.setHeaderText("Ngân sách sắp đạt giới hạn!");
                alert.setContentText(String.format(
                        "Ngân sách '%s' đã sử dụng %.1f%% giới hạn.\n" +
                                "Giới hạn: %,.0f VND\n" +
                                "Đã chi: %,.0f VND",
                        budget.getName(),
                        (budget.getSpent() / budget.getLimit()) * 100,
                        budget.getLimit(),
                        budget.getSpent()
                ));
                alert.showAndWait();
            });
        }
    }

    private void handleTransactionAdded(Object data) {
        if (data instanceof Transaction) {
            Transaction transaction = (Transaction) data;
            System.out.println("Giao dịch mới: " + transaction.getDescription() +
                    " - " + String.format("%,.0f VND", transaction.getAmount()));
        }
    }

    private void handleWalletBalanceUpdated(Object data) {
        System.out.println("Số dư ví đã được cập nhật: " + data);
    }

    private void handleWalletLowBalance(Object data) {
        if (data instanceof Wallet) {
            Wallet wallet = (Wallet) data;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cảnh báo số dư");
                alert.setHeaderText("Số dư ví thấp!");
                alert.setContentText(String.format(
                        "Ví '%s' có số dư thấp.\n" +
                                "Số dư hiện tại: %,.0f VND\n" +
                                "Bạn nên nạp thêm tiền vào ví.",
                        wallet.getName(),
                        wallet.getBalance()
                ));
                alert.showAndWait();
            });
        }
    }

    private void handleWalletCriticallyLow(Object data) {
        if (data instanceof Wallet) {
            Wallet wallet = (Wallet) data;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo nghiêm trọng");
                alert.setHeaderText("Số dư ví rất thấp!");
                alert.setContentText(String.format(
                        "Ví '%s' có số dư rất thấp.\n" +
                                "Số dư hiện tại: %,.0f VND\n" +
                                "Vui lòng nạp tiền ngay để tránh gián đoạn giao dịch.",
                        wallet.getName(),
                        wallet.getBalance()
                ));
                alert.showAndWait();
            });
        }
    }

    private void handleWalletNegativeBalance(Object data) {
        if (data instanceof Wallet) {
            Wallet wallet = (Wallet) data;
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Cảnh báo thấu chi");
                alert.setHeaderText("Số dư ví âm!");
                alert.setContentText(String.format(
                        "Ví '%s' đã bị thấu chi.\n" +
                                "Số dư hiện tại: %,.0f VND\n" +
                                "Vui lòng nạp tiền ngay lập tức!",
                        wallet.getName(),
                        wallet.getBalance()
                ));
                alert.showAndWait();
            });
        }
    }

    private void handleTransactionAddedToWallet(Object data) {
        if (data instanceof Transaction) {
            Transaction transaction = (Transaction) data;
            System.out.println("Giao dịch mới được thêm vào ví: " + transaction.getDescription() +
                    " - " + String.format("%,.0f VND", transaction.getAmount()));
        }
    }

    private void handleGenericNotification(String message, Object data) {
        System.out.println("Thông báo chung: " + message +
                (data != null ? " - Dữ liệu: " + data.toString() : ""));
    }
}
