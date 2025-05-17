package ood.application.moneykeeper.model;

public class NotificationObserver implements IObserver {
    private String message;

    public NotificationObserver(String message) {
        this.message = message;
    }

    @Override
    public void update(String message) {
        System.out.println("[" + message + "] " + message);
    }

}
