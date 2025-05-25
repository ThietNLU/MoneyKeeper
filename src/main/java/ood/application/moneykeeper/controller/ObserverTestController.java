package ood.application.moneykeeper.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ood.application.moneykeeper.dao.BudgetDAO;
import ood.application.moneykeeper.dao.WalletDAO;
import ood.application.moneykeeper.dao.CategoryDAO;
import ood.application.moneykeeper.model.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Demo controller to showcase Observer pattern functionality
 */
public class ObserverTestController implements Initializable {
    
    @FXML private ListView<String> notificationListView;
    @FXML private Label statusLabel;
    @FXML private ComboBox<Wallet> walletComboBox;
    @FXML private ComboBox<Budget> budgetComboBox;
    @FXML private TextField transactionAmountField;
    @FXML private TextField transactionDescriptionField;
    @FXML private TextField budgetSpentField;
    @FXML private Button addTransactionButton;
    @FXML private Button updateBudgetButton;
    @FXML private Button clearNotificationsButton;
    @FXML private CheckBox enableAlertsCheckBox;
    
    private NotificationManager notificationManager;
    private ObservableList<String> notifications;
    private WalletDAO walletDAO;
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize DAOs
            walletDAO = new WalletDAO();
            budgetDAO = new BudgetDAO();
            categoryDAO = new CategoryDAO();
            
            // Initialize notification system
            notifications = FXCollections.observableArrayList();
            notificationListView.setItems(notifications);
            
            notificationManager = NotificationManager.getInstance();
            notificationManager.initializeUIObserver(statusLabel);
            
            // Add custom notification observer
            notificationManager.addGlobalObserver(notificationData -> 
                Platform.runLater(() -> notifications.add(0, notificationData.toString()))
            );
            
            // Load data
            loadWalletsAndBudgets();
            setupEventHandlers();
            
            // Setup observers for existing data
            setupExistingObservers();
            
        } catch (SQLException e) {
            showError("Error initializing demo: " + e.getMessage());
        }
    }
    
    private void loadWalletsAndBudgets() throws SQLException {
        // Load wallets
        List<Wallet> wallets = walletDAO.getAll();
        walletComboBox.setItems(FXCollections.observableArrayList(wallets));
        walletComboBox.setCellFactory(param -> new ListCell<Wallet>() {
            @Override
            protected void updateItem(Wallet wallet, boolean empty) {
                super.updateItem(wallet, empty);
                setText(empty || wallet == null ? null : wallet.getName() + " (" + wallet.getBalance() + " VND)");
            }
        });
        walletComboBox.setButtonCell(new ListCell<Wallet>() {
            @Override
            protected void updateItem(Wallet wallet, boolean empty) {
                super.updateItem(wallet, empty);
                setText(empty || wallet == null ? null : wallet.getName() + " (" + wallet.getBalance() + " VND)");
            }
        });
        
        // Load budgets
        List<Budget> budgets = budgetDAO.getAll();
        budgetComboBox.setItems(FXCollections.observableArrayList(budgets));
        budgetComboBox.setCellFactory(param -> new ListCell<Budget>() {
            @Override
            protected void updateItem(Budget budget, boolean empty) {
                super.updateItem(budget, empty);
                setText(empty || budget == null ? null : 
                       budget.getName() + " (" + budget.getSpent() + "/" + budget.getLimit() + " VND)");
            }
        });
        budgetComboBox.setButtonCell(new ListCell<Budget>() {
            @Override
            protected void updateItem(Budget budget, boolean empty) {
                super.updateItem(budget, empty);
                setText(empty || budget == null ? null : 
                       budget.getName() + " (" + budget.getSpent() + "/" + budget.getLimit() + " VND)");
            }
        });
    }
    
    private void setupEventHandlers() {
        addTransactionButton.setOnAction(e -> addTransaction());
        updateBudgetButton.setOnAction(e -> updateBudgetSpent());
        clearNotificationsButton.setOnAction(e -> clearNotifications());
        enableAlertsCheckBox.setOnAction(e -> notificationManager.setUIAlertsEnabled(enableAlertsCheckBox.isSelected()));
        
        // Enable alerts by default
        enableAlertsCheckBox.setSelected(true);
    }
    
    private void setupExistingObservers() throws SQLException {
        // Setup observers for all existing wallets
        List<Wallet> wallets = walletDAO.getAll();
        for (Wallet wallet : wallets) {
            notificationManager.setupWalletObservers(wallet);
        }
        
        // Setup observers for all existing budgets
        List<Budget> budgets = budgetDAO.getAll();
        for (Budget budget : budgets) {
            notificationManager.setupBudgetObservers(budget);
        }
    }
    
    @FXML
    private void addTransaction() {
        try {
            Wallet selectedWallet = walletComboBox.getValue();
            if (selectedWallet == null) {
                showError("Please select a wallet");
                return;
            }
            
            String amountText = transactionAmountField.getText().trim();
            String description = transactionDescriptionField.getText().trim();
            
            if (amountText.isEmpty() || description.isEmpty()) {
                showError("Please fill in amount and description");
                return;
            }
            
            double amount = Double.parseDouble(amountText);
            
            // Get a default category (expense)
            List<Category> categories = categoryDAO.getAll();
            Category expenseCategory = categories.stream()
                    .filter(Category::isExpense)
                    .findFirst()
                    .orElse(categories.get(0));
            
            // Create transaction
            Transaction transaction = new Transaction(selectedWallet, amount, expenseCategory, description);
            transaction.setStrategy(new ExpenseTransactionStrategy());
            
            // Process transaction (this will trigger observers)
            selectedWallet.addTransaction(transaction);
            
            // Clear fields
            transactionAmountField.clear();
            transactionDescriptionField.clear();
            
            // Refresh combo box display
            walletComboBox.getSelectionModel().clearSelection();
            walletComboBox.getSelectionModel().select(selectedWallet);
            
        } catch (NumberFormatException e) {
            showError("Please enter a valid amount");
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }
    
    @FXML
    private void updateBudgetSpent() {
        try {
            Budget selectedBudget = budgetComboBox.getValue();
            if (selectedBudget == null) {
                showError("Please select a budget");
                return;
            }
            
            String spentText = budgetSpentField.getText().trim();
            if (spentText.isEmpty()) {
                showError("Please enter spent amount");
                return;
            }
            
            double newSpent = Double.parseDouble(spentText);
            
            // Update budget (this will trigger observers)
            selectedBudget.updateSpent(newSpent);
            
            // Clear field
            budgetSpentField.clear();
            
            // Refresh combo box display
            budgetComboBox.getSelectionModel().clearSelection();
            budgetComboBox.getSelectionModel().select(selectedBudget);
            
        } catch (NumberFormatException e) {
            showError("Please enter a valid amount");
        }
    }
    
    @FXML
    private void clearNotifications() {
        notifications.clear();
        notificationManager.clearHistory();
        statusLabel.setText("Notifications cleared");
        statusLabel.setStyle("-fx-text-fill: blue;");
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
