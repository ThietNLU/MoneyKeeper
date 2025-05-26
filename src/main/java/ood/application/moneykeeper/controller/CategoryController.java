package ood.application.moneykeeper.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.dao.CategoryDAO;
import ood.application.moneykeeper.dao.TransactionDAO;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.Wallet;

import java.time.format.DateTimeFormatter;

import java.net.URL;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {
    @FXML
    private TableView<Category> categoryTableView;
    @FXML
    private TableColumn<Category, String> nameColumn;
    @FXML
    private TableColumn<Category, String> typeColumn;
    @FXML
    private Button addCategoryButton;
    @FXML
    private Button editCategoryButton;
    @FXML
    private Button deleteCategoryButton;
    @FXML
    private Label categoryIdLabel;
    @FXML
    private Label categoryNameLabel;
    @FXML
    private Label categoryTypeLabel;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private Button saveCategoryButton;
    @FXML
    private Button clearFormButton;

    @FXML
    private TableView<Transaction> transactionTableView;
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    @FXML
    private TableColumn<Transaction, String> amountColumn;
    @FXML
    private TableColumn<Transaction, String> noteColumn;
    @FXML
    private TableColumn<Transaction, String> walletColumn;

    @FXML
    private TableView<ood.application.moneykeeper.model.Budget> budgetTableView;
    @FXML
    private TableColumn<ood.application.moneykeeper.model.Budget, String> budgetNameColumn;
    @FXML
    private TableColumn<ood.application.moneykeeper.model.Budget, String> budgetLimitColumn;
    @FXML
    private TableColumn<ood.application.moneykeeper.model.Budget, String> budgetPeriodColumn;
    @FXML
    private TableColumn<ood.application.moneykeeper.model.Budget, String> budgetSpentColumn;

    private Category editingCategory = null;
    private CategoryDAO categoryDAO;
    private TransactionDAO transactionDAO;
    private ood.application.moneykeeper.dao.BudgetDAO budgetDAO;
    private ObservableList<Category> categories = FXCollections.observableArrayList();
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private ObservableList<ood.application.moneykeeper.model.Budget> budgets = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            categoryDAO = new CategoryDAO();
            transactionDAO = new TransactionDAO();
            budgetDAO = new ood.application.moneykeeper.dao.BudgetDAO();
            categories.setAll(categoryDAO.getAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isExpense() ? "Chi tiêu" : "Thu nhập"));
        categoryTableView.setItems(categories);
        categoryTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> showCategoryDetails(newVal));
        editCategoryButton.setDisable(true);
        deleteCategoryButton.setDisable(true);
        categoryTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            editCategoryButton.setDisable(!selected);
            deleteCategoryButton.setDisable(!selected);
            if (selected) {
                fillForm(newVal);
            }
        });
        saveCategoryButton.setOnAction(e -> saveCategory());
        clearFormButton.setOnAction(e -> clearForm());
        addCategoryButton.setOnAction(e -> clearForm());
        editCategoryButton.setOnAction(e -> {
            Category selected = categoryTableView.getSelectionModel().getSelectedItem();
            if (selected != null) fillForm(selected);
        });
        deleteCategoryButton.setOnAction(e -> deleteCategory());

        // Transaction TableView setup
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateTime() != null)
                return new SimpleStringProperty(cellData.getValue().getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            return new SimpleStringProperty("");
        });
        amountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%.0f", cellData.getValue().getAmount())));
        noteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        walletColumn.setCellValueFactory(cellData -> {
            Wallet w = cellData.getValue().getWallet();
            return new SimpleStringProperty(w != null ? w.getName() : "");
        });
        transactionTableView.setItems(transactions);

        // Budget TableView setup
        budgetNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        budgetLimitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getLimit())));
        budgetPeriodColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPeriod()));
        budgetSpentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSpent())));
        budgetTableView.setItems(budgets);

        // Demo data
        categories.addAll(
                new Category("Ăn uống", true),
                new Category("Lương", false)
        );
    }

    private void showCategoryDetails(Category category) {
        if (category != null) {
            categoryIdLabel.setText("ID: " + category.getId());
            categoryNameLabel.setText("Tên: " + category.getName());
            categoryTypeLabel.setText("Loại: " + (category.isExpense() ? "Chi tiêu" : "Thu nhập"));
            // Load transactions for this category
            try {
                transactions.setAll(transactionDAO.getAll().stream().filter(t -> t.getCategory() != null && t.getCategory().getId().equals(category.getId())).toList());
                budgets.setAll(budgetDAO.getAll().stream().filter(b -> b.getCategory() != null && b.getCategory().getId().equals(category.getId())).toList());
            } catch (Exception e) {
                transactions.clear();
                budgets.clear();
            }
        } else {
            categoryIdLabel.setText("ID: ");
            categoryNameLabel.setText("Tên: ");
            categoryTypeLabel.setText("Loại: ");
            transactions.clear();
            budgets.clear();
        }
    }

    private void fillForm(Category category) {
        editingCategory = category;
        nameField.setText(category.getName());
        typeComboBox.setValue(category.isExpense() ? "Chi tiêu" : "Thu nhập");
    }

    private void clearForm() {
        editingCategory = null;
        nameField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        categoryTableView.getSelectionModel().clearSelection();
    }

    private void saveCategory() {
        String name = nameField.getText().trim();
        String type = typeComboBox.getValue();
        if (name.isEmpty() || type == null) return;
        boolean isExpense = "Chi tiêu".equals(type);
        try {
            if (editingCategory == null) {
                Category newCat = new Category(name, isExpense);
                if (categoryDAO.save(newCat)) {
                    categories.add(newCat);
                    clearForm();
                }
            } else {
                editingCategory.setName(name);
                editingCategory.setExpense(isExpense);
                if (categoryDAO.update(editingCategory)) {
                    categoryTableView.refresh();
                    clearForm();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void deleteCategory() {
        Category selected = categoryTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            if (categoryDAO.delete(selected)) {
                categories.remove(selected);
                clearForm();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

