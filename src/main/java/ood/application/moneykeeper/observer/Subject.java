package ood.application.moneykeeper.observer;

import java.util.ArrayList;
import java.util.List;

public interface Subject {
    void addObserver(Observer observer);
    
    void removeObserver(Observer observer);
    
    void notifyObservers(String message, Object data);
}
