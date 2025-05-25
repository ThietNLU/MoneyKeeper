package ood.application.moneykeeper.model;

import java.util.function.Consumer;

/**
 * Specialized observer for wallet-related notifications
 */
public class WalletUpdateObserver implements IObserver {
    private Consumer<Wallet> onBalanceChanged;
    private Consumer<Wallet> onLowBalance;
    private Consumer<Transaction> onTransactionAdded;
    private double lowBalanceThreshold = 1000.0; // Default low balance threshold

    public WalletUpdateObserver() {
    }

    public WalletUpdateObserver(Consumer<Wallet> onBalanceChanged) {
        this.onBalanceChanged = onBalanceChanged;
    }

    public WalletUpdateObserver(Consumer<Wallet> onBalanceChanged, 
                               Consumer<Wallet> onLowBalance,
                               Consumer<Transaction> onTransactionAdded) {
        this.onBalanceChanged = onBalanceChanged;
        this.onLowBalance = onLowBalance;
        this.onTransactionAdded = onTransactionAdded;
    }

    @Override
    public void update(NotificationData notificationData) {
        switch (notificationData.getType()) {
            case WALLET_UPDATE:
                if (notificationData.getData() instanceof Wallet) {
                    Wallet wallet = (Wallet) notificationData.getData();
                    if (onBalanceChanged != null) {
                        onBalanceChanged.accept(wallet);
                    }
                }
                break;
                
            case LOW_BALANCE:
                if (notificationData.getData() instanceof Wallet) {
                    Wallet wallet = (Wallet) notificationData.getData();
                    if (onLowBalance != null) {
                        onLowBalance.accept(wallet);
                    }
                }
                break;
                
            case TRANSACTION_ADDED:
                if (notificationData.getData() instanceof Transaction) {
                    Transaction transaction = (Transaction) notificationData.getData();
                    if (onTransactionAdded != null) {
                        onTransactionAdded.accept(transaction);
                    }
                }
                break;
        }
        
        // Print to console for debugging
        System.out.println("WALLET OBSERVER: " + notificationData.toString());
    }

    public boolean isLowBalance(Wallet wallet) {
        return wallet.getBalance() < lowBalanceThreshold;
    }

    // Setters for callback functions
    public void setOnBalanceChanged(Consumer<Wallet> onBalanceChanged) {
        this.onBalanceChanged = onBalanceChanged;
    }

    public void setOnLowBalance(Consumer<Wallet> onLowBalance) {
        this.onLowBalance = onLowBalance;
    }

    public void setOnTransactionAdded(Consumer<Transaction> onTransactionAdded) {
        this.onTransactionAdded = onTransactionAdded;
    }

    public void setLowBalanceThreshold(double lowBalanceThreshold) {
        this.lowBalanceThreshold = lowBalanceThreshold;
    }

    public double getLowBalanceThreshold() {
        return lowBalanceThreshold;
    }
}
