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
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Category;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.ResourceBundle;

public class BudgetController implements Initializable {
    
    // Form controls
    @FXML
    private TextField budgetNameField;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private TextField limitField;
    @FXML
    private ComboBox<String> periodComboBox;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Button clearButton;
    @FXML
    private Button saveButton;
    @FXML
    private Label errorLabel;

    // Filter and search controls
    @FXML
    private ComboBox<String> filterComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    // Table controls
    @FXML
    private TableView<Budget> budgetTable;
    @FXML
    private TableColumn<Budget, String> nameColumn;
    @FXML
    private TableColumn<Budget, String> categoryColumn;
    @FXML
    private TableColumn<Budget, Double> limitColumn;
    @FXML
    private TableColumn<Budget, Double> usedColumn;
    @FXML
    private TableColumn<Budget, Double> remainingColumn;
    @FXML
    private TableColumn<Budget, String> statusColumn;

    // Action buttons
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    // Data
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;
    private ObservableList<Budget> budgets;
    private Budget selectedBudget;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            budgetDAO = new BudgetDAO();
            categoryDAO = new CategoryDAO();
            
            // Initialize period options
            periodComboBox.setItems(FXCollections.observableArrayList(
                "Tháng này", "Quý này", "Năm này", "Tùy chỉnh"
            ));
            periodComboBox.getSelectionModel().selectFirst();

            // Initialize filter options
            filterComboBox.setItems(FXCollections.observableArrayList(
                "Tất cả", "Hoạt động", "Vượt ngân sách", "Sắp hết hạn"
            ));
            filterComboBox.getSelectionModel().selectFirst();

            // Set up table columns
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            categoryColumn.setCellValueFactory(cellData -> 
                new ReadOnlyStringWrapper(cellData.getValue().getCategory().getName()));            limitColumn.setCellValueFactory(new PropertyValueFactory<>("limit"));
            usedColumn.setCellValueFactory(new PropertyValueFactory<>("spent"));
            remainingColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleDoubleProperty(
                    cellData.getValue().getLimit() - cellData.getValue().getSpent()).asObject());
            statusColumn.setCellValueFactory(cellData -> {
                Budget budget = cellData.getValue();
                double percentage = budget.getSpent() / budget.getLimit() * 100;
                if (percentage >= 100) {
                    return new ReadOnlyStringWrapper("Vượt ngân sách");
                } else if (percentage >= 80) {
                    return new ReadOnlyStringWrapper("Cảnh báo");
                } else {
                    return new ReadOnlyStringWrapper("Bình thường");
                }
            });

            // Format currency columns
            limitColumn.setCellFactory(column -> new TableCell<Budget, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VNĐ", amount));
                    }
                }
            });            usedColumn.setCellFactory(column -> new TableCell<Budget, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VNĐ", amount));
                    }
                }
            });

            remainingColumn.setCellFactory(column -> new TableCell<Budget, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("%,.0f VNĐ", amount));
                    }
                }
            });

            // Enable/disable buttons based on selection
            budgetTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isSelected = (newSelection != null);
                    editButton.setDisable(!isSelected);
                    deleteButton.setDisable(!isSelected);
                    selectedBudget = newSelection;
                    
                    // Fill form with selected budget data
                    if (newSelection != null) {
                        fillFormWithBudget(newSelection);
                    }
                }
            );

            // Load initial data
            loadCategories();
            refreshData();

        } catch (SQLException e) {
            showError("Lỗi khởi tạo: " + e.getMessage());
        }
    }

    private void loadCategories() throws SQLException {
        List<Category> categories = categoryDAO.getAll();
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    private void saveBudget() {
        try {
            // Validate input
            if (budgetNameField.getText().trim().isEmpty()) {
                showError("Vui lòng nhập tên ngân sách");
                return;
            }
            
            if (categoryComboBox.getValue() == null) {
                showError("Vui lòng chọn hạng mục");
                return;
            }
            
            double limit;
            try {
                limit = Double.parseDouble(limitField.getText().replace(",", ""));
                if (limit <= 0) {
                    showError("Giới hạn phải lớn hơn 0");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Giới hạn không hợp lệ");
                return;
            }

            // Calculate start and end dates based on period
            LocalDateTime startDate = LocalDateTime.now();
            LocalDateTime endDate = calculateEndDate(startDate, periodComboBox.getValue());

            Budget budget;
            if (selectedBudget != null) {
                // Update existing budget
                budget = selectedBudget;
                budget.setName(budgetNameField.getText().trim());
                budget.setCategory(categoryComboBox.getValue());
                budget.setLimit(limit);
                budget.setStartDate(startDate);
                budget.setEndDate(endDate);
                
                if (budgetDAO.update(budget)) {
                    showSuccess("Cập nhật ngân sách thành công");
                } else {
                    showError("Không thể cập nhật ngân sách");
                }
            } else {
                // Create new budget
                budget = new Budget(
                    budgetNameField.getText().trim(),
                    limit,
                    startDate,
                    endDate,
                    categoryComboBox.getValue()
                );
                
                if (budgetDAO.save(budget)) {
                    showSuccess("Thêm ngân sách thành công");
                } else {
                    showError("Không thể thêm ngân sách");
                }
            }

            clearForm();
            refreshData();

        } catch (SQLException e) {
            showError("Lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        budgetNameField.clear();
        categoryComboBox.setValue(null);
        limitField.clear();
        periodComboBox.getSelectionModel().selectFirst();
        descriptionField.clear();
        selectedBudget = null;
        budgetTable.getSelectionModel().clearSelection();
        saveButton.setText("Lưu ngân sách");
        hideError();
    }

    @FXML
    private void editBudget() {
        Budget selected = budgetTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedBudget = selected;
            fillFormWithBudget(selected);
            saveButton.setText("Cập nhật ngân sách");
        }
    }

    @FXML
    private void deleteBudget() {
        Budget selected = budgetTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận xóa");
            alert.setHeaderText("Xóa ngân sách");
            alert.setContentText("Bạn có chắc chắn muốn xóa ngân sách \"" + selected.getName() + "\"?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    if (budgetDAO.delete(selected)) {
                        showSuccess("Xóa ngân sách thành công");
                        clearForm();
                        refreshData();
                    } else {
                        showError("Không thể xóa ngân sách");
                    }
                } catch (SQLException e) {
                    showError("Lỗi cơ sở dữ liệu: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void searchBudgets() {
        String searchText = searchField.getText().toLowerCase();
        String filter = filterComboBox.getValue();
        
        if (budgets != null) {
            ObservableList<Budget> filteredBudgets = FXCollections.observableArrayList();
            
            for (Budget budget : budgets) {
                boolean matchesSearch = searchText.isEmpty() || 
                    budget.getName().toLowerCase().contains(searchText) ||
                    budget.getCategory().getName().toLowerCase().contains(searchText);
                
                boolean matchesFilter = true;
                if (!filter.equals("Tất cả")) {
                    double percentage = budget.getSpent() / budget.getLimit() * 100;
                    switch (filter) {
                        case "Hoạt động":
                            matchesFilter = percentage < 100;
                            break;
                        case "Vượt ngân sách":
                            matchesFilter = percentage >= 100;
                            break;
                        case "Sắp hết hạn":
                            matchesFilter = percentage >= 80 && percentage < 100;
                            break;
                    }
                }
                
                if (matchesSearch && matchesFilter) {
                    filteredBudgets.add(budget);
                }
            }
            
            budgetTable.setItems(filteredBudgets);
        }
    }

    @FXML
    private void refreshData() {
        try {
            budgets = FXCollections.observableArrayList(budgetDAO.getAll());
            budgetTable.setItems(budgets);
            hideError();
        } catch (SQLException e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void fillFormWithBudget(Budget budget) {
        budgetNameField.setText(budget.getName());
        categoryComboBox.setValue(budget.getCategory());
        limitField.setText(String.format("%.0f", budget.getLimit()));
        // Note: description field is not part of Budget model
    }

    private LocalDateTime calculateEndDate(LocalDateTime startDate, String period) {
        switch (period) {
            case "Tháng này":
                return startDate.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
            case "Quý này":
                int currentQuarter = (startDate.getMonthValue() - 1) / 3;
                int lastMonthOfQuarter = (currentQuarter + 1) * 3;
                return startDate.withMonth(lastMonthOfQuarter)
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);
            case "Năm này":
                return startDate.with(TemporalAdjusters.lastDayOfYear()).withHour(23).withMinute(59).withSecond(59);
            default:
                return startDate.plusMonths(1);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }
}
