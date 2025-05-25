package ood.application.moneykeeper.model;

/**
 * Enum defining different types of notifications in the system
 */
public enum NotificationType {
    INFO("INFO"),
    WARNING("WARNING"),
    ERROR("ERROR"),
    BUDGET_ALERT("BUDGET_ALERT"),
    WALLET_UPDATE("WALLET_UPDATE"),
    TRANSACTION_ADDED("TRANSACTION_ADDED"),
    LOW_BALANCE("LOW_BALANCE");

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}
