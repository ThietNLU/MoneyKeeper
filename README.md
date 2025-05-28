# OOD Project
## Strategy Pattern
## Factory Pattern
## MVC Pattern
## DAO Pattern
## Singleton Pattern
## Observer Pattern
git ad
# 📋 Phân tích Observer Pattern trong Project MoneyKeeper

## 🏗️ 1. Cấu trúc tổng thể của Observer Pattern

### 📐 **Các thành phần chính:**

#### **🔸 Core Interfaces:**
- **Observer.java**: Interface định nghĩa method `update(String message, Object data)`
- **Subject.java**: Interface định nghĩa các method quản lý observers (`addObserver`, `removeObserver`, `notifyObservers`)

#### **🔸 Abstract Implementation:**
- **AbstractSubject.java**: Triển khai cơ bản của Subject interface, quản lý danh sách observers và thông báo

#### **🔸 Concrete Subjects (Observable Objects):**
- **Budget.java**: Extends AbstractSubject, thông báo khi ngân sách vượt giới hạn
- **Wallet.java**: Extends AbstractSubject, thông báo khi số dư thay đổi

#### **🔸 Concrete Observers:**
- **NotificationObserver.java**: Hiển thị thông báo popup cho người dùng
- **UIUpdateObserver.java**: Cập nhật giao diện người dùng

#### **🔸 Management:**
- **ObserverManager.java**: Quản lý tập trung các observers (Singleton pattern)

---

## 🔍 2. Chi tiết implementation

### **💼 Budget Subject Implementation**

```java
public class Budget extends AbstractSubject {
    // Kiểm tra giới hạn ngân sách và thông báo
    private void checkBudgetLimits() {
        if (isOverLimit()) {
            notifyObservers("BUDGET_EXCEEDED", this);
        } else if (isNearLimit()) {
            notifyObservers("BUDGET_WARNING", this);
        }
    }
    
    // Được gọi khi thêm transaction hoặc cập nhật spent
    public void addTransaction(Transaction trans) {
        this.transactions.add(trans);
        processTrans(trans);
        checkBudgetLimits(); // ✅ Tự động kiểm tra và notify
    }
}
```

**Thông báo các sự kiện:**
- **`BUDGET_WARNING`**: Khi chi tiêu đạt 80% giới hạn
- **`BUDGET_EXCEEDED`**: Khi chi tiêu vượt quá 100% giới hạn

### **💰 Wallet Subject Implementation**

```java
public class Wallet extends AbstractSubject {
    // Kiểm tra điều kiện số dư và thông báo
    private void checkBalanceConditions(double oldBalance) {
        notifyObservers("WALLET_BALANCE_UPDATED", this);
        
        if (isCriticallyLowBalance()) {
            notifyObservers("WALLET_CRITICALLY_LOW", this);
        } else if (isLowBalance() && oldBalance >= 100000.0) {
            notifyObservers("WALLET_LOW_BALANCE", this);
        }
        
        if (this.balance < 0) {
            notifyObservers("WALLET_NEGATIVE_BALANCE", this);
        }
    }
    
    // Được gọi khi thêm transaction
    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        double oldBalance = this.balance;
        transaction.processWallet();
        
        notifyObservers("TRANSACTION_ADDED_TO_WALLET", transaction);
        checkBalanceConditions(oldBalance); // ✅ Tự động kiểm tra và notify
    }
}
```

**Thông báo các sự kiện:**
- **`WALLET_BALANCE_UPDATED`**: Khi số dư thay đổi
- **`WALLET_LOW_BALANCE`**: Khi số dư thấp (< 100,000 VND)
- **`WALLET_CRITICALLY_LOW`**: Khi số dư rất thấp (< 50,000 VND)
- **`WALLET_NEGATIVE_BALANCE`**: Khi số dư âm (thấu chi)
- **`TRANSACTION_ADDED_TO_WALLET`**: Khi thêm giao dịch vào ví

---

## 🔔 3. Concrete Observers

### **📢 NotificationObserver**
Xử lý thông báo popup cho người dùng:

```java
public class NotificationObserver implements Observer {
    @Override
    public void update(String message, Object data) {
        switch (message) {
            case "BUDGET_EXCEEDED":
                handleBudgetExceeded(data); // Hiển thị Alert popup
                break;
            case "BUDGET_WARNING":
                handleBudgetWarning(data);   // Hiển thị Alert popup
                break;
            case "WALLET_LOW_BALANCE":
                handleWalletLowBalance(data); // Hiển thị Alert popup
                break;
            // ... các cases khác
        }
    }
}
```

### **🖥️ UIUpdateObserver**
Cập nhật JavaFX ObservableList và giao diện:

```java
public class UIUpdateObserver implements Observer {
    private ObservableList<Transaction> transactionList;
    private ObservableList<Budget> budgetList;
    private ObservableList<String> notificationHistory;
    
    @Override
    public void update(String message, Object data) {
        Platform.runLater(() -> {
            switch (message) {
                case "TRANSACTION_ADDED":
                    updateTransactionList(data);
                    addNotification("Đã thêm giao dịch mới");
                    break;
                case "BUDGET_EXCEEDED":
                    addNotification("Cảnh báo: Ngân sách đã vượt quá giới hạn!");
                    break;
                // ... các cases khác
            }
        });
    }
}
```

---

## 🎛️ 4. ObserverManager - Quản lý tập trung

Sử dụng Singleton pattern để quản lý tất cả observers:

```java
public class ObserverManager {
    private static ObserverManager instance;
    private NotificationObserver notificationObserver;
    private UIUpdateObserver uiUpdateObserver;
    
    // Đăng ký observers cho Budget
    public void registerBudgetObservers(Budget budget) {
        if (notificationObserver != null) {
            budget.addObserver(notificationObserver);
        }
        if (uiUpdateObserver != null) {
            budget.addObserver(uiUpdateObserver);
        }
    }
    
    // Đăng ký observers cho Wallet
    public void registerWalletObservers(Wallet wallet) {
        if (notificationObserver != null) {
            wallet.addObserver(notificationObserver);
        }
        if (uiUpdateObserver != null) {
            wallet.addObserver(uiUpdateObserver);
        }
    }
    
    // Các method notify trực tiếp
    public void notifyTransactionAdded(Transaction transaction) {
        if (notificationObserver != null) {
            notificationObserver.update("TRANSACTION_ADDED", transaction);
        }
        if (uiUpdateObserver != null) {
            uiUpdateObserver.update("TRANSACTION_ADDED", transaction);
        }
    }
}
```

---

## 🔗 5. Tích hợp với Controllers

### **📊 BudgetController Integration**
```java
public class BudgetController {
    private void initializeObservers() {
        // Đăng ký observers cho tất cả budgets hiện có
        List<Budget> allBudgets = budgetDAO.getAll();
        for (Budget budget : allBudgets) {
            ObserverManager.getInstance().registerBudgetObservers(budget);
        }
    }
}
```

### **💸 AddTransactionController Integration**
```java
public class AddTransactionController {
    private void updateBudgetsForTransaction(Transaction transaction) {
        List<Budget> budgets = budgetDAO.getBudgetsByCategory(transaction.getCategory().getId());
        ObserverManager observerManager = ObserverManager.getInstance();
        
        for (Budget budget : budgets) {
            // Đăng ký observers cho budget
            observerManager.registerBudgetObservers(budget);
            
            // Thêm transaction sẽ tự động trigger observers
            budget.addTransaction(transaction); // ✅ Tự động notify!
        }
    }
}
```

---

## ✅ 6. Ưu điểm của implementation này

1. **🔄 Loose Coupling**: Subjects không biết chi tiết về observers
2. **📈 Scalability**: Dễ thêm observers mới mà không thay đổi subjects
3. **🎯 Single Responsibility**: Mỗi observer có nhiệm vụ riêng biệt
4. **🔧 Centralized Management**: ObserverManager quản lý tập trung
5. **⚡ Real-time Updates**: Thông báo ngay lập tức khi có thay đổi
6. **🎨 JavaFX Integration**: Tích hợp tốt với ObservableList
7. **🛡️ Error Handling**: Có xử lý lỗi trong ObserverManager
8. **🧪 Testable**: Có unit tests và validation

---

## 🔧 7. Các tính năng đặc biệt

### **🎚️ Threshold-based Notifications**
- Budget: Cảnh báo ở 80% và 100% giới hạn
- Wallet: Cảnh báo ở ngưỡng thấp (100k VND) và rất thấp (50k VND)

### **📱 Platform Integration**
- Sử dụng `Platform.runLater()` cho thread-safe UI updates
- JavaFX Alert dialogs cho notification popups

### **💾 Database Integration**  
- Tự động cập nhật database khi có thay đổi
- Đồng bộ observers khi load dữ liệu từ database

### **🔄 Lifecycle Management**
- Register/unregister observers khi cần thiết
- Singleton ObserverManager đảm bảo consistency

---

## 🎯 8. Kết luận

Observer Pattern trong MoneyKeeper được triển khai một cách **hoàn chỉnh và chuyên nghiệp**, với:

- ✅ **Cấu trúc rõ ràng** với interfaces và abstract classes
- ✅ **Tích hợp tốt** với JavaFX và database
- ✅ **Quản lý tập trung** qua ObserverManager
- ✅ **Real-time notifications** cho người dùng
- ✅ **Error handling** và thread safety
- ✅ **Unit tests** và validation

Pattern này giúp ứng dụng **tự động thông báo** khi:
- 💰 Ngân sách sắp đạt hoặc vượt giới hạn
- 🏦 Số dư ví thấp hoặc âm
- 📊 Có giao dịch mới được thêm vào

```
ThietNLU: Mối quan hệ giữa các dối tượng chính trong project

GitHub Copilot: Tôi sẽ phân tích mối quan hệ giữa các đối tượng chính trong project MoneyKeeper của bạn. Hãy để tôi khám phá cấu trúc và code để hiểu rõ hơn. Bây giờ hãy để tôi xem thêm một số file model chính để hiểu rõ hơn về cấu trúc: Dựa trên việc phân tích code của project MoneyKeeper, tôi sẽ trình bày mối quan hệ giữa các đối tượng chính trong hệ thống:

## Mối quan hệ giữa các đối tượng chính trong MoneyKeeper

### 🏗️ **1. Kiến trúc tổng thể**

Project MoneyKeeper được xây dựng theo mô hình **MVC (Model-View-Controller)** với các thành phần chính:

### 📊 **2. Các đối tượng Model chính và mối quan hệ**

#### **🔸 User (Người dùng)**
```java
public class User {
    private String id;
    private String name;
    private WalletManager wallets;     // 1-to-Many với Wallet
    private BudgetManager budgets;     // 1-to-Many với Budget
}
```

#### **🔸 Wallet (Ví tiền)**
```java
public class Wallet extends AbstractSubject {
    private String id;
    private String name;
    private double balance;
    private List<Transaction> transactions;  // 1-to-Many với Transaction
    private User owner;                     // Many-to-1 với User
    private LocalDateTime creationDate;
}
```

#### **🔸 Transaction (Giao dịch)**
```java
public class Transaction {
    private String tId;
    private Wallet wallet;              // Many-to-1 với Wallet
    private ITransactionStrategy strategy; // Strategy Pattern
    private double amount;
    private LocalDateTime dateTime;
    private Category category;          // Many-to-1 với Category
    private String description;
}
```

#### **🔸 Budget (Ngân sách)**
```java
public class Budget extends AbstractSubject {
    private String id;
    private String name;
    private double limit;
    private double spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Category category;              // Many-to-1 với Category
    private List<Transaction> transactions; // 1-to-Many với Transaction
}
```

#### **🔸 Category (Danh mục)**
```java
public class Category {
    private String id;
    private String name;
    private boolean isExpense;  // true: chi tiêu, false: thu nhập
}
```

### 🔗 **3. Mối quan hệ chính**

#### **📍 User → Wallet → Transaction**
- **User** có nhiều **Wallet** (1:N)
- **Wallet** có nhiều **Transaction** (1:N)
- Mỗi **Transaction** thuộc về một **Wallet** (N:1)

#### **📍 User → Budget → Category**
- **User** có nhiều **Budget** (1:N)
- **Budget** thuộc về một **Category** (N:1)
- **Category** có thể có nhiều **Budget** (1:N)

#### **📍 Transaction → Category**
- **Transaction** thuộc về một **Category** (N:1)
- **Category** có thể có nhiều **Transaction** (1:N)

#### **📍 Budget → Transaction**
- **Budget** theo dõi các **Transaction** cùng category trong khoảng thời gian (1:N)

### 🎯 **4. Design Patterns được sử dụng**

#### **🔸 Observer Pattern**
```java
// AbstractSubject (Subject)
public abstract class AbstractSubject implements Subject {
    protected List<Observer> observers;
    
    // Budget và Wallet extend AbstractSubject
    // Thông báo khi có thay đổi quan trọng
}

// Concrete Observers
- NotificationObserver: Hiển thị popup alerts
- UIUpdateObserver: Cập nhật giao diện
```

#### **🔸 Strategy Pattern**
```java
// ITransactionStrategy interface
- ExpenseTransactionStrategy: Xử lý giao dịch chi tiêu
- IncomeTransactionStrategy: Xử lý giao dịch thu nhập
```

#### **🔸 DAO Pattern**
```java
// Data Access Objects
- UserDAO, WalletDAO, TransactionDAO, BudgetDAO, CategoryDAO
// Tách biệt logic business và data access
```

#### **🔸 Manager Pattern**
```java
- WalletManager: Quản lý danh sách ví của user
- BudgetManager: Quản lý danh sách ngân sách của user
- ObserverManager: Quản lý tập trung các observers (Singleton)
```

### ⚡ **5. Luồng hoạt động chính**

#### **📝 Khi tạo Transaction mới:**
1. **User** chọn **Wallet** và **Category**
2. **Transaction** được tạo với **Strategy** tương ứng
3. **Wallet** cập nhật balance và thông báo observers
4. **Budget** cùng category được cập nhật spent
5. **Observer** system kiểm tra và thông báo:
   - Wallet balance thấp
   - Budget vượt/sắp vượt giới hạn

#### **📊 Khi kiểm tra ngân sách:**
1. **Budget** so sánh spent với limit
2. Nếu vượt 80%: notify "BUDGET_WARNING"
3. Nếu vượt 100%: notify "BUDGET_EXCEEDED"
4. **NotificationObserver** hiển thị popup
5. **UIUpdateObserver** cập nhật giao diện

### 🔧 **6. Tính năng quan trọng**

#### **🔔 Real-time Notifications:**
- Wallet balance thấp/âm
- Budget warning/exceeded
- Transaction updates

#### **📈 Data Consistency:**
- Tự động cập nhật wallet balance
- Tự động cập nhật budget spent
- Đồng bộ database

#### **🎨 UI Integration:**
- ObservableList để binding với JavaFX
- Platform.runLater() cho thread-safe updates
- Automatic refresh của tables/charts

### 💡 **7. Kết luận**

MoneyKeeper được thiết kế với:
- **Loose coupling** qua Observer pattern
- **High cohesion** trong từng module
- **Separation of concerns** rõ ràng
- **Easy maintenance** và **extensibility**

Các đối tượng chính hoạt động như một hệ sinh thái, trong đó mỗi thay đổi đều được thông báo và xử lý tự động, đảm bảo tính nhất quán của dữ liệu và trải nghiệm người dùng mượt mà.

ThietNLU: Kiểu quan hệ giữa các đối tượng chính

GitHub Copilot: Tôi sẽ phân tích chi tiết các kiểu quan hệ giữa các đối tượng chính trong project MoneyKeeper. Để có cái nhìn đầy đủ, hãy để tôi xem xét các file model và cấu trúc database. Summarized conversation historyTôi sẽ phân tích chi tiết các kiểu quan hệ giữa các đối tượng chính trong project MoneyKeeper. Hãy để tôi tổng hợp và phân tích sâu hơn về các mối quan hệ này. Bây giờ để tôi tìm hiểu thêm về UML association metadata và các kiểu quan hệ cụ thể trong class diagram: Summarized conversation historyTôi sẽ phân tích chi tiết các kiểu quan hệ giữa các đối tượng chính trong project MoneyKeeper dựa trên thông tin đã thu thập từ UML class diagram và source code. Giờ tôi sẽ phân tích chi tiết các kiểu quan hệ giữa các đối tượng chính trong project MoneyKeeper dựa trên dữ liệu đã thu thập: Dựa trên việc phân tích chi tiết source code và UML class diagram, tôi sẽ tổng hợp các kiểu quan hệ cụ thể trong project MoneyKeeper:

## **PHÂN TÍCH CHI TIẾT CÁC KIỂU QUAN HỆ TRONG MONEYKEEPER**

### **1. COMPOSITION RELATIONSHIPS (Quan hệ tổng hợp mạnh)**

#### **1.1 User → WalletManager & BudgetManager**
```
User (1) ←--contains--→ (1) WalletManager
User (1) ←--contains--→ (1) BudgetManager
```
- **Kiểu**: Composition 
- **Multiplicity**: 1:1
- **Lý do**: Khi User bị xóa, WalletManager và BudgetManager cũng bị xóa. Chúng không thể tồn tại độc lập.
- **Evidence**: User.java khởi tạo managers trong constructor và quản lý hoàn toàn lifecycle.

#### **1.2 WalletManager → Wallet Collection**
```
WalletManager (1) ←--contains--→ (0..*) Wallet
```
- **Kiểu**: Composition
- **Multiplicity**: 1:N
- **Lý do**: WalletManager quản lý hoàn toàn lifecycle của wallets trong collection.

#### **1.3 BudgetManager → Budget Collection**
```
BudgetManager (1) ←--contains--→ (0..*) Budget
```
- **Kiểu**: Composition
- **Multiplicity**: 1:N
- **Lý do**: BudgetManager quản lý hoàn toàn lifecycle của budgets trong collection.

### **2. AGGREGATION RELATIONSHIPS (Quan hệ tổng hợp yếu)**

#### **2.1 Wallet → Transaction Collection**
```
Wallet (1) ←--has--→ (0..*) Transaction
```
- **Kiểu**: Aggregation
- **Multiplicity**: 1:N
- **Evidence từ UML**: `"aggregation": "shared"` trong class diagram
- **Lý do**: Transaction có thể tồn tại độc lập sau khi Wallet bị xóa (thông qua database persistence).

#### **2.2 Budget → Transaction Collection (Many-to-Many through join table)**
```
Budget (0..*) ←--tracks--→ (0..*) Transaction
```
- **Kiểu**: Aggregation (Many-to-Many)
- **Multiplicity**: M:N
- **Implementation**: Qua bảng `Budget_Transaction` với composite primary key
- **Evidence**: Database schema và `BudgetDAO.saveBudgetTransactions()`

### **3. ASSOCIATION RELATIONSHIPS (Quan hệ liên kết)**

#### **3.1 Wallet → User (Owner)**
```
Wallet (0..*) ←--owned by--→ (1) User
```
- **Kiểu**: Association
- **Multiplicity**: N:1
- **Direction**: Navigable từ Wallet → User
- **Evidence**: Foreign key `owner_id` trong database, `Wallet.getOwner()`

#### **3.2 Transaction → Wallet**
```
Transaction (0..*) ←--belongs to--→ (1) Wallet
```
- **Kiểu**: Association
- **Multiplicity**: N:1
- **Direction**: Navigable từ Transaction → Wallet
- **Evidence**: Foreign key `wallet_id`, `Transaction.getWallet()`

#### **3.3 Transaction → Category**
```
Transaction (0..*) ←--categorized by--→ (1) Category
```
- **Kiểu**: Association
- **Multiplicity**: N:1
- **Direction**: Navigable từ Transaction → Category
- **Evidence**: Foreign key `category_id`, `Transaction.getCategory()`

#### **3.4 Budget → Category**
```
Budget (0..*) ←--monitors--→ (1) Category
```
- **Kiểu**: Association
- **Multiplicity**: N:1
- **Direction**: Navigable từ Budget → Category
- **Evidence**: Foreign key `category_id`, `Budget.getCategory()`

### **4. STRATEGY PATTERN RELATIONSHIPS**

#### **4.1 Transaction → ITransactionStrategy**
```
Transaction (1) ←--uses--→ (1) ITransactionStrategy
```
- **Kiểu**: Association (Strategy Pattern)
- **Multiplicity**: 1:1
- **Implementation**: `ExpenseTransactionStrategy` hoặc `IncomeTransactionStrategy`

### **5. OBSERVER PATTERN RELATIONSHIPS**

#### **5.1 AbstractSubject ← Budget, Wallet**
```
AbstractSubject (1) ←--extends--→ (0..*) Budget
AbstractSubject (1) ←--extends--→ (0..*) Wallet
```
- **Kiểu**: Inheritance + Observer Pattern
- **Multiplicity**: 1:N (inheritance)

#### **5.2 Observer Registration**
```
Budget/Wallet (1) ←--observes--→ (0..*) Observer
```
- **Kiểu**: Association (Observer Pattern)
- **Multiplicity**: 1:N

### **6. DAO PATTERN RELATIONSHIPS**

#### **6.1 DAO → DBConnection**
```
*DAO (0..*) ←--uses--→ (1) DBConnection (Singleton)
```
- **Kiểu**: Association
- **Multiplicity**: N:1

#### **6.2 DAO Dependencies**
```
TransactionDAO (1) ←--depends on--→ (1) WalletDAO
TransactionDAO (1) ←--depends on--→ (1) CategoryDAO
BudgetDAO (1) ←--depends on--→ (1) CategoryDAO
```
- **Kiểu**: Dependency Association
- **Multiplicity**: 1:1

### **7. TỔNG HỢP SƠ ĐỒ QUAN HỆ CHI TIẾT**

```
                    User (1)
                 ●━━━━━┳━━━━━●
            (contains) ┃ (contains)
                      ┃
        WalletManager (1)    BudgetManager (1)
              ●━━━━━━━━━━━━━━━━━━━●
         (contains)              (contains)
              ┃                      ┃
        Wallet (0..*)            Budget (0..*)
          │ ○ ←──(owned by)───       │ ○
          │ │                       │ │
          │ │  (has)                │ │ (tracks)
          │ └─────○ Transaction ○────┘ │
          │         (0..*)            │
          │           │ ○             │
          │           │ │(categorized │
          │           │  by)          │(monitors)
          │           │               │
          └───────────┼───────────────┘
                      ▽
                 Category (1)
                      ▲
                      │ (uses)
                      │
            ITransactionStrategy (1)
                   ▲
                   │
        ┌──────────┴──────────┐
ExpenseStrategy    IncomeStrategy
```

### **8. ĐÁNH GIÁ CONSISTENCY VÀ DESIGN QUALITY**

#### **✅ Điểm mạnh:**
1. **Separation of Concerns**: Rõ ràng giữa model, DAO, controller
2. **Strategy Pattern**: Linh hoạt xử lý expense/income
3. **Observer Pattern**: Notification system tốt
4. **Many-to-Many**: Đúng thiết kế cho Budget-Transaction

#### **⚠️ Điểm cần cải thiện:**
1. **Bidirectional Navigation**: Thiếu navigation từ Category → Transaction/Budget
2. **Cascade Operations**: Chưa rõ ràng về cascade delete rules
3. **Transaction Integrity**: Cần đảm bảo ACID trong complex operations

#### **📊 Multiplicity Summary:**
- **1:1**: User-WalletManager, User-BudgetManager, Transaction-Strategy
- **1:N**: Wallet-Transaction, User-Wallet, Category-Transaction, Category-Budget
- **M:N**: Budget-Transaction (qua join table)

