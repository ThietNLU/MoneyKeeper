package ood.application.moneykeeper.model;

public interface IObserver {
    /**
     * Update method with detailed notification data
     * @param notificationData The notification data containing type, message, source, etc.
     */
    void update(NotificationData notificationData);
    
    /**
     * Legacy update method for backward compatibility
     * @param message Simple string message
     */
    default void update(String message) {
        update(new NotificationData(NotificationType.INFO, message, "Unknown"));
    }
}
