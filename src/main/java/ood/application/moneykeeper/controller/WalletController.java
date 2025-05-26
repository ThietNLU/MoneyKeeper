package ood.application.moneykeeper.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ood.application.moneykeeper.dao.TransactionDAO;
import ood.application.moneykeeper.dao.WalletDAO;
import ood.application.moneykeeper.dao.UserDAO;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.Wallet;
import ood.application.moneykeeper.model.User;
import ood.application.moneykeeper.report.CategoryReport;
import ood.application.moneykeeper.report.MonthlyTimeStrategy;
import ood.application.moneykeeper.report.Report;
import ood.application.moneykeeper.report.ReportData;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class WalletController implements Initializable {

    @FXML private ListView<Wallet> walletListView;
    @FXML private Button addWalletButton;
    @FXML private Button editWalletButton;
    @FXML private Button deleteWalletButton;
    @FXML private Label walletNameLabel;
    @FXML private Label balanceLabel;
    @FXML private Label creationDateLabel;
    @FXML private ComboBox<String> transactionFilterComboBox;
    @FXML private Button addTransactionButton;
    @FXML private TableView<Transaction> transactionTableView;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private ComboBox<String> reportPeriodComboBox;
    @FXML private BorderPane expenseChartContainer;
    @FXML private BorderPane incomeExpenseContainer;
    @FXML private Label totalBalanceLabel;    private WalletDAO walletDAO;
    private TransactionDAO transactionDAO;
    private UserDAO userDAO;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {        try {
            walletDAO = new WalletDAO();
            transactionDAO = new TransactionDAO();
            userDAO = new UserDAO();
            setupWalletListView();
            setupTransactionTable();
            setupComboBoxes();
            setupButtons();
            refreshWalletList();
        } catch (SQLException e) {
            showErrorAlert("Database error", "Could not initialize wallet view: " + e.getMessage());
        }
    }

    private void setupWalletListView() {
        walletListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Wallet wallet, boolean empty) {
                super.updateItem(wallet, empty);
                setText((empty || wallet == null) ? null : wallet.getName() + " (" + formatMoney(wallet.getBalance()) + ")");
            }
        });
        walletListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayWalletDetails(newSelection);
                editWalletButton.setDisable(false);
                deleteWalletButton.setDisable(false);
            } else {
                clearWalletDetails();
            }
        });
    }

    private void clearWalletDetails() {
        walletNameLabel.setText("Chi tiết ví");
        balanceLabel.setText("0 VNĐ");
        creationDateLabel.setText("");
        editWalletButton.setDisable(true);
        deleteWalletButton.setDisable(true);
        transactionTableView.setItems(FXCollections.observableArrayList());
        expenseChartContainer.setCenter(null);
        incomeExpenseContainer.setCenter(null);
    }

    private void setupTransactionTable() {
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateTime().format(dateFormatter)));
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory().getName()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        amountColumn.setCellValueFactory(cellData -> {
            double amount = cellData.getValue().getAmount();
            if (cellData.getValue().isExpense()) amount = -amount;
            return new SimpleDoubleProperty(amount).asObject();
        });
        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatMoney(amount));
                    setStyle(amount < 0 ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
                }
            }
        });
    }

    private void setupComboBoxes() {
        transactionFilterComboBox.setItems(FXCollections.observableArrayList(
                "Tất cả", "7 ngày gần đây", "30 ngày gần đây", "Thu nhập", "Chi tiêu"
        ));
        transactionFilterComboBox.getSelectionModel().select(0);
        transactionFilterComboBox.setOnAction(e -> loadTransactions());
        reportPeriodComboBox.setItems(FXCollections.observableArrayList(
                "Tháng này", "Quý này", "Năm nay"
        ));
        reportPeriodComboBox.getSelectionModel().select(0);
        reportPeriodComboBox.setOnAction(e -> updateCharts());
    }

    private void setupButtons() {
        addWalletButton.setOnAction(event -> showWalletDialog(null));
        editWalletButton.setOnAction(event -> showWalletDialog(walletListView.getSelectionModel().getSelectedItem()));
        deleteWalletButton.setOnAction(event -> deleteWallet());
        addTransactionButton.setOnAction(event -> showTransactionDialog());
    }

    private void refreshWalletList() {
        try {
            List<Wallet> wallets = walletDAO.getAll();
            walletListView.setItems(FXCollections.observableArrayList(wallets));
            double totalBalance = wallets.stream().mapToDouble(Wallet::getBalance).sum();
            totalBalanceLabel.setText(formatMoney(totalBalance));
        } catch (SQLException e) {
            showErrorAlert("Database error", "Could not load wallets: " + e.getMessage());
        }
    }

    private void displayWalletDetails(Wallet wallet) {
        walletNameLabel.setText(wallet.getName());
        balanceLabel.setText(formatMoney(wallet.getBalance()));
        if (wallet.getCreationDate() != null) {
            creationDateLabel.setText(wallet.getCreationDate().format(dateFormatter));
        } else {
            creationDateLabel.setText("");
        }
        loadTransactions();
        updateCharts();
    }

    private void loadTransactions() {
        Wallet selectedWallet = walletListView.getSelectionModel().getSelectedItem();
        if (selectedWallet == null) {
            transactionTableView.setItems(FXCollections.observableArrayList());
            return;
        }
        try {
            String filter = transactionFilterComboBox.getValue();
            List<Transaction> transactions;
            LocalDateTime now = LocalDateTime.now();
            if (filter.equals("Tất cả")) {
                transactions = transactionDAO.getTransactionsByWalletId(selectedWallet.getId());
            } else if (filter.equals("7 ngày gần đây")) {
                transactions = transactionDAO.getTransactionsByWalletAndDateRange(selectedWallet.getId(), now.minusDays(7), now);
            } else if (filter.equals("30 ngày gần đây")) {
                transactions = transactionDAO.getTransactionsByWalletAndDateRange(selectedWallet.getId(), now.minusDays(30), now);
            } else if (filter.equals("Thu nhập")) {
                transactions = transactionDAO.getTransactionsByWalletAndType(selectedWallet.getId(), false);
            } else {
                transactions = transactionDAO.getTransactionsByWalletAndType(selectedWallet.getId(), true);
            }
            transactionTableView.setItems(FXCollections.observableArrayList(transactions));
        } catch (SQLException e) {
            showErrorAlert("Database error", "Could not load transactions: " + e.getMessage());
        }
    }

    private void updateCharts() {
        Wallet selectedWallet = walletListView.getSelectionModel().getSelectedItem();
        if (selectedWallet == null) {
            expenseChartContainer.setCenter(null);
            incomeExpenseContainer.setCenter(null);
            return;
        }
        try {
            Report report = new CategoryReport();
            report.setTimeStrategy(new MonthlyTimeStrategy());
            ReportData reportData = report.generateReport();
            PieChart expenseChart = new PieChart();
            expenseChart.setTitle("Chi tiêu theo hạng mục");
            if (reportData.getExpensesByCategory() != null) {
                ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
                for (Map.Entry<String, Double> entry : reportData.getExpensesByCategory().entrySet()) {
                    pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
                expenseChart.setData(pieChartData);
            }
            expenseChartContainer.setCenter(expenseChart);
            // Optionally update incomeExpenseContainer here
        } catch (SQLException e) {
            showErrorAlert("Report error", "Could not generate wallet reports: " + e.getMessage());
        }
    }

    private void showWalletDialog(Wallet walletToEdit) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(walletToEdit == null ? "Thêm ví mới" : "Chỉnh sửa ví");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            TextField nameField = new TextField();
            nameField.setPromptText("Tên ví");
            TextField balanceField = new TextField();
            balanceField.setPromptText("Số dư ban đầu");
            grid.add(new Label("Tên ví:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Số dư:"), 0, 1);
            grid.add(balanceField, 1, 1);
            if (walletToEdit != null) {
                nameField.setText(walletToEdit.getName());
                balanceField.setText(String.valueOf(walletToEdit.getBalance()));
                balanceField.setDisable(true);
            }
            dialog.getDialogPane().setContent(grid);
            nameField.requestFocus();
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showErrorAlert("Invalid input", "Wallet name cannot be empty.");
                    return;
                }                if (walletToEdit == null) {
                    try {
                        double balance = Double.parseDouble(balanceField.getText());
                        Wallet newWallet = new Wallet();
                        newWallet.setName(name);
                        newWallet.setBalance(balance);
                        newWallet.setCreationDate(LocalDateTime.now());
                        
                        // Set the owner for the wallet
                        User defaultUser = getOrCreateDefaultUser();
                        newWallet.setOwner(defaultUser);
                        
                        walletDAO.save(newWallet);
                        refreshWalletList();
                    } catch (NumberFormatException e) {
                        showErrorAlert("Invalid input", "Please enter a valid balance amount.");
                    }
                } else {
                    walletToEdit.setName(name);
                    walletDAO.update(walletToEdit);
                    refreshWalletList();
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database error", "Error working with wallet: " + e.getMessage());
        }
    }

    private void deleteWallet() {
        Wallet selectedWallet = walletListView.getSelectionModel().getSelectedItem();
        if (selectedWallet == null) return;
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận");
        confirmDialog.setHeaderText("Xóa ví " + selectedWallet.getName());
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa ví này? Tất cả giao dịch liên quan cũng sẽ bị xóa.");
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                walletDAO.deleteById(selectedWallet.getId());
                refreshWalletList();
            } catch (SQLException e) {
                showErrorAlert("Database error", "Could not delete wallet: " + e.getMessage());
            }
        }
    }    private void showTransactionDialog() {
        Wallet selectedWallet = walletListView.getSelectionModel().getSelectedItem();
        if (selectedWallet == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ood/application/moneykeeper/transaction_test.fxml"));
            Parent root = loader.load();
            TransactionController controller = loader.getController();
            controller.initData(selectedWallet);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Thêm giao dịch mới");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            refreshWalletList();
            if (walletListView.getSelectionModel().getSelectedItem() != null) {
                loadTransactions();
                updateCharts();
            }
        } catch (IOException e) {
            showErrorAlert("UI Error", "Could not open transaction dialog: " + e.getMessage());
        }
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Get the default user with id "1" or create one if it doesn't exist
     */
    private User getOrCreateDefaultUser() throws SQLException {
        User user = userDAO.get("1");
        if (user != null) {
            return user;
        }
        // Otherwise, create a default user with id "1"
        user = new User("1", "Default User");
        userDAO.save(user);
        return user;
    }
}

