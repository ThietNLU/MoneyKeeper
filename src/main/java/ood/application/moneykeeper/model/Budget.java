package ood.application.moneykeeper.model;

import lombok.Data;
import ood.application.moneykeeper.utils.DateTimeUtils;
import ood.application.moneykeeper.utils.UUIDUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Budget implements ISubject {
    private String id;
    private String name;
    private double limit;
    private double spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Category category;
    private List<IObserver> observers = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    public Budget(String name, double limit, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.limit = limit;
        this.spent = 0.0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
    }

    public Budget(String id, String name, double limit, double spent, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        this.id = id;
        this.name = name;
        this.limit = limit;
        this.spent = spent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.observers = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    @Override
    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        this.observers.remove(observer);
    }    @Override
    public void notifyObservers(NotificationData notificationData) {
        for (IObserver observer : observers) {
            observer.update(notificationData);
        }
    }

    public boolean isOverLimit() {
        return this.spent > this.limit;
    }
    
    public boolean isNearLimit() {
        return this.spent >= (this.limit * 0.8) && this.spent <= this.limit;
    }

    public String getInfo() {
        return this.name + "\n" + this.limit + "\n" + this.spent + "\n" + this.startDate
                + "\n" + this.endDate + "\n" + this.category.getName();
    }

    public void addTransaction(Transaction trans) {
        this.transactions.add(trans);
        processTrans(trans);
        
        // Notify observers about transaction addition
        NotificationData transactionNotification = new NotificationData(
            NotificationType.TRANSACTION_ADDED,
            "Transaction added to budget: " + this.name,
            "Budget",
            trans
        );
        notifyObservers(transactionNotification);
        
        // Check for budget alerts after adding transaction
        checkBudgetLimits();
    }

    public void processTrans(Transaction trans) {
        spent += trans.getAmount();
    }
    
    /**
     * Check budget limits and notify observers if necessary
     */
    private void checkBudgetLimits() {
        if (isOverLimit()) {
            NotificationData alertNotification = new NotificationData(
                NotificationType.BUDGET_ALERT,
                String.format("Budget '%s' is over limit! Spent: %.2f / Limit: %.2f", 
                             name, spent, limit),
                "Budget",
                this
            );
            notifyObservers(alertNotification);
        } else if (isNearLimit()) {
            NotificationData warningNotification = new NotificationData(
                NotificationType.WARNING,
                String.format("Budget '%s' is near limit (%.1f%%). Spent: %.2f / Limit: %.2f", 
                             name, (spent / limit) * 100, spent, limit),
                "Budget",
                this
            );
            notifyObservers(warningNotification);
        }
    }
    
    /**
     * Update budget spent amount and check limits
     */
    public void updateSpent(double newSpent) {
        this.spent = newSpent;
        checkBudgetLimits();
        
        // Notify about budget update
        NotificationData updateNotification = new NotificationData(
            NotificationType.INFO,
            String.format("Budget '%s' updated. Spent: %.2f / Limit: %.2f", 
                         name, spent, limit),
            "Budget",
            this
        );
        notifyObservers(updateNotification);
    }

    public String toString() {
        return "Id: " + id + "\nName: " + name + "\nLimit: " + limit + "\nSpent: "
                + spent + "\nStart: " + DateTimeUtils.formatDefault(startDate) + "\nEnd: "
                + DateTimeUtils.formatDefault(endDate) + "\nCategory: " + category.getName();
    }
}
