# 🏗️ Design Patterns Analysis - MoneyKeeper Project

## 📋 Tổng quan về các Design Patterns được sử dụng

Project MoneyKeeper sử dụng **6 design patterns chính** để tạo ra một kiến trúc phần mềm vững chắc, dễ bảo trì và mở rộng:

1. **Observer Pattern** - Thông báo real-time
2. **Singleton Pattern** - Quản lý tài nguyên độc nhất
3. **DAO Pattern** - Truy cập dữ liệu
4. **Strategy Pattern** - Xử lý giao dịch linh hoạt
5. **Factory Pattern** - Tạo đối tượng
6. **MVC Pattern** - Kiến trúc tổng thể

---

## 🔔 1. Observer Pattern

### 📐 **Cấu trúc và Thành phần**

#### **Core Interfaces:**
- **`Observer.java`**: Interface định nghĩa method `update(String message, Object data)`
- **`Subject.java`**: Interface quản lý observers (`addObserver`, `removeObserver`, `notifyObservers`)

#### **Abstract Implementation:**
- **`AbstractSubject.java`**: Triển khai cơ bản của Subject, quản lý danh sách observers

#### **Concrete Subjects:**
- **`Budget.java`**: Thông báo khi ngân sách vượt giới hạn
- **`Wallet.java`**: Thông báo khi số dư thay đổi

#### **Concrete Observers:**
- **`NotificationObserver.java`**: Hiển thị popup thông báo
- **`UIUpdateObserver.java`**: Cập nhật giao diện người dùng

#### **Management:**
- **`ObserverManager.java`**: Quản lý tập trung observers (Singleton)

### 💡 **Implementation Example:**

```java
// Budget Subject - Tự động thông báo khi vượt giới hạn
public class Budget extends AbstractSubject {
    private void checkBudgetLimits() {
        if (isOverLimit()) {
            notifyObservers("BUDGET_EXCEEDED", this);
        } else if (isNearLimit()) {
            notifyObservers("BUDGET_WARNING", this);
        }
    }
    
    public void addTransaction(Transaction trans) {
        this.transactions.add(trans);
        processTrans(trans);
        checkBudgetLimits(); // ✅ Tự động notify
    }
}

// Observer Implementation - Xử lý thông báo
public class NotificationObserver implements Observer {
    @Override
    public void update(String message, Object data) {
        switch (message) {
            case "BUDGET_EXCEEDED":
                showAlert("Ngân sách đã vượt quá giới hạn!");
                break;
            case "WALLET_LOW_BALANCE":
                showAlert("Số dư ví thấp!");
                break;
        }
    }
}
```

### ✅ **Ưu điểm:**
- **Real-time notifications** cho budget và wallet
- **Loose coupling** giữa subjects và observers
- **Scalable** - dễ thêm observers mới
- **Thread-safe** với JavaFX Platform.runLater()

---

## 🔒 2. Singleton Pattern

### 📐 **Implementation trong Project**

#### **`DBConnection.java`** - Database Connection Management:
```java
public class DBConnection {
    private static DBConnection instance;
    private static Connection connection;
    
    private DBConnection() {
        // Private constructor
    }
    
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }
    
    public Connection getConnection() {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
}
```

#### **`ObserverManager.java`** - Observer Management:
```java
public class ObserverManager {
    private static ObserverManager instance;
    private NotificationObserver notificationObserver;
    private UIUpdateObserver uiUpdateObserver;
    
    public static ObserverManager getInstance() {
        if (instance == null) {
            instance = new ObserverManager();
        }
        return instance;
    }
    
    // Centralized observer registration
    public void registerBudgetObservers(Budget budget) {
        if (notificationObserver != null) {
            budget.addObserver(notificationObserver);
        }
        if (uiUpdateObserver != null) {
            budget.addObserver(uiUpdateObserver);
        }
    }
}
```

### ✅ **Ưu điểm:**
- **Đảm bảo duy nhất một instance** cho database connection
- **Quản lý tài nguyên hiệu quả**
- **Thread-safe** với double-checked locking
- **Global access point** cho observers management

---

## 💾 3. DAO (Data Access Object) Pattern

### 📐 **Cấu trúc DAO Layer**

#### **Base Interface:**
```java
public interface DAO<T> {
    void save(T entity);
    void update(T entity);
    void delete(int id);
    T findById(int id);
    List<T> getAll();
}
```

#### **Concrete DAO Implementations:**
- **`UserDAO.java`** - Quản lý dữ liệu User
- **`WalletDAO.java`** - Quản lý dữ liệu Wallet
- **`TransactionDAO.java`** - Quản lý dữ liệu Transaction
- **`BudgetDAO.java`** - Quản lý dữ liệu Budget
- **`CategoryDAO.java`** - Quản lý dữ liệu Category

### 💡 **Implementation Example:**

```java
public class TransactionDAO implements DAO<Transaction> {
    private DBConnection dbConnection = DBConnection.getInstance();
    
    @Override
    public void save(Transaction transaction) {
        String sql = "INSERT INTO transactions (amount, description, category_id, wallet_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, transaction.getAmount());
            stmt.setString(2, transaction.getDescription());
            stmt.setInt(3, transaction.getCategory().getId());
            stmt.setInt(4, transaction.getWallet().getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving transaction", e);
        }
    }
    
    @Override
    public List<Transaction> getAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        // Implementation...
        return transactions;
    }
    
    // Specific methods for Transaction
    public List<Transaction> getTransactionsByWallet(int walletId) {
        // Custom query implementation
    }
    
    public List<Transaction> getTransactionsByDateRange(LocalDate start, LocalDate end) {
        // Date range query implementation
    }
}
```

### ✅ **Ưu điểm:**
- **Separation of concerns** - tách biệt logic database
- **Testable** - dễ mock cho unit testing
- **Consistency** - interface chung cho tất cả entities
- **Maintainable** - dễ thay đổi database logic

---

## 🎯 4. Strategy Pattern

### 📐 **Transaction Processing Strategy**

#### **Strategy Interface:**
```java
public interface TransactionStrategy {
    void processTransaction(Transaction transaction, Wallet wallet);
    String getTransactionType();
}
```

#### **Concrete Strategies:**

**Income Strategy:**
```java
public class IncomeTransactionStrategy implements TransactionStrategy {
    @Override
    public void processTransaction(Transaction transaction, Wallet wallet) {
        double currentBalance = wallet.getBalance();
        wallet.setBalance(currentBalance + transaction.getAmount());
        
        // Log income transaction
        System.out.println("Processed INCOME: +" + transaction.getAmount());
    }
    
    @Override
    public String getTransactionType() {
        return "INCOME";
    }
}
```

**Expense Strategy:**
```java
public class ExpenseTransactionStrategy implements TransactionStrategy {
    @Override
    public void processTransaction(Transaction transaction, Wallet wallet) {
        double currentBalance = wallet.getBalance();
        wallet.setBalance(currentBalance - transaction.getAmount());
        
        // Check for negative balance
        if (wallet.getBalance() < 0) {
            System.out.println("Warning: Wallet balance is negative!");
        }
        
        System.out.println("Processed EXPENSE: -" + transaction.getAmount());
    }
    
    @Override
    public String getTransactionType() {
        return "EXPENSE";
    }
}
```

#### **Context Class:**
```java
public class Transaction {
    private TransactionStrategy strategy;
    private double amount;
    private String description;
    private Category category;
    private Wallet wallet;
    
    public void setStrategy(TransactionStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void processWallet() {
        if (strategy != null) {
            strategy.processTransaction(this, wallet);
        }
    }
}
```

### ✅ **Ưu điểm:**
- **Flexible transaction processing** - dễ thêm loại giao dịch mới
- **Open/Closed Principle** - mở rộng không sửa đổi code cũ
- **Runtime switching** - có thể thay đổi strategy khi runtime

---

## 🏭 5. Factory Pattern

### 📐 **Transaction Factory Implementation**

#### **Factory Interface:**
```java
public interface TransactionFactory {
    TransactionStrategy createStrategy();
    Transaction createTransaction(double amount, String description, Category category, Wallet wallet);
}
```

#### **Concrete Factories:**

**Income Transaction Factory:**
```java
public class IncomeTransactionFactory implements TransactionFactory {
    @Override
    public TransactionStrategy createStrategy() {
        return new IncomeTransactionStrategy();
    }
    
    @Override
    public Transaction createTransaction(double amount, String description, Category category, Wallet wallet) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setCategory(category);
        transaction.setWallet(wallet);
        transaction.setStrategy(createStrategy());
        transaction.setTransactionDate(LocalDateTime.now());
        
        return transaction;
    }
}
```

**Expense Transaction Factory:**
```java
public class ExpenseTransactionFactory implements TransactionFactory {
    @Override
    public TransactionStrategy createStrategy() {
        return new ExpenseTransactionStrategy();
    }
    
    @Override
    public Transaction createTransaction(double amount, String description, Category category, Wallet wallet) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setCategory(category);
        transaction.setWallet(wallet);
        transaction.setStrategy(createStrategy());
        transaction.setTransactionDate(LocalDateTime.now());
        
        return transaction;
    }
}
```

#### **Factory Usage in Controllers:**
```java
public class AddTransactionController {
    public void createTransaction(String type, double amount, String description, Category category, Wallet wallet) {
        TransactionFactory factory;
        
        switch (type.toUpperCase()) {
            case "INCOME":
                factory = new IncomeTransactionFactory();
                break;
            case "EXPENSE":
                factory = new ExpenseTransactionFactory();
                break;
            default:
                throw new IllegalArgumentException("Unknown transaction type: " + type);
        }
        
        Transaction transaction = factory.createTransaction(amount, description, category, wallet);
        transaction.processWallet(); // Execute strategy
        
        // Save to database
        transactionDAO.save(transaction);
    }
}
```

### ✅ **Ưu điểm:**
- **Encapsulated object creation** - ẩn chi tiết tạo đối tượng
- **Consistency** - đảm bảo objects được tạo đúng cách
- **Extensible** - dễ thêm factory mới cho transaction types khác

---

## 🏛️ 6. MVC (Model-View-Controller) Pattern

### 📐 **Kiến trúc MVC trong MoneyKeeper**

#### **Model Layer:**
```
src/main/java/ood/application/moneykeeper/model/
├── User.java          - Entity user
├── Wallet.java        - Entity wallet (Observer Subject)
├── Transaction.java   - Entity transaction (với Strategy)
├── Budget.java        - Entity budget (Observer Subject)
├── Category.java      - Entity category
└── Report.java        - Entity report
```

#### **View Layer:**
```
src/main/resources/ood/application/moneykeeper/
├── mainview_test.fxml    - Main application view
├── home_test.fxml        - Home dashboard
├── add_transaction.fxml  - Transaction form
├── budget_test.fxml      - Budget management
├── wallet_test.fxml      - Wallet management
├── transaction_test.fxml - Transaction history
├── category_test.fxml    - Category management
├── settings_test.fxml    - Settings page
└── report.fxml          - Reports view
```

#### **Controller Layer:**
```
src/main/java/ood/application/moneykeeper/controller/
├── MainViewController.java      - Main application controller
├── HomeController.java          - Dashboard logic
├── AddTransactionController.java - Transaction creation
├── BudgetController.java        - Budget management
├── WalletController.java        - Wallet operations
├── TransactionController.java   - Transaction history
├── CategoryController.java      - Category CRUD
├── SettingsController.java      - Settings management
└── ReportController.java        - Report generation
```

### 💡 **Controller Integration Example:**

```java
@FXML
public class AddTransactionController {
    // Dependencies injection (DAO layer)
    private TransactionDAO transactionDAO;
    private WalletDAO walletDAO;
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;
    
    // FXML UI components (View binding)
    @FXML private TextField amountField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private ComboBox<Wallet> walletComboBox;
    @FXML private ToggleGroup transactionTypeGroup;
    
    @FXML
    private void initialize() {
        // Initialize UI components with data from Model
        loadCategories();
        loadWallets();
        setupValidation();
    }
    
    @FXML
    private void handleSaveTransaction() {
        // Get data from View
        String type = getSelectedTransactionType();
        double amount = Double.parseDouble(amountField.getText());
        String description = descriptionField.getText();
        Category category = categoryComboBox.getValue();
        Wallet wallet = walletComboBox.getValue();
        
        // Business logic - create transaction using Factory
        TransactionFactory factory = type.equals("INCOME") 
            ? new IncomeTransactionFactory() 
            : new ExpenseTransactionFactory();
            
        Transaction transaction = factory.createTransaction(amount, description, category, wallet);
        
        // Process transaction (Strategy pattern)
        transaction.processWallet();
        
        // Update Model through DAO
        transactionDAO.save(transaction);
        walletDAO.update(wallet);
        
        // Update Budget and trigger Observer notifications
        updateBudgetsForTransaction(transaction);
        
        // Navigate back to previous view
        closeWindow();
    }
    
    private void updateBudgetsForTransaction(Transaction transaction) {
        List<Budget> budgets = budgetDAO.getBudgetsByCategory(transaction.getCategory().getId());
        ObserverManager observerManager = ObserverManager.getInstance();
        
        for (Budget budget : budgets) {
            // Register observers
            observerManager.registerBudgetObservers(budget);
            // Add transaction will automatically notify observers
            budget.addTransaction(transaction);
            budgetDAO.update(budget);
        }
    }
}
```

### ✅ **Ưu điểm của MVC trong MoneyKeeper:**
- **Clear separation of concerns** - tách biệt rõ ràng logic, dữ liệu, giao diện
- **Testable** - controller có thể test độc lập với UI
- **Maintainable** - thay đổi UI không ảnh hưởng business logic
- **Reusable** - model và business logic có thể tái sử dụng

---

## 🔗 7. Pattern Integration & Synergy

### 🎯 **Cách các Patterns hoạt động cùng nhau:**

```java
// Ví dụ: Thêm giao dịch mới - tích hợp tất cả patterns
public class TransactionWorkflow {
    
    public void addNewTransaction(String type, double amount, String description, int categoryId, int walletId) {
        
        // 1. DAO Pattern - Load data from database
        CategoryDAO categoryDAO = new CategoryDAO();
        WalletDAO walletDAO = new WalletDAO();
        BudgetDAO budgetDAO = new BudgetDAO();
        
        Category category = categoryDAO.findById(categoryId);
        Wallet wallet = walletDAO.findById(walletId);
        
        // 2. Factory Pattern - Create transaction with appropriate strategy
        TransactionFactory factory = type.equals("INCOME") 
            ? new IncomeTransactionFactory() 
            : new ExpenseTransactionFactory();
            
        Transaction transaction = factory.createTransaction(amount, description, category, wallet);
        
        // 3. Strategy Pattern - Process transaction
        transaction.processWallet(); // Executes IncomeStrategy or ExpenseStrategy
        
        // 4. Singleton Pattern - Get database connection and observer manager
        DBConnection db = DBConnection.getInstance();
        ObserverManager observerManager = ObserverManager.getInstance();
        
        // 5. DAO Pattern - Save to database
        TransactionDAO transactionDAO = new TransactionDAO();
        transactionDAO.save(transaction);
        walletDAO.update(wallet);
        
        // 6. Observer Pattern - Notify about wallet changes
        observerManager.registerWalletObservers(wallet);
        wallet.addTransaction(transaction); // Triggers WALLET_BALANCE_UPDATED notification
        
        // 7. Observer Pattern - Update budgets and notify
        List<Budget> affectedBudgets = budgetDAO.getBudgetsByCategory(categoryId);
        for (Budget budget : affectedBudgets) {
            observerManager.registerBudgetObservers(budget);
            budget.addTransaction(transaction); // May trigger BUDGET_WARNING or BUDGET_EXCEEDED
            budgetDAO.update(budget);
        }
        
        // 8. MVC Pattern - Controller updates View through Observer notifications
        // UIUpdateObserver automatically updates JavaFX ObservableList
        // NotificationObserver shows popup alerts if needed
    }
}
```

### 🌟 **Pattern Synergy Benefits:**

1. **Observer + Singleton**: ObserverManager ensures consistent notification system
2. **Strategy + Factory**: Factory creates transactions with appropriate processing strategy
3. **DAO + Singleton**: DBConnection provides consistent database access across all DAOs
4. **MVC + Observer**: Controllers trigger business logic, Observers update UI automatically
5. **All Patterns**: Complete separation of concerns with automatic real-time updates

---

## 📊 8. Pattern Usage Summary

| Pattern | Primary Purpose | Key Classes | Benefits |
|---------|----------------|-------------|----------|
| **Observer** | Real-time notifications | `Budget`, `Wallet`, `NotificationObserver`, `UIUpdateObserver` | Loose coupling, automatic UI updates |
| **Singleton** | Resource management | `DBConnection`, `ObserverManager` | Single instance, global access |
| **DAO** | Data access abstraction | `TransactionDAO`, `WalletDAO`, `BudgetDAO` | Testable, maintainable data layer |
| **Strategy** | Transaction processing | `IncomeStrategy`, `ExpenseStrategy` | Flexible transaction types |
| **Factory** | Object creation | `IncomeTransactionFactory`, `ExpenseTransactionFactory` | Consistent object creation |
| **MVC** | Application architecture | Controllers, FXML Views, Model entities | Separation of concerns |

---

## ✅ 9. Kết luận

MoneyKeeper project demonstrate **excellent use of design patterns** với:

### 🎯 **Architectural Excellence:**
- **6 design patterns** được tích hợp seamlessly
- **Clear separation of concerns** với MVC
- **Real-time user experience** với Observer Pattern
- **Flexible business logic** với Strategy và Factory

### 💪 **Code Quality Benefits:**
- **Maintainable**: Dễ sửa đổi và mở rộng
- **Testable**: Các component có thể test độc lập
- **Scalable**: Dễ thêm features mới
- **Robust**: Error handling và thread safety

### 🚀 **Future Extensions:**
Với kiến trúc pattern này, project có thể dễ dàng mở rộng:
- Thêm transaction types mới (Strategy + Factory)
- Thêm notification channels mới (Observer)
- Thêm data sources mới (DAO)
- Thêm UI components mới (MVC + Observer)

Project MoneyKeeper là một **excellent example** của việc áp dụng design patterns trong Java desktop application, tạo ra một hệ thống vừa mạnh mẽ vừa linh hoạt.

Tôi sẽ phân tích các đối tượng chính của dự án MoneyKeeper bằng cách khám phá cấu trúc code và các model classes. Bây giờ tôi sẽ khám phá cấu trúc model để xác định các đối tượng chính: Dựa trên việc phân tích cấu trúc code và các file model, đây là **các đối tượng chính của dự án MoneyKeeper**:

## 🎯 **CÁC ĐỐI TƯỢNG CHÍNH (CORE ENTITIES)**

### 1. **👤 User (Người dùng)**
```java
public class User {
    private String id;
    private String name;
    private WalletManager wallets;
    private BudgetManager budgets;
}
```
- **Vai trò**: Đối tượng trung tâm đại diện cho người sử dụng ứng dụng
- **Chức năng**: Quản lý tất cả ví và ngân sách của một người dùng
- **Quan hệ**: Sở hữu nhiều Wallet và Budget thông qua Manager classes

### 2. **💰 Wallet (Ví tiền)**
```java
public class Wallet extends AbstractSubject {
    private String id;
    private String name;
    private double balance;
    private List<Transaction> transactions;
    private User owner;
    private LocalDateTime creationDate;
}
```
- **Vai trò**: Lưu trữ tiền và quản lý giao dịch
- **Chức năng**: Theo dõi số dư, chứa danh sách giao dịch
- **Observer Pattern**: Thông báo khi số dư thay đổi
- **Quan hệ**: Thuộc về 1 User, chứa nhiều Transaction

### 3. **💸 Transaction (Giao dịch)**
```java
public class Transaction {
    private String tId;
    private Wallet wallet;
    private ITransactionStrategy strategy;
    private double amount;
    private LocalDateTime dateTime;
    private Category category;
    private String description;
}
```
- **Vai trò**: Đại diện cho một giao dịch tài chính
- **Chức năng**: Lưu trữ thông tin chi tiết về giao dịch (thu/chi)
- **Strategy Pattern**: Sử dụng strategy để xử lý khác nhau cho thu nhập và chi tiêu
- **Quan hệ**: Thuộc về 1 Wallet, có 1 Category

### 4. **📊 Budget (Ngân sách)**
```java
public class Budget extends AbstractSubject {
    private String id;
    private String name;
    private double limit;
    private double spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Category category;
    private List<Transaction> transactions;
}
```
- **Vai trò**: Quản lý giới hạn chi tiêu theo category
- **Chức năng**: Theo dõi số tiền đã chi và so sánh với giới hạn
- **Observer Pattern**: Thông báo khi vượt giới hạn hoặc gần đạt giới hạn
- **Quan hệ**: Liên kết với 1 Category, chứa nhiều Transaction

### 5. **🏷️ Category (Danh mục)**
```java
public class Category {
    private String id;
    private String name;
    private boolean isExpense;
}
```
- **Vai trò**: Phân loại các giao dịch
- **Chức năng**: Xác định loại giao dịch (thu nhập/chi tiêu)
- **Quan hệ**: Được sử dụng bởi nhiều Transaction và Budget

---

## 🔧 **CÁC ĐỐI TƯỢNG HỖ TRỢ (SUPPORTING OBJECTS)**

### 6. **📁 Manager Classes**
- **`WalletManager`**: Quản lý tập hợp các ví của user
- **`BudgetManager`**: Quản lý tập hợp các ngân sách của user

### 7. **🎮 Strategy Objects**
- **`ITransactionStrategy`**: Interface cho strategy pattern
- **`IncomeTransactionStrategy`**: Xử lý giao dịch thu nhập
- **`ExpenseTransactionStrategy`**: Xử lý giao dịch chi tiêu

### 8. **🏭 Factory Objects**
- **`ITransactionFactory`**: Interface cho factory pattern
- **`IncomeTransactionFactory`**: Tạo giao dịch thu nhập
- **`ExpenseTransactionFactory`**: Tạo giao dịch chi tiêu

---

## 🎯 **CÁC ĐỐI TƯỢNG CONTROLLER (MVC PATTERN)**

### 9. **🎛️ Controller Classes**
- **`UserController`**: Quản lý thao tác với User
- **`WalletController`**: Quản lý thao tác với Wallet
- **`TransactionController`**: Quản lý thao tác với Transaction
- **`BudgetController`**: Quản lý thao tác với Budget
- **`CategoryController`**: Quản lý thao tác với Category
- **`HomeController`**: Controller cho trang chủ
- **`AddTransactionController`**: Controller cho việc thêm giao dịch
- **`ReportController`**: Controller cho báo cáo

---

## 💾 **CÁC ĐỐI TƯỢNG DAO (DATA ACCESS OBJECTS)**

### 10. **🗄️ DAO Classes**
- **`UserDAO`**: Truy cập dữ liệu User
- **`WalletDAO`**: Truy cập dữ liệu Wallet
- **`TransactionDAO`**: Truy cập dữ liệu Transaction
- **`BudgetDAO`**: Truy cập dữ liệu Budget
- **`CategoryDAO`**: Truy cập dữ liệu Category
- **`DBConnection`**: Singleton quản lý kết nối database

---

## 🔔 **CÁC ĐỐI TƯỢNG OBSERVER PATTERN**

### 11. **👁️ Observer Classes**
- **`Observer`**: Interface observer
- **`Subject`**: Interface subject
- **`AbstractSubject`**: Abstract implementation của subject
- **`NotificationObserver`**: Observer để hiển thị thông báo
- **`UIUpdateObserver`**: Observer để cập nhật giao diện
- **`ObserverManager`**: Singleton quản lý observers

---

## 📈 **QUAN HỆ GIỮA CÁC ĐỐI TƯỢNG**

```
User (1) ──────────────┐
    │                  │
    │ (1:n)            │ (1:n)
    ▼                  ▼
Wallet (n) ────────► Budget (n)
    │                  │
    │ (1:n)            │ (n:n)
    ▼                  ▼
Transaction (n) ───► Category (1)
    │
    │ (1:1)
    ▼
TransactionStrategy
```

**Đây là kiến trúc đối tượng hoàn chỉnh của MoneyKeeper**, với 5 entity chính (User, Wallet, Transaction, Budget, Category) làm nền tảng, được hỗ trợ bởi các pattern implementations và quản lý thông qua MVC architecture với Observer pattern để real-time updates.