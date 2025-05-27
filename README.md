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