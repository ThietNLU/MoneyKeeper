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
    private TextField amountField;
    @FXML
    private TextArea descriptionFields;
    @FXML
    private DatePicker transactionDate;
    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;
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
    private WalletDAO walletDAO;
    private BudgetDAO budgetDAO;
    private ObservableList<Transaction> transactions;
    private Wallet selectedWallet; // Store the wallet passed from WalletController

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
            categoryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCategory().getName()));
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
            loadWallets();

            // Set up event handlers
            transactionTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            populateFields(newSelection);
                        }
                    });
            
            saveButton.setOnAction(event -> addTransaction());
            clearButton.setOnAction(event -> updateTransaction());

            transactionDate.setValue(LocalDate.now());
            
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
    }

    private void populateFields(Transaction transaction) {
        amountField.setText(String.valueOf(transaction.getAmount()));
        descriptionFields.setText(transaction.getDescription());
        transactionDate.setValue(transaction.getDateTime().toLocalDate());
        categoryComboBox.setValue(transaction.getCategory());
        walletComboBox.setValue(transaction.getWallet());
    }

    private void clearFields() {
        amountField.clear();
        descriptionFields.clear();
        transactionDate.setValue(LocalDate.now());
        categoryComboBox.getSelectionModel().clearSelection();
        walletComboBox.getSelectionModel().clearSelection();
    }    private void addTransaction() {
        if (validateInput()) {
            try {
                Wallet chosenWallet = walletComboBox.getValue();
                Category selectedCategory = categoryComboBox.getValue();
                double amount = Double.parseDouble(amountField.getText().trim());
                String description = descriptionFields.getText().trim();
                
                // Create transaction with constructor
                Transaction newTransaction = new Transaction(chosenWallet, amount, selectedCategory, description);
                
                // Automatically determine strategy based on Category's isExpense property
                if (selectedCategory.isExpense()) {
                    newTransaction.setStrategy(new ood.application.moneykeeper.model.ExpenseTransactionStrategy());
                } else {
                    newTransaction.setStrategy(new IncomeTransactionStrategy());
                }
                  // Set date time
                newTransaction.setDateTime(transactionDate.getValue().atStartOfDay());
                
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
                loadTransactions();
                clearFields();
                showInfo("Giao dịch đã được thêm thành công!");
            } catch (SQLException e) {
                showError("Lỗi khi thêm giao dịch: " + e.getMessage());
            }
        }
    }

    private void updateTransaction() {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showError("Vui lòng chọn giao dịch để cập nhật!");
            return;
        }
        if (validateInput()) {
            try {
                Category selectedCategory = categoryComboBox.getValue();
                
                selectedTransaction.setAmount(Double.parseDouble(amountField.getText().trim()));
                selectedTransaction.setDescription(descriptionFields.getText().trim());
                selectedTransaction.setDateTime(transactionDate.getValue().atStartOfDay());
                selectedTransaction.setCategory(selectedCategory);
                selectedTransaction.setWallet(walletComboBox.getValue());
                
                // Automatically determine strategy based on Category's isExpense property
                if (selectedCategory.isExpense()) {
                    selectedTransaction.setStrategy(new ood.application.moneykeeper.model.ExpenseTransactionStrategy());
                } else {
                    selectedTransaction.setStrategy(new IncomeTransactionStrategy());
                }
                
                transactionDAO.update(selectedTransaction);
                loadTransactions();
                showInfo("Giao dịch đã được cập nhật thành công!");
            } catch (SQLException e) {
                showError("Lỗi khi cập nhật giao dịch: " + e.getMessage());
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

