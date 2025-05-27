# OOD Project
## Strategy Pattern
## Factory Pattern
## MVC Pattern
## DAO Pattern
## Singleton Pattern
## Observer Pattern

Tôi sẽ phân tích và giải thích Observer Pattern trong project MoneyKeeper này. Hãy để tôi kiểm tra các file liên quan để hiểu rõ cách triển khai. Dựa trên việc phân tích các file trong project MoneyKeeper, tôi sẽ giải thích chi tiết Observer Pattern đã được triển khai. Đây là một phân tích toàn diện:

## **Observer Pattern trong Project MoneyKeeper**

### **1. 🏗️ Cấu trúc tổng thể của Observer Pattern**

Observer Pattern trong project này được triển khai theo chuẩn Gang of Four với các thành phần chính:

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

### **2. 📋 Chi tiết triển khai các thành phần**

#### **🔹 Observer Interface**
```java
public interface Observer {
    void update(String message, Object data);
}
```

#### **🔹 Subject Interface**
```java
public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(String message, Object data);
}
```

#### **🔹 AbstractSubject Class**
```java
public abstract class AbstractSubject implements Subject {
    protected List<Observer> observers = new ArrayList<>();
    
    // Triển khai các method quản lý observers
    // Đảm bảo không duplicate observers
    // Thông báo đến tất cả observers đã đăng ký
}
```

---

### **3. 🏦 Budget Observer Implementation**

**Budget** làm Subject thông báo về:
- **`BUDGET_WARNING`**: Khi chi tiêu đạt 80% giới hạn
- **`BUDGET_EXCEEDED`**: Khi chi tiêu vượt quá 100% giới hạn
- **`TRANSACTION_ADDED`**: Khi có giao dịch mới được thêm vào budget

```java
public class Budget extends AbstractSubject {
    private void checkLimitAndNotify() {
        double percentage = (spent / limit) * 100;
        
        if (percentage >= 100) {
            notifyObservers("BUDGET_EXCEEDED", this);
        } else if (percentage >= 80) {
            notifyObservers("BUDGET_WARNING", this);
        }
    }
}
```

---

### **4. 💰 Wallet Observer Implementation**

**Wallet** được mở rộng để làm Subject thông báo về:
- **`WALLET_BALANCE_UPDATED`**: Khi số dư thay đổi
- **`WALLET_LOW_BALANCE`**: Khi số dư thấp (< 10% hoặc < 100,000 VND)
- **`WALLET_CRITICALLY_LOW`**: Khi số dư rất thấp (< 50,000 VND)
- **`WALLET_NEGATIVE_BALANCE`**: Khi số dư âm (thấu chi)
- **`TRANSACTION_ADDED_TO_WALLET`**: Khi thêm giao dịch vào ví

```java
public class Wallet extends AbstractSubject {
    public void updateBalance(double newBalance) {
        double oldBalance = this.balance;
        this.balance = newBalance;
        checkBalanceConditions(oldBalance);
    }
    
    private void checkBalanceConditions(double oldBalance) {
        // Kiểm tra các điều kiện và thông báo tương ứng
        notifyObservers("WALLET_BALANCE_UPDATED", this);
        
        if (balance < 0) {
            notifyObservers("WALLET_NEGATIVE_BALANCE", this);
        } else if (isCriticallyLowBalance()) {
            notifyObservers("WALLET_CRITICALLY_LOW", this);
        } else if (isLowBalance()) {
            notifyObservers("WALLET_LOW_BALANCE", this);
        }
    }
}
```

---

### **5. 🔔 NotificationObserver - Xử lý thông báo**

Hiển thị popup alerts cho người dùng:

```java
public class NotificationObserver implements Observer {
    @Override
    public void update(String message, Object data) {
        switch (message) {
            case "BUDGET_EXCEEDED":
                handleBudgetExceeded(data); // Hiển thị alert cảnh báo vượt ngân sách
                break;
            case "WALLET_LOW_BALANCE":
                handleWalletLowBalance(data); // Hiển thị alert số dư thấp
                break;
            // ... các cases khác
        }
    }
}
```

---

### **6. 🖥️ UIUpdateObserver - Cập nhật giao diện**

Cập nhật các ObservableList trong JavaFX:

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
                case "WALLET_LOW_BALANCE":
                    addNotification("Cảnh báo: Ví có số dư thấp!");
                    break;
                // ... các cases khác
            }
        });
    }
}
```

---

### **7. 🎛️ ObserverManager - Quản lý tập trung**

Singleton pattern để quản lý tất cả observers:

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
}
```

---

### **8. 🧪 Testing và Validation**

Project có các file test để kiểm tra Observer Pattern:

#### **🔹 SimpleObserverValidation.java**
- Test cơ bản không cần JavaFX
- Kiểm tra Budget và Wallet observer notifications
- Test đăng ký/hủy đăng ký observers

#### **🔹 ObserverPatternTest.java**
- JUnit tests với JavaFX
- Test integration với UI components
- Kiểm tra ObserverManager functionality

---

### **9. ✅ Ưu điểm của implementation này**

1. **🔄 Loose Coupling**: Subjects không biết chi tiết về observers
2. **📈 Scalability**: Dễ thêm observers mới
3. **🎯 Single Responsibility**: Mỗi observer có nhiệm vụ riêng
4. **🔧 Centralized Management**: ObserverManager quản lý tập trung
5. **⚡ Real-time Updates**: Thông báo ngay lập tức khi có thay đổi
6. **🎨 UI Integration**: Tích hợp tốt với JavaFX ObservableList

---

### **10. 📝 Kết luận**

Observer Pattern trong MoneyKeeper được triển khai rất chuyên nghiệp với:
- **Architecture chuẩn**: Theo đúng mẫu thiết kế Gang of Four
- **Đầy đủ tính năng**: Hỗ trợ cả Budget và Wallet notifications
- **Tích hợp UI**: Cập nhật giao diện real-time
- **Error handling**: Xử lý lỗi và edge cases
- **Testing coverage**: Có đầy đủ tests validation

Pattern này giúp ứng dụng MoneyKeeper có khả năng thông báo tức thời khi ngân sách vượt giới hạn hoặc ví có số dư thấp, cải thiện trải nghiệm người dùng đáng kể.