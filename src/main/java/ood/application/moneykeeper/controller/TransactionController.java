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

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionController implements Initializable {
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
                    // Update existing transaction
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
                    showInfo("Giao dịch đã được cập nhật thành công!");
                } else {
                    // Create new transaction
                    Transaction newTransaction = new Transaction(chosenWallet, amount, selectedCategory, description);
                    
                    // Automatically determine strategy based on Category's isExpense property
                    if (selectedCategory.isExpense()) {
                        newTransaction.setStrategy(new ood.application.moneykeeper.model.ExpenseTransactionStrategy());
                    } else {
                        newTransaction.setStrategy(new IncomeTransactionStrategy());
                    }
                    
                    // Set date time with selected hour and minute
                    newTransaction.setDateTime(transactionDate.getValue().atTime(hour, minute));
                    
                    // Save transaction to database first
                    transactionDAO.save(newTransaction);
                    
                    // Process wallet balance update
                    newTransaction.processWallet();
                    
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
                                    budget.addTransaction(newTransaction);
                                    budgetDAO.update(budget);
                                }
                            }
                        }
                    }
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
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Reverse the wallet balance change before deleting
                Wallet wallet = selectedTransaction.getWallet();
                if (selectedTransaction.getCategory().isExpense()) {
                    // Add money back to wallet (reverse expense)
                    wallet.setBalance(wallet.getBalance() + selectedTransaction.getAmount());
                } else {
                    // Remove money from wallet (reverse income)
                    wallet.setBalance(wallet.getBalance() - selectedTransaction.getAmount());
                }
                
                // Update wallet in database
                walletDAO.update(wallet);
                  // Clear any budget-transaction relationships first
                budgetDAO.clearBudgetTransactionsByTransactionId(selectedTransaction.getTId());
                
                // Delete transaction from database
                transactionDAO.delete(selectedTransaction);
                
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
}

