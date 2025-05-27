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
import ood.application.moneykeeper.observer.ObserverManager;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class BudgetController implements Initializable {
    
    // Form controls
    @FXML
    private TextField budgetNameField;
    @FXML
    private ComboBox<Category> categoryComboBox;    @FXML
    private TextField limitField;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
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
    private Button searchButton;    // Table controls
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
    private TableColumn<Budget, String> startDateColumn;
    @FXML
    private TableColumn<Budget, String> endDateColumn;
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
    private static final String DEFAULT_USER_ID = "1";
    private static final String DEFAULT_USER_NAME = "Default User";
    private ood.application.moneykeeper.dao.UserDAO userDAO;
    private ood.application.moneykeeper.model.User defaultUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            budgetDAO = new BudgetDAO();
            categoryDAO = new CategoryDAO();            userDAO = new ood.application.moneykeeper.dao.UserDAO();
            defaultUser = getOrCreateDefaultUser();

            // Initialize date pickers with current date
            startDatePicker.setValue(LocalDate.now());
            endDatePicker.setValue(LocalDate.now().plusMonths(1));

            // Initialize filter options
            filterComboBox.setItems(FXCollections.observableArrayList(
                    "Tất cả", "Bình thường", "Vượt ngân sách", "Cảnh báo"
            ));
            filterComboBox.getSelectionModel().selectFirst();
            filterComboBox.setOnAction(event -> filterBudgets());

            // Set up table columns
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));            categoryColumn.setCellValueFactory(cellData -> {
                Category category = cellData.getValue().getCategory();
                return new ReadOnlyStringWrapper(category != null ? category.getName() : "N/A");
            });limitColumn.setCellValueFactory(new PropertyValueFactory<>("limit"));
            usedColumn.setCellValueFactory(new PropertyValueFactory<>("spent"));            remainingColumn.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleDoubleProperty(
                    cellData.getValue().getLimit() - cellData.getValue().getSpent()).asObject());
            startDateColumn.setCellValueFactory(cellData -> {
                Budget budget = cellData.getValue();
                return new ReadOnlyStringWrapper(budget.getStartDate() != null ? 
                    budget.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
            });
            endDateColumn.setCellValueFactory(cellData -> {
                Budget budget = cellData.getValue();
                return new ReadOnlyStringWrapper(budget.getEndDate() != null ? 
                    budget.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
            });
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
            );            // Load initial data
            loadCategories();
            refreshData();
            
            // THÊM: Khởi tạo ObserverManager và đăng ký observers cho budgets hiện có
            initializeObservers();

        } catch (SQLException e) {
            showError("Lỗi khởi tạo: " + e.getMessage());
        }
    }

    private ood.application.moneykeeper.model.User getOrCreateDefaultUser() throws SQLException {
        ood.application.moneykeeper.model.User user = userDAO.get(DEFAULT_USER_ID);
        if (user != null) return user;
        user = new ood.application.moneykeeper.model.User(DEFAULT_USER_ID, DEFAULT_USER_NAME);
        userDAO.save(user);
        return user;
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
            
            if (startDatePicker.getValue() == null) {
                showError("Vui lòng chọn ngày bắt đầu");
                return;
            }
            
            if (endDatePicker.getValue() == null) {
                showError("Vui lòng chọn ngày kết thúc");
                return;
            }
            
            if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
                showError("Ngày kết thúc phải sau ngày bắt đầu");
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
                return;            }

            // Convert LocalDate to LocalDateTime
            LocalDateTime startDate = startDatePicker.getValue().atStartOfDay();
            LocalDateTime endDate = endDatePicker.getValue().atTime(23, 59, 59);

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
                    // THÊM: Đăng ký observers cho budget được cập nhật
                    ObserverManager.getInstance().registerBudgetObservers(budget);
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
                // Gán user mặc định nếu Budget có thuộc tính user
                try {
                    java.lang.reflect.Method setUser = budget.getClass().getMethod("setUser", ood.application.moneykeeper.model.User.class);
                    setUser.invoke(budget, defaultUser);
                } catch (Exception ignore) {}                if (budgetDAO.save(budget)) {
                    // THÊM: Đăng ký observers cho budget mới được tạo
                    ObserverManager.getInstance().registerBudgetObservers(budget);
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
    }    @FXML
    private void clearForm() {
        budgetNameField.clear();
        categoryComboBox.setValue(null);
        limitField.clear();
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusMonths(1));
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
            alert.setContentText("Bạn có chắc chắn muốn xóa ngân sách \"" + selected.getName() + "\"?");            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    if (budgetDAO.delete(selected)) {
                        // THÊM: Hủy đăng ký observers khi xóa budget
                        ObserverManager.getInstance().unregisterBudgetObservers(selected);
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

    private void filterBudgets() {
        String filter = filterComboBox.getValue();

        if (budgets == null) return;

        ObservableList<Budget> filtered = FXCollections.observableArrayList();

        for (Budget budget : budgets) {
            boolean matchesFilter = true;
            double percentage = budget.getSpent() / budget.getLimit() * 100;

            switch (filter) {
                case "Bình thường":
                    matchesFilter = percentage < 80;
                    break;
                case "Vượt ngân sách":
                    matchesFilter = percentage >= 100;
                    break;
                case "Cảnh báo":
                    matchesFilter = percentage >= 80 && percentage < 100;
                    break;
                case "Tất cả":
                default:
                    matchesFilter = true;
                    break;
            }

            if (matchesFilter) {
                filtered.add(budget);
            }
        }

        budgetTable.setItems(filtered);
    }

    @FXML
    private void searchBudgets() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (budgets == null) return;

        ObservableList<Budget> filtered = FXCollections.observableArrayList();

        for (Budget budget : budgets) {
            boolean matchesSearch = searchText.isEmpty()
                    || budget.getName().toLowerCase().contains(searchText)
                    || budget.getCategory().getName().toLowerCase().contains(searchText);

            if (matchesSearch) {
                filtered.add(budget);
            }
        }

        budgetTable.setItems(filtered);
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
    }    private void fillFormWithBudget(Budget budget) {
        budgetNameField.setText(budget.getName());
        categoryComboBox.setValue(budget.getCategory());
        limitField.setText(String.format("%.0f", budget.getLimit()));
        
        // Set date pickers if budget has dates
        if (budget.getStartDate() != null) {
            startDatePicker.setValue(budget.getStartDate().toLocalDate());        }
        if (budget.getEndDate() != null) {
            endDatePicker.setValue(budget.getEndDate().toLocalDate());
        }
        
        // Note: description field is not part of Budget model
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
    }    private void hideError() {
        errorLabel.setVisible(false);
    }
    
    /**
     * Khởi tạo Observer pattern cho BudgetController
     */
    private void initializeObservers() {
        try {
            // Đăng ký observers cho tất cả budgets hiện có
            registerObserversForExistingBudgets();
        } catch (SQLException e) {
            showError("Lỗi khởi tạo observers: " + e.getMessage());
        }
    }
    
    /**
     * Đăng ký observers cho tất cả budgets hiện có
     */
    private void registerObserversForExistingBudgets() throws SQLException {
        List<Budget> allBudgets = budgetDAO.getAll();
        for (Budget budget : allBudgets) {
            ObserverManager.getInstance().registerBudgetObservers(budget);
        }
    }
}
