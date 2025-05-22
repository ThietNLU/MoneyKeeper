package ood.application.moneykeeper.model;

public interface ISubject {
    public void addObserver(IObserver observer);

    public void removeObserver(IObserver observer);

    public void notifyObservers(String message);
}
