package ood.application.moneykeeper.model;

import java.util.function.Consumer;

/**
 * Specialized observer for budget-related alerts and notifications
 */
public class BudgetAlertObserver implements IObserver {
    private Consumer<Budget> onBudgetOverLimit;
    private Consumer<Budget> onBudgetNearLimit; // 80% of limit
    private Consumer<Budget> onBudgetUpdated;
    private double warningThreshold = 0.8; // 80%

    public BudgetAlertObserver() {
    }

    public BudgetAlertObserver(Consumer<Budget> onBudgetOverLimit) {
        this.onBudgetOverLimit = onBudgetOverLimit;
    }

    public BudgetAlertObserver(Consumer<Budget> onBudgetOverLimit, 
                              Consumer<Budget> onBudgetNearLimit,
                              Consumer<Budget> onBudgetUpdated) {
        this.onBudgetOverLimit = onBudgetOverLimit;
        this.onBudgetNearLimit = onBudgetNearLimit;
        this.onBudgetUpdated = onBudgetUpdated;
    }

    @Override
    public void update(NotificationData notificationData) {
        if (notificationData.getData() instanceof Budget) {
            Budget budget = (Budget) notificationData.getData();
            
            switch (notificationData.getType()) {
                case BUDGET_ALERT:
                    if (budget.isOverLimit() && onBudgetOverLimit != null) {
                        onBudgetOverLimit.accept(budget);
                    } else if (isNearLimit(budget) && onBudgetNearLimit != null) {
                        onBudgetNearLimit.accept(budget);
                    }
                    break;
                case INFO:
                    if (onBudgetUpdated != null) {
                        onBudgetUpdated.accept(budget);
                    }
                    break;
            }
        }
        
        // Print to console for debugging
        System.out.println("BUDGET OBSERVER: " + notificationData.toString());
    }

    private boolean isNearLimit(Budget budget) {
        return budget.getSpent() >= (budget.getLimit() * warningThreshold) && 
               budget.getSpent() <= budget.getLimit();
    }

    // Setters for callback functions
    public void setOnBudgetOverLimit(Consumer<Budget> onBudgetOverLimit) {
        this.onBudgetOverLimit = onBudgetOverLimit;
    }

    public void setOnBudgetNearLimit(Consumer<Budget> onBudgetNearLimit) {
        this.onBudgetNearLimit = onBudgetNearLimit;
    }

    public void setOnBudgetUpdated(Consumer<Budget> onBudgetUpdated) {
        this.onBudgetUpdated = onBudgetUpdated;
    }

    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }
}
