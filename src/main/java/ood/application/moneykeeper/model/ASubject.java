package ood.application.moneykeeper.model;

import java.util.*;

public abstract class ASubject {
    protected List<IObserver> observers;

    public ASubject() {
        this.observers = new ArrayList<>();
    }

    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(IObserver observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers(String message) {
        for (IObserver observer : observers) {
            observer.update(message);
        }
    }
}
