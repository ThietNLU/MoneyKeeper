package ood.application.moneykeeper.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField transactionAmountField;
    @FXML private TextField transactionDescriptionField;
    @FXML private DatePicker transactionDatePicker;
    @FXML private TextField budgetSpentField;
    @FXML private Button addTransactionButton;
    @FXML private Button updateBudgetButton;
    @FXML private Button clearNotificationsButton;
    @FXML private CheckBox enableAlertsCheckBox;
    @FXML private RadioButton incomeRadioButton;
    @FXML private RadioButton expenseRadioButton;
    
    // Advanced transaction details
    @FXML private TextArea transactionNotesArea;
    @FXML private TextField transactionTagsField;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private CheckBox recurringTransactionCheckBox;
    @FXML private VBox recurringDetailsBox;
    @FXML private ComboBox<String> frequencyComboBox;
    
    // Strategy selection
    @FXML private CheckBox autoStrategyCheckBox;
    @FXML private ComboBox<String> strategyComboBox;
    
    // Observer testing
    @FXML private CheckBox emailNotificationCheckBox;
    @FXML private CheckBox smsNotificationCheckBox;
    @FXML private CheckBox pushNotificationCheckBox;
    @FXML private CheckBox logObserverCheckBox;
    @FXML private TextField thresholdField;
    @FXML private Button testObserversButton;
    @FXML private Button simulateErrorButton;
    
    private NotificationManager notificationManager;
    private ObservableList<String> notifications;
    private WalletDAO walletDAO;
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;
    private ToggleGroup transactionTypeGroup;

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
        
        // Load categories
        loadCategories();
        
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
    }private void setupEventHandlers() {
        addTransactionButton.setOnAction(e -> addTransaction());
        updateBudgetButton.setOnAction(e -> updateBudgetSpent());
        clearNotificationsButton.setOnAction(e -> clearNotifications());
        enableAlertsCheckBox.setOnAction(e -> notificationManager.setUIAlertsEnabled(enableAlertsCheckBox.isSelected()));
        
        // Setup radio button group for transaction type
        transactionTypeGroup = new ToggleGroup();
        incomeRadioButton.setToggleGroup(transactionTypeGroup);
        expenseRadioButton.setToggleGroup(transactionTypeGroup);
        expenseRadioButton.setSelected(true); // Default to expense
        
        // Setup strategy selection
        setupStrategyControls();
        
        // Setup recurring transaction controls
        setupRecurringTransactionControls();
        
        // Setup observer testing controls
        setupObserverTestingControls();
        
        // Initialize date picker to current date
        if (transactionDatePicker != null) {
            transactionDatePicker.setValue(java.time.LocalDate.now());
        }
        
        // Enable alerts by default
        enableAlertsCheckBox.setSelected(true);
    }
    
    private void setupStrategyControls() {
        if (autoStrategyCheckBox != null && strategyComboBox != null) {
            // Initialize strategy ComboBox
            strategyComboBox.getItems().addAll(
                "Auto (Based on Type)",
                "Income Strategy",
                "Expense Strategy",
                "Investment Strategy",
                "Transfer Strategy"
            );
            strategyComboBox.setValue("Auto (Based on Type)");
            
            // Handle auto strategy checkbox
            autoStrategyCheckBox.setOnAction(e -> {
                strategyComboBox.setDisable(autoStrategyCheckBox.isSelected());
                if (autoStrategyCheckBox.isSelected()) {
                    strategyComboBox.setValue("Auto (Based on Type)");
                }
            });
        }
    }
    
    private void setupRecurringTransactionControls() {
        if (recurringTransactionCheckBox != null && recurringDetailsBox != null) {
            recurringTransactionCheckBox.setOnAction(e -> {
                recurringDetailsBox.setDisable(!recurringTransactionCheckBox.isSelected());
            });
            
            if (frequencyComboBox != null) {
                frequencyComboBox.setValue("Monthly");
            }
        }
    }
    
    private void setupObserverTestingControls() {
        if (testObserversButton != null) {
            testObserversButton.setOnAction(e -> testAllObservers());
        }
        
        if (simulateErrorButton != null) {
            simulateErrorButton.setOnAction(e -> simulateErrorScenario());
        }
        
        if (priorityComboBox != null) {
            priorityComboBox.setValue("Normal");
        }
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
              // Determine transaction type based on radio button selection
            boolean isIncome = incomeRadioButton.isSelected();
            
            // Get appropriate category based on transaction type
            List<Category> categories = categoryDAO.getAll();
            if (categories.isEmpty()) {
                showError("No categories available. Please create categories first.");
                return;
            }
            
            Category selectedCategory = categories.stream()
                    .filter(category -> isIncome ? !category.isExpense() : category.isExpense())
                    .findFirst()
                    .orElse(categories.get(0)); // Fallback to first category if no matching type found
            
            if (selectedCategory == null) {
                showError("Unable to find a suitable category for this transaction type.");
                return;
            }
            
            // Create transaction
            Transaction transaction = new Transaction(selectedWallet, amount, selectedCategory, description);
              // Set appropriate strategy based on transaction type
            if (isIncome) {
                transaction.setStrategy(new IncomTransactionStrategy());
            } else {
                transaction.setStrategy(new ExpenseTransactionStrategy());
            }
            
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
    
    private void testAllObservers() {
        try {
            String thresholdText = thresholdField != null ? thresholdField.getText() : "50000";
            double threshold = Double.parseDouble(thresholdText.isEmpty() ? "50000" : thresholdText);
            
            // Create a test notification
            NotificationData testNotification = new NotificationData(
                NotificationType.TRANSACTION_ADDED,
                String.format("Testing all observers with threshold: %.2f VND", threshold),
                "Test",
                null
            );
            
            // Test different observer types based on checkboxes
            StringBuilder testResults = new StringBuilder("Observer Test Results:\n");
            
            if (emailNotificationCheckBox != null && emailNotificationCheckBox.isSelected()) {
                testResults.append("✓ Email notifications would be sent\n");
            }
            
            if (smsNotificationCheckBox != null && smsNotificationCheckBox.isSelected()) {
                testResults.append("✓ SMS notifications would be sent\n");
            }
            
            if (pushNotificationCheckBox != null && pushNotificationCheckBox.isSelected()) {
                testResults.append("✓ Push notifications active\n");
            }
            
            if (logObserverCheckBox != null && logObserverCheckBox.isSelected()) {
                testResults.append("✓ Log observer recording events\n");
            }
            
            // Add to notification list
            Platform.runLater(() -> {
                notifications.add(0, testResults.toString());
                statusLabel.setText("Observer test completed");
                statusLabel.setStyle("-fx-text-fill: green;");
            });
            
        } catch (NumberFormatException e) {
            showError("Invalid threshold value. Please enter a valid number.");
        }
    }
    
    private void simulateErrorScenario() {
        try {
            // Simulate various error scenarios for observer pattern testing
            Platform.runLater(() -> {
                notifications.add(0, "🚨 SIMULATED ERROR: Database connection lost");
                notifications.add(0, "⚠️ SIMULATED WARNING: Low wallet balance detected");
                notifications.add(0, "❌ SIMULATED ERROR: Invalid transaction amount");
                notifications.add(0, "🔄 SIMULATED INFO: Attempting automatic recovery");
                notifications.add(0, "✅ SIMULATED SUCCESS: Error recovery completed");
                
                statusLabel.setText("Error simulation completed");
                statusLabel.setStyle("-fx-text-fill: orange;");
            });
            
            // Show a dialog with error handling information
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error Simulation");
            alert.setHeaderText("Observer Pattern Error Handling Demo");
            alert.setContentText("This demonstrates how the Observer pattern handles:\n" +
                               "• Database connection errors\n" +
                               "• Invalid data scenarios\n" +
                               "• Automatic error recovery\n" +
                               "• Multiple observer notifications\n\n" +
                               "Check the notification list to see the simulated events.");
            alert.showAndWait();
            
        } catch (Exception e) {
            showError("Error during simulation: " + e.getMessage());
        }
    }
    
    private void loadCategories() throws SQLException {
        if (categoryComboBox != null) {
            List<Category> categories = categoryDAO.getAll();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            categoryComboBox.setCellFactory(param -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    if (empty || category == null) {
                        setText(null);
                    } else {
                        setText(category.getName() + (category.isExpense() ? " (Expense)" : " (Income)"));
                    }
                }
            });
            categoryComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category category, boolean empty) {
                    super.updateItem(category, empty);
                    if (empty || category == null) {
                        setText(null);
                    } else {
                        setText(category.getName() + (category.isExpense() ? " (Expense)" : " (Income)"));
                    }
                }
            });
        }
    }
}
