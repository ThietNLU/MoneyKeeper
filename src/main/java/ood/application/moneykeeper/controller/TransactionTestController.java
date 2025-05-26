package ood.application.moneykeeper.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import ood.application.moneykeeper.dao.CategoryDAO;
import ood.application.moneykeeper.dao.TransactionDAO;
import ood.application.moneykeeper.dao.WalletDAO;
import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.Wallet;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionTestController implements Initializable {
    @FXML
    private RadioButton expenseRadio;
    @FXML
    private RadioButton incomeRadio;
    @FXML
    private ToggleGroup transactionType;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private ComboBox<Wallet> walletComboBox;
    @FXML
    private TextField amountField;
    @FXML
    private TextArea descriptionFields;
    @FXML
    private DatePicker transactionDate; // Changed from datePicker to match FXML
    @FXML
    private Button saveButton; // Changed from addButton to match FXML
    @FXML
    private Button clearButton; // Changed from updateButton to match FXML
    // Removed deleteButton which isn't in the FXML
    @FXML
    private TableView<Transaction> transactionTable;
    // Removed idColumn which isn't in the FXML
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
    private TableColumn<Transaction, Boolean> typeColumn;
    @FXML
    private ComboBox<String> filterComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Label errorLabel;

    @FXML
    private Label totalExpenseLabel;
    @FXML
    private Label totalIncomeLabel;

    @FXML
    private Button updateButton;


    private TransactionDAO transactionDAO;
    private CategoryDAO categoryDAO;
    private WalletDAO walletDAO;
    private ObservableList<Transaction> transactions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            transactionDAO = new TransactionDAO();
            categoryDAO = new CategoryDAO();
            walletDAO = new WalletDAO();

            // Initialize filter button
            filterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Chi tiêu", "Thu nhập"));
            filterComboBox.getSelectionModel().selectFirst(); // Mặc định chọn "Tất cả"

            filterComboBox.setOnAction(event -> applyFilter());

            // Initialize search button
            searchButton.setOnAction(event -> searchTransactionByKeyword());


            // Change from 'date' to 'dateTime' to match the property name in Transaction class
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
            // Check if descriptionColumn exists in FXML before using it
            if (descriptionColumn != null) {
                descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            }
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            categoryColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCategory().getName()));
            // Check if walletColumn exists in FXML before using it
            if (walletColumn != null) {
                walletColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getWallet().getName()));
            }
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("expense"));

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
            });

            loadTransactions();
            loadCategories();
            loadWallets();

            transactionTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            populateFields(newSelection);
                        }
                    });

            saveButton.setOnAction(event -> addTransaction());


            transactionDate.setValue(LocalDate.now());
            expenseRadio.setSelected(true);
        } catch (SQLException e) {
            showError("Lỗi khi khởi tạo: " + e.getMessage());
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


    private void loadTransactions() throws SQLException {
        List<Transaction> transactionList = transactionDAO.getAll();
        transactions = FXCollections.observableArrayList(transactionList);
        transactionTable.setItems(transactions);
    }

    private void loadCategories() throws SQLException {
        List<Category> categories = categoryDAO.getAll();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
    }

    private void loadWallets() throws SQLException {
        List<Wallet> wallets = walletDAO.getAll();
        walletComboBox.setItems(FXCollections.observableArrayList(wallets));
    }

    private void populateFields(Transaction transaction) {
        amountField.setText(String.valueOf(transaction.getAmount()));
        descriptionFields.setText(transaction.getDescription());
        transactionDate.setValue(transaction.getDateTime().toLocalDate());
        categoryComboBox.setValue(transaction.getCategory());
        walletComboBox.setValue(transaction.getWallet());
        if (transaction.isExpense()) {
            expenseRadio.setSelected(true);
        } else {
            incomeRadio.setSelected(true);
        }
    }

    private void clearFields() {
        amountField.clear();
        descriptionFields.clear();
        transactionDate.setValue(LocalDate.now());
        categoryComboBox.getSelectionModel().clearSelection();
        walletComboBox.getSelectionModel().clearSelection();
        expenseRadio.setSelected(true);
    }

    private void addTransaction() {
        if (validateInput()) {
            try {
                Transaction newTransaction = new Transaction();
                newTransaction.setAmount(Double.parseDouble(amountField.getText().trim()));
                newTransaction.setDescription(descriptionFields.getText().trim());
                newTransaction.setDateTime(transactionDate.getValue().atStartOfDay());
                newTransaction.setCategory(categoryComboBox.getValue());
                newTransaction.setWallet(walletComboBox.getValue());
                // Sử dụng Strategy pattern đúng cách
                if (expenseRadio.isSelected()) {
                    newTransaction.setStrategy(new ood.application.moneykeeper.model.ExpenseTransactionStrategy());
                } else {
                    newTransaction.setStrategy(new ood.application.moneykeeper.model.IncomTransactionStrategy());
                }
                transactionDAO.save(newTransaction);
                loadTransactions();
                clearFields();
                showInfo("Giao dịch đã được thêm thành công!");
            } catch (SQLException e) {
                showError("Lỗi khi thêm giao dịch: " + e.getMessage());
            }
        }
    }

    private void reloadTransactions() throws SQLException {
        List<Transaction> updatedList = transactionDAO.getAll();
        transactions.setAll(updatedList); // Giữ nguyên ObservableList, chỉ thay nội dung
    }


    private void updateTransaction() {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {

            showError("Vui lòng chọn giao dịch để cập nhật!");
            return;
        }
        if (validateInput()) {
            try {
                selectedTransaction.setAmount(Double.parseDouble(amountField.getText().trim()));
                selectedTransaction.setDescription(descriptionFields.getText().trim());
                selectedTransaction.setDateTime(transactionDate.getValue().atStartOfDay());
                selectedTransaction.setCategory(categoryComboBox.getValue());
                selectedTransaction.setWallet(walletComboBox.getValue());
                // Sử dụng Strategy pattern đúng cách khi cập nhật
                if (expenseRadio.isSelected()) {
                    selectedTransaction.setStrategy(new ood.application.moneykeeper.model.ExpenseTransactionStrategy());
                } else {
                    selectedTransaction.setStrategy(new ood.application.moneykeeper.model.IncomTransactionStrategy());
                }
                transactionDAO.update(selectedTransaction);
                loadTransactions();
                showInfo("Giao dịch đã được cập nhật thành công!");
            } catch (SQLException e) {
                showError("Lỗi khi cập nhật giao dịch: " + e.getMessage());
            }
        }
    }

    private void deleteTransaction() {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction == null) {
            showError("Vui lòng chọn giao dịch để xóa!");
            return;
        }
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa giao dịch này không?");
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    transactionDAO.deleteById(selectedTransaction.getTId());
                    loadTransactions();
                    clearFields();
                    showInfo("Giao dịch đã được xóa thành công!");
                } catch (SQLException e) {
                    showError("Lỗi khi xóa giao dịch: " + e.getMessage());
                }
            }
        });
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
}
