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
        
        // Setup ComboBox options
        typeComboBox.getItems().addAll("Chi tiêu", "Thu nhập");
        
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
        });        saveCategoryButton.setOnAction(e -> saveCategory());
        clearFormButton.setOnAction(e -> clearForm());
        addCategoryButton.setOnAction(e -> initializeAddCategory());
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
        budgetSpentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getSpent())));        budgetTableView.setItems(budgets);
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
    }    private void fillForm(Category category) {
        editingCategory = category;
        nameField.setText(category.getName());
        typeComboBox.setValue(category.isExpense() ? "Chi tiêu" : "Thu nhập");
        
        // Update button text for editing mode
        saveCategoryButton.setText("Cập nhật danh mục");
    }private void clearForm() {
        editingCategory = null;
        nameField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        categoryTableView.getSelectionModel().clearSelection();
        
        // Update button text based on mode
        saveCategoryButton.setText("Thêm danh mục");
    }private void saveCategory() {
        String name = nameField.getText().trim();
        String type = typeComboBox.getValue();
        
        // Validation
        if (name.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tên danh mục");
            nameField.requestFocus();
            return;
        }
        if (type == null) {
            showAlert("Lỗi", "Vui lòng chọn loại danh mục");
            typeComboBox.requestFocus();
            return;
        }
        
        boolean isExpense = "Chi tiêu".equals(type);
        try {
            if (editingCategory == null) {
                // Adding new category
                Category newCat = new Category(name, isExpense);
                if (categoryDAO.save(newCat)) {
                    categories.add(newCat);
                    clearForm();
                    showAlert("Thành công", "Đã thêm danh mục mới thành công!");
                } else {
                    showAlert("Lỗi", "Không thể thêm danh mục mới");
                }
            } else {
                // Editing existing category
                editingCategory.setName(name);
                editingCategory.setExpense(isExpense);
                if (categoryDAO.update(editingCategory)) {
                    categoryTableView.refresh();
                    clearForm();
                    showAlert("Thành công", "Đã cập nhật danh mục thành công!");
                } else {
                    showAlert("Lỗi", "Không thể cập nhật danh mục");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }    private void deleteCategory() {
        Category selected = categoryTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Vui lòng chọn danh mục để xóa");
            return;
        }
        
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Xóa danh mục");
        confirmAlert.setContentText("Bạn có chắc chắn muốn xóa danh mục '" + selected.getName() + "'?");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                if (categoryDAO.delete(selected)) {
                    categories.remove(selected);
                    clearForm();
                    showAlert("Thành công", "Đã xóa danh mục thành công!");
                } else {
                    showAlert("Lỗi", "Không thể xóa danh mục");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Lỗi", "Đã xảy ra lỗi khi xóa: " + e.getMessage());
            }
        }
    }

    private void initializeAddCategory() {
        clearForm();
        // Focus on name field for new category
        nameField.requestFocus();
    }
}

