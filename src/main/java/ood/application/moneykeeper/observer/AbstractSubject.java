package ood.application.moneykeeper.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract Subject class - triển khai cơ bản của Subject interface
 */
public abstract class AbstractSubject implements Subject {
    protected List<Observer> observers = new ArrayList<>();
    
    @Override
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(String message, Object data) {
        for (Observer observer : observers) {
            observer.update(message, data);
        }
    }
    
    /**
     * Thông báo với message đơn giản
     */
    protected void notifyObservers(String message) {
        notifyObservers(message, null);
    }
}
