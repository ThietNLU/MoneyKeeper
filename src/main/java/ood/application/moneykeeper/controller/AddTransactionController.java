package ood.application.moneykeeper.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ood.application.moneykeeper.dao.CategoryDAO;
import ood.application.moneykeeper.dao.TransactionDAO;
import ood.application.moneykeeper.dao.UserDAO;
import ood.application.moneykeeper.dao.WalletDAO;
import ood.application.moneykeeper.dao.BudgetDAO;
import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.model.IncomeTransactionStrategy;
import ood.application.moneykeeper.model.Transaction;
import ood.application.moneykeeper.model.User;
import ood.application.moneykeeper.model.Wallet;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.observer.ObserverManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AddTransactionController {
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<Wallet> walletComboBox;    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Spinner<Integer> hourSpinner;
    @FXML
    private Spinner<Integer> minuteSpinner;
    @FXML
    private TextField descriptionField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label errorLabel;    private final WalletDAO walletDAO;
    private CategoryDAO categoryDAO;
    private TransactionDAO transactionDAO;
    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO = new UserDAO();
    private User defaultUser;

    public AddTransactionController() throws SQLException {
        this.walletDAO = new WalletDAO();
        this.categoryDAO = new CategoryDAO();
        this.transactionDAO = new TransactionDAO();
        this.budgetDAO = new BudgetDAO();
        this.categoryDAO = new CategoryDAO();
        this.transactionDAO = new TransactionDAO();
    }

    @FXML
    public void initialize() {
        try {
            defaultUser = getOrCreateDefaultUser();
            // Load wallets and categories
            List<Wallet> wallets = walletDAO.getAll();
            List<Category> categories = categoryDAO.getAll();

            // Set up wallet ComboBox
            walletComboBox.setItems(FXCollections.observableArrayList(wallets));
            walletComboBox.setConverter(new StringConverter<Wallet>() {
                @Override
                public String toString(Wallet wallet) {
                    return wallet != null ? wallet.getName() : null;
                }

                @Override
                public Wallet fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });

            // Set up category ComboBox
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            categoryComboBox.setConverter(new StringConverter<Category>() {
                @Override
                public String toString(Category category) {
                    return category != null ? category.getName() + " (" + (category.isExpense() ? "Chi tiêu" : "Thu nhập") + ")" : null;
                }

                @Override
                public Category fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });            // Set default date to today
            datePicker.setValue(java.time.LocalDate.now());

            // Set up time spinners
            hourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalDateTime.now().getHour()));
            hourSpinner.setEditable(true);
            
            minuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, LocalDateTime.now().getMinute()));
            minuteSpinner.setEditable(true);

            // Add event handlers
            saveButton.setOnAction(e -> handleSave());
            cancelButton.setOnAction(e -> ((Stage) cancelButton.getScene().getWindow()).close());

        } catch (Exception e) {
            e.printStackTrace(); // Print detailed error for debugging
            errorLabel.setText("Không thể tải dữ liệu ví/hạng mục: " + e.getMessage());
        }
    }

    private User getOrCreateDefaultUser() throws SQLException {
        // Lấy tên user từ giao diện nếu có (ví dụ từ Settings hoặc MainView)
        String userName = "Default User";
        // Nếu có logic đồng bộ tên user với label trong mainview, lấy tên từ đó
        // Ví dụ: userName = MainViewController.getCurrentUserName();
        User user = userDAO.get("1");
        if (user != null) {
            // Nếu tên user đã thay đổi ở nơi khác, cập nhật lại tên cho đồng bộ
            if (!user.getName().equals(userName)) {
                user.setName(userName);
                userDAO.update(user);
            }
            return user;
        }
        user = new User("1", userName);
        userDAO.save(user);
        return user;
    }    private void handleSave() {
        try {
            Wallet wallet = walletComboBox.getValue();
            Category category = categoryComboBox.getValue();
            double amount = Double.parseDouble(amountField.getText().trim());
            String desc = descriptionField.getText().trim();
            LocalDate date = datePicker.getValue();
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            
            Transaction transaction = new Transaction(wallet, amount, category, desc);
            transaction.setDateTime(date.atTime(hour, minute));
            
            if (category.isExpense()) {
                transaction.setStrategy(new ood.application.moneykeeper.model.ExpenseTransactionStrategy());
            } else {
                transaction.setStrategy(new IncomeTransactionStrategy());
            }
            // Gán user mặc định nếu Transaction có setUser
            try {
                java.lang.reflect.Method setUser = transaction.getClass().getMethod("setUser", User.class);
                setUser.invoke(transaction, defaultUser);
            } catch (Exception ignore) {}
            
            // Save transaction first
            if (transactionDAO.save(transaction)) {
                // Update budgets if this is an expense transaction
                if (category.isExpense()) {
                    updateBudgetsForTransaction(transaction);
                }
                ((Stage) saveButton.getScene().getWindow()).close();
            } else {
                errorLabel.setText("Lưu giao dịch thất bại!");
            }
        } catch (Exception ex) {
            errorLabel.setText("Lỗi: " + ex.getMessage());
        }
    }
    
    /**
     * Update budgets when a new transaction is added
     */
    private void updateBudgetsForTransaction(Transaction transaction) throws SQLException {
        // Get all budgets for this category
        List<Budget> budgets = budgetDAO.getBudgetsByCategory(transaction.getCategory().getId());
        
        // Initialize ObserverManager if not already done
        ObserverManager observerManager = ObserverManager.getInstance();
          for (Budget budget : budgets) {
            // Check if this budget is active (transaction date within budget period)
            LocalDateTime transactionDate = transaction.getDateTime();
            if (transactionDate != null && 
                transactionDate.isAfter(budget.getStartDate().minusDays(1)) && 
                transactionDate.isBefore(budget.getEndDate().plusDays(1))) {
                
                // Register observers for this budget
                observerManager.registerBudgetObservers(budget);
                
                // Add transaction to budget (this will trigger observer notifications)
                budget.addTransaction(transaction);
                
                // Update budget in database
                budgetDAO.update(budget);
                
                System.out.println("Updated budget: " + budget.getName() + 
                                 " | Spent: " + budget.getSpent() + "/" + budget.getLimit());
            }
        }
    }
}
