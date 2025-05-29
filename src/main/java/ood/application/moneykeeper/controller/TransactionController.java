package ood.application.moneykeeper.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import ood.application.moneykeeper.dao.BudgetDAO;
import ood.application.moneykeeper.dao.CategoryDAO;
import ood.application.moneykeeper.dao.TransactionDAO;
import ood.application.moneykeeper.dao.WalletDAO;
import ood.application.moneykeeper.model.*;
import ood.application.moneykeeper.observer.ObserverManager;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    @FXML
    private Label totalExpenseLabel;
    @FXML
    private Label totalIncomeLabel;

    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private ComboBox<Wallet> walletComboBox;
    @FXML
    private TextField amountField;    @FXML
    private TextArea descriptionFields;
    @FXML
    private DatePicker transactionDate;
    @FXML
    private Spinner<Integer> hourSpinner;
    @FXML
    private Spinner<Integer> minuteSpinner;    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, LocalDateTime> dateColumn;
    @FXML
    private TableColumn<Transaction, String> descriptionColumn;
    @FXML
    private TableColumn<Transaction, Double> amountColumn;
    @FXML
    private TableColumn<Transaction, String> categoryColumn;
    @FXML
    private TableColumn<Transaction, String> walletColumn;
    @FXML
    private TableColumn<Transaction, Boolean> typeColumn;    private TransactionDAO transactionDAO;
    private CategoryDAO categoryDAO;
    private WalletDAO walletDAO;    private BudgetDAO budgetDAO;
    private ObservableList<Transaction> transactions;
    private Wallet selectedWallet; // Store the wallet passed from WalletController
    private Transaction selectedTransaction; // Track the selected transaction for editing

    @FXML
    private ComboBox<String> filterComboBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {        try {
            transactionDAO = new TransactionDAO();
            categoryDAO = new CategoryDAO();
            walletDAO = new WalletDAO();
            budgetDAO = new BudgetDAO();

            // Configure table columns
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
            if (descriptionColumn != null) {
                descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            }            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            categoryColumn.setCellValueFactory(cellData -> {
                Category category = cellData.getValue().getCategory();
                return new ReadOnlyStringWrapper(category != null ? category.getName() : "N/A");
            });
            if (walletColumn != null) {
                walletColumn.setCellValueFactory(cellData -> {
                    Transaction transaction = cellData.getValue();
                    if (transaction.getWallet() != null) {
                        return new ReadOnlyStringWrapper(transaction.getWallet().getName());
                    } else {
                        return new ReadOnlyStringWrapper("Không xác định");
                    }
                });
            }
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("expense"));

            // Configure cell factories for formatting
            dateColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDateTime date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setText(null);
                    } else {
                        setText(date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    }
                }
            });
            
            amountColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f", amount));
                    }
                }
            });
            
            typeColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Boolean isExpense, boolean empty) {
                    super.updateItem(isExpense, empty);
                    if (empty || isExpense == null) {
                        setText(null);
                    } else {
                        setText(isExpense ? "Chi tiêu" : "Thu nhập");
                    }
                }
            });            // Load initial data
            loadTransactions();
            loadCategories();
            loadWallets();            // Set up event handlers
            transactionTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {                        selectedTransaction = newSelection;
                        boolean isSelected = (newSelection != null);
                        editButton.setDisable(!isSelected);
                        deleteButton.setDisable(!isSelected);
                        
                        if (newSelection != null) {
                            populateFields(newSelection);
                            saveButton.setText("Cập nhật giao dịch");
                        } else {
                            saveButton.setText("Lưu giao dịch");
                        }
                    });
              
            saveButton.setOnAction(event -> addTransaction());
            clearButton.setOnAction(event -> clearFields());
            editButton.setOnAction(event -> addTransaction());
            deleteButton.setOnAction(event -> deleteTransaction());

        // Initialize filter button
        filterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Chi tiêu", "Thu nhập"));
        filterComboBox.getSelectionModel().selectFirst(); // Mặc định chọn "Tất cả"

        filterComboBox.setOnAction(event -> applyFilter());

        // Initialize search button
        searchButton.setOnAction(event -> searchTransactionByKeyword());

            transactionDate.setValue(LocalDate.now());
            
            // Set up time spinners
            hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalDateTime.now().getHour()));
            hourSpinner.setEditable(true);
            
            minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, LocalDateTime.now().getMinute()));
            minuteSpinner.setEditable(true);
            
            // If a wallet was pre-selected via initData, set it in the combo box
            if (selectedWallet != null && walletComboBox != null) {
                walletComboBox.setValue(selectedWallet);
            }
        } catch (SQLException e) {
            showError("Lỗi khi khởi tạo: " + e.getMessage());
        }
    }

    private void loadTransactions() throws SQLException {
        List<Transaction> transactionList = transactionDAO.getAll();
        transactions = FXCollections.observableArrayList(transactionList);
        transactionTable.setItems(transactions);
    }

    private void loadCategories() throws SQLException {
        List<Category> categories = categoryDAO.getAll();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
    }    private void loadWallets() throws SQLException {
        List<Wallet> wallets = walletDAO.getAll();
        walletComboBox.setItems(FXCollections.observableArrayList(wallets));
        
        // If a wallet was pre-selected via initData, set it in the combo box
        if (selectedWallet != null) {
            walletComboBox.setValue(selectedWallet);
        }
    }    private void populateFields(Transaction transaction) {
        amountField.setText(String.valueOf(transaction.getAmount()));
        descriptionFields.setText(transaction.getDescription());
        transactionDate.setValue(transaction.getDateTime().toLocalDate());
        hourSpinner.getValueFactory().setValue(transaction.getDateTime().getHour());
        minuteSpinner.getValueFactory().setValue(transaction.getDateTime().getMinute());
        categoryComboBox.setValue(transaction.getCategory());
        walletComboBox.setValue(transaction.getWallet());
    }    private void clearFields() {
        amountField.clear();
        descriptionFields.clear();
        transactionDate.setValue(LocalDate.now());
        hourSpinner.getValueFactory().setValue(LocalDateTime.now().getHour());
        minuteSpinner.getValueFactory().setValue(LocalDateTime.now().getMinute());
        categoryComboBox.getSelectionModel().clearSelection();
        walletComboBox.getSelectionModel().clearSelection();
        
        // Clear selection and reset button text
        transactionTable.getSelectionModel().clearSelection();
        selectedTransaction = null;
        saveButton.setText("Lưu giao dịch");
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }private void addTransaction() {
        if (validateInput()) {
            try {
                Wallet chosenWallet = walletComboBox.getValue();
                Category selectedCategory = categoryComboBox.getValue();
                double amount = Double.parseDouble(amountField.getText().trim());
                String description = descriptionFields.getText().trim();
                int hour = hourSpinner.getValue();
                int minute = minuteSpinner.getValue();
                  if (selectedTransaction != null) {
                    // Store old values for reversal
                    Wallet oldWallet = selectedTransaction.getWallet();
                    double oldAmount = selectedTransaction.getAmount();
                    Category oldCategory = selectedTransaction.getCategory();
                    
                    // THÊM: Đăng ký observers cho wallet cũ trước khi hoàn tác
                    ObserverManager.getInstance().registerWalletObservers(oldWallet);
                    
                    // Reverse the old transaction from wallet
                    if (oldCategory.isExpense()) {
                        // Add money back to wallet (reverse old expense)
                        oldWallet.income(oldAmount);
                    } else {
                        // Remove money from wallet (reverse old income)
                        oldWallet.expense(oldAmount);
                    }
                    
                    // Update existing transaction with new values
                    selectedTransaction.setAmount(amount);
                    selectedTransaction.setDescription(description);
                    selectedTransaction.setDateTime(transactionDate.getValue().atTime(hour, minute));
                    selectedTransaction.setCategory(selectedCategory);
                    selectedTransaction.setWallet(chosenWallet);
                    
                    // Automatically determine strategy based on Category's isExpense property
                    if (selectedCategory.isExpense()) {
                        selectedTransaction.setStrategy(new ood.application.moneykeeper.model.ExpenseTransactionStrategy());
                    } else {
                        selectedTransaction.setStrategy(new IncomeTransactionStrategy());
                    }
                    
                    // Update transaction in database
                    transactionDAO.update(selectedTransaction);
                    
                    // THÊM: Đăng ký observers cho wallet mới nếu khác wallet cũ
                    if (!chosenWallet.getId().equals(oldWallet.getId())) {
                        ObserverManager.getInstance().registerWalletObservers(chosenWallet);
                    }
                    
                    // Apply the new transaction to wallet
                    selectedTransaction.processWallet();
                      // Update wallet(s) in database
                    walletDAO.update(oldWallet);
                    if (!chosenWallet.getId().equals(oldWallet.getId())) {
                        walletDAO.update(chosenWallet);
                    }
                    
                    // Process budget updates for expense transactions
                    if (oldCategory.isExpense() || selectedCategory.isExpense()) {
                        List<Budget> budgets = budgetDAO.getAll();
                        
                        // Remove from old budget if it was an expense
                        if (oldCategory.isExpense()) {
                            for (Budget budget : budgets) {
                                if (budget.getCategory().getId().equals(oldCategory.getId())) {
                                    LocalDateTime oldTransactionDateTime = selectedTransaction.getDateTime();
                                    if (oldTransactionDateTime.isAfter(budget.getStartDate()) && 
                                        oldTransactionDateTime.isBefore(budget.getEndDate())) {
                                        
                                        // Remove old transaction from budget
                                        budget.removeTransaction(selectedTransaction);
                                        budgetDAO.update(budget);
                                    }
                                }
                            }
                        }
                        
                        // Add to new budget if it's an expense
                        if (selectedCategory.isExpense()) {
                            for (Budget budget : budgets) {
                                if (budget.getCategory().getId().equals(selectedCategory.getId())) {
                                    LocalDateTime newTransactionDateTime = selectedTransaction.getDateTime();
                                    if (newTransactionDateTime.isAfter(budget.getStartDate()) && 
                                        newTransactionDateTime.isBefore(budget.getEndDate())) {
                                        
                                        // THÊM: Đăng ký observers cho budget trước khi thêm transaction
                                        ObserverManager.getInstance().registerBudgetObservers(budget);
                                        
                                        budget.addTransaction(selectedTransaction);
                                        budgetDAO.update(budget);
                                    }
                                }
                            }
                        }
                    }
                    
                    // THÊM: Thông báo transaction được cập nhật
                    ObserverManager.getInstance().notifyTransactionUpdated(selectedTransaction);
                    
                    showInfo("Giao dịch đã được cập nhật thành công!");
                } else {
                    // Create new transaction
                    Transaction newTransaction = new Transaction(chosenWallet, amount, selectedCategory, description);
                    
                    // Set date and time from form inputs
                    newTransaction.setDateTime(transactionDate.getValue().atTime(hour, minute));
                    
                    // Automatically determine strategy based on Category's isExpense property
                    if (selectedCategory.isExpense()) {
                        newTransaction.setStrategy(new ood.application.moneykeeper.model.ExpenseTransactionStrategy());
                    } else {
                        newTransaction.setStrategy(new IncomeTransactionStrategy());
                    }                    // Save transaction
                    transactionDAO.save(newTransaction);
                      // THÊM: Đăng ký observers cho wallet trước khi cập nhật số dư
                    ObserverManager.getInstance().registerWalletObservers(chosenWallet);
                    
                    // Process wallet balance update using wallet.addTransaction to trigger notifications
                    chosenWallet.addTransaction(newTransaction);
                    
                    // Update wallet in database
                    walletDAO.update(chosenWallet);
                    
                    // Process budget for expense transactions
                    if (selectedCategory.isExpense()) {
                        // Get all budgets for this category
                        List<Budget> budgets = budgetDAO.getAll();
                        for (Budget budget : budgets) {
                            if (budget.getCategory().getId().equals(selectedCategory.getId())) {
                                // Check if transaction is within budget period
                                LocalDateTime transactionDateTime = newTransaction.getDateTime();
                                if (transactionDateTime.isAfter(budget.getStartDate()) && 
                                    transactionDateTime.isBefore(budget.getEndDate())) {
                                    
                                    // THÊM: Đăng ký observers cho budget trước khi thêm transaction
                                    ObserverManager.getInstance().registerBudgetObservers(budget);
                                    
                                    budget.addTransaction(newTransaction);
                                    budgetDAO.update(budget);
                                }
                            }
                        }
                    }
                    
                    // THÊM: Thông báo transaction được thêm
                    ObserverManager.getInstance().notifyTransactionAdded(newTransaction);
                    
                    showInfo("Giao dịch đã được thêm thành công!");
                }
                
                loadTransactions();
                clearFields();
            } catch (SQLException e) {
                showError("Lỗi khi lưu giao dịch: " + e.getMessage());
            }
        }    }    private void deleteTransaction() {
        if (selectedTransaction == null) {
            showError("Vui lòng chọn giao dịch để xóa!");
            return;
        }
        
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa giao dịch này?");
        confirmAlert.setContentText("Thao tác này không thể hoàn tác.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {            try {
                // Reverse the wallet balance change before deleting
                Wallet wallet = selectedTransaction.getWallet();
                
                // THÊM: Đăng ký observers cho wallet trước khi thay đổi balance
                ObserverManager.getInstance().registerWalletObservers(wallet);
                
                if (selectedTransaction.getCategory().isExpense()) {
                    // Add money back to wallet (reverse expense) - use income method to trigger notifications
                    wallet.income(selectedTransaction.getAmount());
                } else {
                    // Remove money from wallet (reverse income) - use expense method to trigger notifications
                    wallet.expense(selectedTransaction.getAmount());
                }
                
                // Update wallet in database
                walletDAO.update(wallet);
                
                // Process budget removal for expense transactions
                if (selectedTransaction.getCategory().isExpense()) {
                    // Get all budgets for this category
                    List<Budget> budgets = budgetDAO.getAll();
                    for (Budget budget : budgets) {
                        if (budget.getCategory().getId().equals(selectedTransaction.getCategory().getId())) {
                            // Check if transaction was within budget period
                            LocalDateTime transactionDateTime = selectedTransaction.getDateTime();
                            if (transactionDateTime.isAfter(budget.getStartDate()) && 
                                transactionDateTime.isBefore(budget.getEndDate())) {
                                
                                // THÊM: Đăng ký observers cho budget trước khi xóa transaction
                                ObserverManager.getInstance().registerBudgetObservers(budget);
                                
                                // Remove transaction from budget and update spent amount
                                budget.removeTransaction(selectedTransaction);
                                budgetDAO.update(budget);
                            }
                        }
                    }
                }
                
                // Clear any budget-transaction relationships first
                budgetDAO.clearBudgetTransactionsByTransactionId(selectedTransaction.getTId());
                
                // Delete transaction from database
                transactionDAO.delete(selectedTransaction);
                
                // THÊM: Thông báo transaction được xóa
                ObserverManager.getInstance().notifyTransactionDeleted(selectedTransaction);
                
                // Refresh the transaction list
                loadTransactions();
                clearFields();
                
                showInfo("Giao dịch đã được xóa thành công!");
            } catch (SQLException e) {
                showError("Lỗi khi xóa giao dịch: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                errorMessage.append("- Số tiền phải lớn hơn 0\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("- Số tiền không hợp lệ\n");
        }
        if (descriptionFields.getText().trim().isEmpty()) {
            errorMessage.append("- Vui lòng nhập mô tả giao dịch\n");
        }
        if (transactionDate.getValue() == null) {
            errorMessage.append("- Vui lòng chọn ngày giao dịch\n");
        }
        if (categoryComboBox.getValue() == null) {
            errorMessage.append("- Vui lòng chọn hạng mục\n");
        }
        if (walletComboBox.getValue() == null) {
            errorMessage.append("- Vui lòng chọn ví\n");
        }
        if (errorMessage.length() > 0) {
            showError("Vui lòng sửa các lỗi sau:\n" + errorMessage);
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to initialize data from WalletController
    public void initData(Wallet wallet) {
        this.selectedWallet = wallet;
        if (walletComboBox != null && wallet != null) {
            // Pre-select the wallet in the combo box
            walletComboBox.setValue(wallet);
        }
    }

    private void applyFilter() {
        String selectedFilter = (String) filterComboBox.getValue();
        try {
            List<Transaction> allTransactions = transactionDAO.getAll();
            List<Transaction> filtered = new ArrayList<Transaction>();
            String keyword = searchField.getText().toLowerCase().trim();


            for (int i = 0; i < allTransactions.size(); i++) {
                Transaction t = allTransactions.get(i);
                if ("Chi tiêu".equals(selectedFilter)) {
                    if (t.isExpense()) {
                        filtered.add(t);
                    }
                } else if ("Thu nhập".equals(selectedFilter)) {
                    if (!t.isExpense()) {
                        filtered.add(t);
                    }
                } else {
                    // "Tất cả"
                    filtered.add(t);
                }
            }

            transactions = FXCollections.observableArrayList(filtered);
            transactionTable.setItems(transactions);
            updateTotalLabels(filtered);
        } catch (SQLException e) {
            showError("Lỗi khi lọc giao dịch: " + e.getMessage());
        }
    }

    private void searchTransactionByKeyword() {
        String keyword = searchField.getText().toLowerCase().trim();

        if (keyword.isEmpty()) {
            applyFilter(); // Nếu không nhập gì, áp dụng bộ lọc chung
            return;
        }

        try {
            List<Transaction> allTransactions = transactionDAO.getAll();
            List<Transaction> filtered = new ArrayList<>();

            for (Transaction t : allTransactions) {
                if (t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword)) {
                    filtered.add(t);
                }
            }

            transactions = FXCollections.observableArrayList(filtered);
            transactionTable.setItems(transactions);
            updateTotalLabels(filtered);
        } catch (SQLException e) {
            showError("Lỗi khi tìm kiếm: " + e.getMessage());
        }
    }

    private void updateTotalLabels(List<Transaction> transactions) {
        double totalExpense = 0;
        double totalIncome = 0;

        for (Transaction transaction : transactions) {
            if (transaction.isExpense()) {
                totalExpense += transaction.getAmount();
            } else {
                totalIncome += transaction.getAmount();
            }
        }

        totalExpenseLabel.setText(String.format("%.0f VNĐ", totalExpense));
        totalIncomeLabel.setText(String.format("%.0f VNĐ", totalIncome));
    }
}

