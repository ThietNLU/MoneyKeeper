package ood.application.moneykeeper.model;

/**
 * Basic notification observer that prints notifications to console
 */
public class NotificationObserver implements IObserver {
    private String observerName;

    public NotificationObserver() {
        this.observerName = "DefaultObserver";
    }

    public NotificationObserver(String observerName) {
        this.observerName = observerName;
    }

    @Override
    public void update(NotificationData notificationData) {
        System.out.println(String.format("[%s] %s", observerName, notificationData.toString()));
    }

    @Override
    public void update(String message) {
        System.out.println(String.format("[%s] %s", observerName, message));
    }

    public String getObserverName() {
        return observerName;
    }

    public void setObserverName(String observerName) {
        this.observerName = observerName;
    }
}
