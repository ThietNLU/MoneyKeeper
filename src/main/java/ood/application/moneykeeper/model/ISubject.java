package ood.application.moneykeeper.model;

public interface ISubject {
    /**
     * Add an observer to the subject
     * @param observer The observer to add
     */
    void addObserver(IObserver observer);

    /**
     * Remove an observer from the subject
     * @param observer The observer to remove
     */
    void removeObserver(IObserver observer);

    /**
     * Notify all observers with detailed notification data
     * @param notificationData The notification data to send
     */
    void notifyObservers(NotificationData notificationData);
    
    /**
     * Legacy notify method for backward compatibility
     * @param message Simple string message
     */
    default void notifyObservers(String message) {
        notifyObservers(new NotificationData(NotificationType.INFO, message, this.getClass().getSimpleName()));
    }
}
