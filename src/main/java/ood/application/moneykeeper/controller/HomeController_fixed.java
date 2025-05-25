package ood.application.moneykeeper.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import ood.application.moneykeeper.dao.BudgetDAO;
import ood.application.moneykeeper.dao.TransactionDAO;
import ood.application.moneykeeper.dao.WalletDAO;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.Wallet;
import ood.application.moneykeeper.model.NotificationManager;
import ood.application.moneykeeper.model.ExpenseTransactionStrategy;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController_fixed implements Initializable {

    // Balance Summary Section
    @FXML
    private Label totalBalanceLabel;
    @FXML
    private Label monthlyIncomeLabel;
    @FXML
    private Label monthlyExpenseLabel;
    @FXML
    private Label lastUpdateLabel;
    @FXML
    private Button refreshButton;

    // Recent Transactions Section
    @FXML
    private TableView<Transaction> recentTransactionsTable;
    @FXML
    private TableColumn<Transaction, String> transactionDateColumn;
    @FXML
    private TableColumn<Transaction, String> transactionDescriptionColumn;
    @FXML
    private TableColumn<Transaction, String> transactionWalletColumn;
    @FXML
    private TableColumn<Transaction, String> transactionAmountColumn;
    @FXML
    private TableColumn<Transaction, String> transactionTypeColumn;

    // Budget Overview Section
    @FXML
    private HBox budgetAlertBox;
    @FXML
    private Label budgetAlertLabel;
    @FXML
    private BarChart<String, Number> budgetChart;
    @FXML
    private CategoryAxis budgetXAxis;
    @FXML
    private NumberAxis budgetYAxis;
    @FXML
    private TableView<Budget> overLimitBudgetsTable;
    @FXML
    private TableColumn<Budget, String> budgetNameColumn;
    @FXML
    private TableColumn<Budget, String> budgetCategoryColumn;
    @FXML
    private TableColumn<Budget, String> budgetLimitColumn;
    @FXML
    private TableColumn<Budget, String> budgetSpentColumn;
    @FXML
    private TableColumn<Budget, String> budgetStatusColumn;

    // Observer Demo Section
    @FXML
    private ListView<String> notificationListView;
    @FXML
    private ComboBox<Wallet> walletComboBox;
    @FXML
    private ComboBox<Budget> budgetComboBox;
    @FXML
    private TextField transactionAmountField;
    @FXML
    private TextField transactionDescriptionField;
    @FXML
    private TextField budgetSpentField;
    @FXML
    private Button addTransactionButton;
    @FXML
    private Button updateBudgetButton;
    @FXML
    private Button clearNotificationsButton;
    @FXML
    private CheckBox enableAlertsCheckBox;

    // Data Access Objects
    private TransactionDAO transactionDAO;
    private WalletDAO walletDAO;
    private BudgetDAO budgetDAO;
    
    // Notification Management
    private NotificationManager notificationManager;
    @FXML
    private Label statusLabel; // Status label for notifications

    // Data
    private ObservableList<Transaction> recentTransactions;
    private ObservableList<Budget> overLimitBudgets;

    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialize DAOs
            transactionDAO = new TransactionDAO();
            walletDAO = new WalletDAO();
            budgetDAO = new BudgetDAO();

            // Initialize notification manager
            notificationManager = NotificationManager.getInstance();
            if (statusLabel != null) {
                notificationManager.initializeUIObserver(statusLabel);
            }

            // Initialize data lists
            recentTransactions = FXCollections.observableArrayList();
            overLimitBudgets = FXCollections.observableArrayList();

            // Setup tables
            setupRecentTransactionsTable();
            setupOverLimitBudgetsTable();
            setupBudgetChart();
            
            // Setup observer demo
            setupObserverDemo();

            // Load initial data
            refreshData();
            
            // Setup observers for existing data
            setupObservers();

        } catch (SQLException e) {
            showError("Lỗi khởi tạo: " + e.getMessage());
        }
    }

    private void setupRecentTransactionsTable() {
        // Setup columns
        transactionDateColumn.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(data.getValue().getDateTime().format(dateFormatter)));
        
        transactionDescriptionColumn.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(data.getValue().getDescription()));
        
        transactionWalletColumn.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(data.getValue().getWallet().getName()));
        
        transactionAmountColumn.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(formatMoney(data.getValue().getAmount())));
        
        transactionTypeColumn.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(data.getValue().getStrategy().isExpense() ? "Chi tiêu" : "Thu nhập"));

        // Set color formatting for amount column
        transactionAmountColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(amount);
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    if (transaction.getStrategy().isExpense()) {
                        setStyle("-fx-text-fill: #D32F2F;"); // Red for expenses
                    } else {
                        setStyle("-fx-text-fill: #2E7D32;"); // Green for income
                    }
                }
            }
        });

        recentTransactionsTable.setItems(recentTransactions);
    }

    private void setupOverLimitBudgetsTable() {
        // Setup columns
        budgetNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        budgetCategoryColumn.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(data.getValue().getCategory().getName()));
        
        budgetLimitColumn.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(formatMoney(data.getValue().getLimit())));
        
        budgetSpentColumn.setCellValueFactory(data -> 
            new ReadOnlyStringWrapper(formatMoney(data.getValue().getSpent())));
        
        budgetStatusColumn.setCellValueFactory(data -> {
            Budget budget = data.getValue();
            double percentage = (budget.getSpent() / budget.getLimit()) * 100;
            return new ReadOnlyStringWrapper(String.format("%.1f%%", percentage));
        });

        // Set color formatting for status column
        budgetStatusColumn.setCellFactory(column -> new TableCell<Budget, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    Budget budget = getTableView().getItems().get(getIndex());
                    if (budget.isOverLimit()) {
                        setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;"); // Red for over limit
                    } else {
                        setStyle("-fx-text-fill: #FF9800;"); // Orange for near limit
                    }
                }
            }
        });

        overLimitBudgetsTable.setItems(overLimitBudgets);
    }

    private void setupObserverDemo() {
        try {
            // Setup notification list
            if (notificationListView != null) {
                notificationListView.setItems(notificationManager.getNotificationHistory());
            }

            // Load wallets and budgets for combo boxes
            if (walletComboBox != null) {
                List<Wallet> wallets = walletDAO.getAll();
                walletComboBox.setItems(FXCollections.observableArrayList(wallets));
                walletComboBox.setConverter(new StringConverter<Wallet>() {
                    @Override
                    public String toString(Wallet wallet) {
                        return wallet != null ? wallet.getName() + " (" + formatMoney(wallet.getBalance()) + ")" : "";
                    }

                    @Override
                    public Wallet fromString(String string) {
                        return null;
                    }
                });
            }

            if (budgetComboBox != null) {
                List<Budget> budgets = budgetDAO.getAll();
                budgetComboBox.setItems(FXCollections.observableArrayList(budgets));
                budgetComboBox.setConverter(new StringConverter<Budget>() {
                    @Override
                    public String toString(Budget budget) {
                        return budget != null ? budget.getName() + " (" + formatMoney(budget.getSpent()) + "/" + formatMoney(budget.getLimit()) + ")" : "";
                    }

                    @Override
                    public Budget fromString(String string) {
                        return null;
                    }
                });
            }

            // Setup button actions
            if (addTransactionButton != null) {
                addTransactionButton.setOnAction(e -> handleAddTestTransaction());
            }
            
            if (updateBudgetButton != null) {
                updateBudgetButton.setOnAction(e -> handleUpdateTestBudget());
            }
            
            if (clearNotificationsButton != null) {
                clearNotificationsButton.setOnAction(e -> handleClearNotifications());
            }

            // Setup alerts checkbox
            if (enableAlertsCheckBox != null) {
                enableAlertsCheckBox.setOnAction(e -> {
                    notificationManager.setUIAlertsEnabled(enableAlertsCheckBox.isSelected());
                });
            }

        } catch (SQLException e) {
            showError("Lỗi setup observer demo: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddTestTransaction() {
        try {
            Wallet selectedWallet = walletComboBox.getValue();
            String amountText = transactionAmountField.getText();
            String description = transactionDescriptionField.getText();

            if (selectedWallet == null || amountText.isEmpty() || description.isEmpty()) {
                showAlert("Vui lòng điền đầy đủ thông tin giao dịch");
                return;
            }

            double amount = Double.parseDouble(amountText);
            
            // Create a test transaction (expense)
            Transaction testTransaction = new Transaction();
            testTransaction.setWallet(selectedWallet);
            testTransaction.setAmount(amount);
            testTransaction.setDescription(description);
            testTransaction.setDateTime(LocalDateTime.now());
            
            // Use expense strategy for demo
            testTransaction.setStrategy(new ExpenseTransactionStrategy());

            // Save transaction (this will trigger observers)
            transactionDAO.save(testTransaction);

            // Clear fields
            transactionAmountField.clear();
            transactionDescriptionField.clear();
            
            // Refresh data to show changes
            refreshData();
            
            if (enableAlertsCheckBox.isSelected()) {
                showAlert("Giao dịch đã được thêm thành công! Observer pattern đã thông báo các thay đổi.");
            }

        } catch (NumberFormatException e) {
            showAlert("Số tiền không hợp lệ");
        } catch (SQLException e) {
            showError("Lỗi thêm giao dịch: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTestBudget() {
        try {
            Budget selectedBudget = budgetComboBox.getValue();
            String spentText = budgetSpentField.getText();

            if (selectedBudget == null || spentText.isEmpty()) {
                showAlert("Vui lòng chọn ngân sách và nhập số tiền đã chi");
                return;
            }

            double newSpent = Double.parseDouble(spentText);
            
            // Update budget spent amount
            selectedBudget.setSpent(newSpent);
            
            // Save budget (this will trigger observers)
            budgetDAO.update(selectedBudget);

            // Clear field
            budgetSpentField.clear();
            
            // Refresh data to show changes
            refreshData();
            
            // Refresh combo boxes
            setupObserverDemo();
            
            if (enableAlertsCheckBox.isSelected()) {
                showAlert("Ngân sách đã được cập nhật! Observer pattern đã thông báo các thay đổi.");
            }

        } catch (NumberFormatException e) {
            showAlert("Số tiền không hợp lệ");
        } catch (SQLException e) {
            showError("Lỗi cập nhật ngân sách: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearNotifications() {
        notificationManager.clearHistory();
        showAlert("Đã xóa tất cả thông báo");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupBudgetChart() {
        budgetChart.setTitle("Tình trạng ngân sách");
        budgetXAxis.setLabel("Hạng mục");
        budgetYAxis.setLabel("Số tiền (VND)");
    }

    @FXML
    private void handleRefresh() {
        refreshData();
    }

    private void refreshData() {
        try {
            loadBalanceSummary();
            loadRecentTransactions();
            loadBudgetOverview();
            updateLastUpdateTime();
        } catch (SQLException e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void loadBalanceSummary() throws SQLException {
        // Calculate total balance from all wallets
        List<Wallet> wallets = walletDAO.getAll();
        double totalBalance = wallets.stream().mapToDouble(Wallet::getBalance).sum();
        totalBalanceLabel.setText(formatMoney(totalBalance));

        // Calculate monthly income and expense
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);

        List<Transaction> monthlyTransactions = transactionDAO.getTransactionsByDateRange(startOfMonth, endOfMonth);
        
        double monthlyIncome = monthlyTransactions.stream()
            .filter(t -> !t.getStrategy().isExpense())
            .mapToDouble(Transaction::getAmount)
            .sum();
        
        double monthlyExpense = monthlyTransactions.stream()
            .filter(t -> t.getStrategy().isExpense())
            .mapToDouble(Transaction::getAmount)
            .sum();

        monthlyIncomeLabel.setText(formatMoney(monthlyIncome));
        monthlyExpenseLabel.setText(formatMoney(monthlyExpense));
    }

    private void loadRecentTransactions() throws SQLException {
        // Get last 10 transactions
        List<Transaction> allTransactions = transactionDAO.getAll();
        
        // Sort by date descending and take first 10
        List<Transaction> recent = allTransactions.stream()
            .sorted((t1, t2) -> t2.getDateTime().compareTo(t1.getDateTime()))
            .limit(10)
            .toList();

        recentTransactions.clear();
        recentTransactions.addAll(recent);
    }

    private void loadBudgetOverview() throws SQLException {
        // Load over-limit budgets
        List<Budget> overLimit = budgetDAO.getOverLimitBudgets();
        overLimitBudgets.clear();
        overLimitBudgets.addAll(overLimit);

        // Show/hide alert box
        if (!overLimit.isEmpty()) {
            budgetAlertBox.setVisible(true);
            budgetAlertBox.setManaged(true);
            budgetAlertLabel.setText(String.format("Có %d ngân sách vượt giới hạn!", overLimit.size()));
        } else {
            budgetAlertBox.setVisible(false);
            budgetAlertBox.setManaged(false);
        }

        // Update budget chart
        updateBudgetChart();
    }

    private void updateBudgetChart() throws SQLException {
        List<Budget> activeBudgets = budgetDAO.getActiveBudgets();
        
        XYChart.Series<String, Number> limitSeries = new XYChart.Series<>();
        limitSeries.setName("Giới hạn");
        
        XYChart.Series<String, Number> spentSeries = new XYChart.Series<>();
        spentSeries.setName("Đã chi");

        for (Budget budget : activeBudgets) {
            String categoryName = budget.getCategory().getName();
            limitSeries.getData().add(new XYChart.Data<>(categoryName, budget.getLimit()));
            spentSeries.getData().add(new XYChart.Data<>(categoryName, budget.getSpent()));
        }

        budgetChart.getData().clear();
        if (!limitSeries.getData().isEmpty()) {
            budgetChart.getData().add(limitSeries);
        }
        if (!spentSeries.getData().isEmpty()) {
            budgetChart.getData().add(spentSeries);
        }
    }

    private void updateLastUpdateTime() {
        LocalDateTime now = LocalDateTime.now();
        lastUpdateLabel.setText(now.format(dateFormatter));
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f VND", amount);
    }

    /**
     * Setup observers for all wallets and budgets
     */
    private void setupObservers() {
        try {
            // Setup observers for all wallets
            List<Wallet> wallets = walletDAO.getAll();
            for (Wallet wallet : wallets) {
                notificationManager.setupWalletObservers(wallet);
            }

            // Setup observers for all budgets
            List<Budget> budgets = budgetDAO.getAll();
            for (Budget budget : budgets) {
                notificationManager.setupBudgetObservers(budget);
            }

        } catch (SQLException e) {
            showError("Lỗi setup observers: " + e.getMessage());
        }
    }

    /**
     * Add observers to a new wallet
     */
    public void addWalletObservers(Wallet wallet) {
        notificationManager.setupWalletObservers(wallet);
    }

    /**
     * Add observers to a new budget
     */
    public void addBudgetObservers(Budget budget) {
        notificationManager.setupBudgetObservers(budget);
    }

    /**
     * Get notification manager instance
     */
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
