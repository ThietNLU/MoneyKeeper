package ood.application.moneykeeper.model;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.collections.ObservableList;

/**
 * Observer that handles UI notifications for JavaFX applications
 */
public class UINotificationObserver implements IObserver {
    private Label statusLabel;
    private ObservableList<String> notificationList;
    private boolean showAlerts;

    public UINotificationObserver() {
        this.showAlerts = true;
    }

    public UINotificationObserver(Label statusLabel) {
        this.statusLabel = statusLabel;
        this.showAlerts = false;
    }

    public UINotificationObserver(ObservableList<String> notificationList) {
        this.notificationList = notificationList;
        this.showAlerts = false;
    }

    public UINotificationObserver(Label statusLabel, ObservableList<String> notificationList) {
        this.statusLabel = statusLabel;
        this.notificationList = notificationList;
        this.showAlerts = false;
    }

    @Override
    public void update(NotificationData notificationData) {
        // Run on JavaFX Application Thread
        Platform.runLater(() -> {
            // Update status label if available
            if (statusLabel != null) {
                statusLabel.setText(notificationData.getMessage());
                
                // Set text color based on notification type
                switch (notificationData.getType()) {
                    case ERROR:
                        statusLabel.setStyle("-fx-text-fill: red;");
                        break;
                    case WARNING:
                    case BUDGET_ALERT:
                    case LOW_BALANCE:
                        statusLabel.setStyle("-fx-text-fill: orange;");
                        break;
                    case INFO:
                    case WALLET_UPDATE:
                    case TRANSACTION_ADDED:
                        statusLabel.setStyle("-fx-text-fill: green;");
                        break;
                    default:
                        statusLabel.setStyle("-fx-text-fill: black;");
                }
            }

            // Add to notification list if available
            if (notificationList != null) {
                notificationList.add(0, notificationData.toString()); // Add to top
                
                // Keep only last 50 notifications
                if (notificationList.size() > 50) {
                    notificationList.remove(notificationList.size() - 1);
                }
            }

            // Show alert for critical notifications
            if (showAlerts && shouldShowAlert(notificationData.getType())) {
                showAlert(notificationData);
            }
        });
    }

    private boolean shouldShowAlert(NotificationType type) {
        return type == NotificationType.ERROR || 
               type == NotificationType.BUDGET_ALERT || 
               type == NotificationType.LOW_BALANCE;
    }

    private void showAlert(NotificationData notificationData) {
        Alert.AlertType alertType;
        
        switch (notificationData.getType()) {
            case ERROR:
                alertType = Alert.AlertType.ERROR;
                break;
            case WARNING:
            case BUDGET_ALERT:
            case LOW_BALANCE:
                alertType = Alert.AlertType.WARNING;
                break;
            default:
                alertType = Alert.AlertType.INFORMATION;
        }

        Alert alert = new Alert(alertType);
        alert.setTitle(notificationData.getType().getType());
        alert.setHeaderText(notificationData.getSource());
        alert.setContentText(notificationData.getMessage());
        alert.show();
    }

    public void setShowAlerts(boolean showAlerts) {
        this.showAlerts = showAlerts;
    }
}
