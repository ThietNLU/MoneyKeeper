# BÁO CÁO: OBSERVER PATTERN & NOTIFICATION SYSTEM
## MoneyKeeper Application

---

## 1. TỔNG QUAN VỀ OBSERVER PATTERN

### 1.1 Định nghĩa
Observer Pattern là một behavioral design pattern cho phép một đối tượng (Subject) thông báo tự động đến nhiều đối tượng khác (Observers) khi trạng thái của nó thay đổi, mà không cần biết chi tiết về các observers này.

### 1.2 Vấn đề cần giải quyết trong MoneyKeeper
- **Cập nhật UI real-time**: Khi dữ liệu thay đổi (budget, wallet, transaction), UI cần được cập nhật ngay lập tức
- **Thông báo người dùng**: Hiển thị alerts khi budget vượt ngưỡng hoặc ví có số dư thấp
- **Loose coupling**: Tách biệt logic business với presentation layer
- **Scalability**: Dễ dàng thêm các loại thông báo mới mà không thay đổi code cũ

---

## 2. KIẾN TRÚC OBSERVER PATTERN TRONG MONEYKEEPER

### 2.1 Class Diagram
```
Observer Interface
    ↑
    ├── NotificationObserver (JavaFX Alerts)
    └── UIUpdateObserver (UI Refresh)

Subject Interface
    ↑
AbstractSubject (Implementation)
    ↑
    ├── Budget
    └── Wallet

ObserverManager (Singleton)
```

### 2.2 Các thành phần chính

#### **Observer Interface**
```java
public interface Observer {
    void update(String message, Object data);
}
```

#### **Subject Interface**
```java
public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(String message, Object data);
}
```

#### **AbstractSubject**
- Implement cơ bản của Subject interface
- Quản lý danh sách observers
- Cung cấp phương thức notify chung

---

## 3. CONCRETE IMPLEMENTATIONS

### 3.1 NotificationObserver
**Mục đích**: Hiển thị JavaFX alerts cho người dùng

**Chức năng**:
- Budget warning (80% limit reached)
- Budget exceeded (100% limit exceeded)
- Wallet low balance
- Wallet negative balance
- Transaction success/failure notifications

**Code Example**:
```java
@Override
public void update(String message, Object data) {
    Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("MoneyKeeper Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    });
}
```

### 3.2 UIUpdateObserver
**Mục đích**: Cập nhật JavaFX UI components

**Chức năng**:
- Refresh ObservableList trong TableView
- Cập nhật charts và statistics
- Real-time UI synchronization

**Code Example**:
```java
@Override
public void update(String message, Object data) {
    Platform.runLater(() -> {
        if (data instanceof ObservableList) {
            // Refresh UI components
            refreshTableViews();
            updateCharts();
        }
    });
}
```

---

## 4. SUBJECT IMPLEMENTATIONS

### 4.1 Budget Class
**Extends**: AbstractSubject

**Trigger Events**:
- Khi spending ratio đạt 80% (warning)
- Khi spending ratio đạt 100% (exceeded)
- Khi có transaction mới được thêm vào budget

**Key Methods**:
```java
public void addTransaction(Transaction transaction) {
    // Business logic
    updateSpentAmount();
    
    // Notify observers
    double ratio = getSpentAmount() / getBudgetAmount();
    if (ratio >= 0.8 && ratio < 1.0) {
        notifyObservers("Budget Warning: 80% spent", this);
    } else if (ratio >= 1.0) {
        notifyObservers("Budget Exceeded!", this);
    }
}
```

### 4.2 Wallet Class
**Extends**: AbstractSubject

**Trigger Events**:
- Khi balance thay đổi
- Khi balance < 100,000 (low balance warning)
- Khi balance < 0 (negative balance alert)

**Key Methods**:
```java
public void updateBalance(double amount) {
    this.balance += amount;
    
    // Notify observers
    if (balance < 0) {
        notifyObservers("Warning: Negative balance!", this);
    } else if (balance < 100000) {
        notifyObservers("Low balance warning", this);
    }
    
    notifyObservers("Balance updated", this);
}
```

---

## 5. OBSERVER MANAGER (SINGLETON PATTERN)

### 5.1 Mục đích
- Quản lý tập trung tất cả observers
- Đăng ký/hủy đăng ký observers một cách nhất quán
- Singleton để đảm bảo chỉ có một instance

### 5.2 Key Features
```java
public class ObserverManager {
    private static ObserverManager instance;
    private List<Observer> globalObservers;
    
    public static ObserverManager getInstance() {
        if (instance == null) {
            instance = new ObserverManager();
        }
        return instance;
    }
    
    public void registerObserver(Observer observer) {
        globalObservers.add(observer);
    }
    
    public void unregisterObserver(Observer observer) {
        globalObservers.remove(observer);
    }
}
```

---

## 6. TÍCH HỢP VỚI CONTROLLERS

### 6.1 BudgetController
```java
@FXML
private void initialize() {
    // Đăng ký observers
    ObserverManager manager = ObserverManager.getInstance();
    manager.registerObserver(new NotificationObserver());
    manager.registerObserver(new UIUpdateObserver(budgetTableView));
}
```

### 6.2 TransactionController
```java
private void addTransaction() {
    // Business logic
    Transaction transaction = createTransaction();
    
    // Trigger observer notifications
    budget.addTransaction(transaction);
    wallet.updateBalance(-transaction.getAmount());
}
```

---

## 7. DEMO SCENARIOS

### 7.1 Scenario 1: Budget Warning
1. User tạo budget 1,000,000 VND
2. User thêm transactions tổng 800,000 VND
3. **Observer Pattern triggers**: NotificationObserver hiển thị "Budget Warning: 80% spent"
4. **UI Update**: Budget progress bar chuyển màu vàng

### 7.2 Scenario 2: Wallet Low Balance
1. Wallet có balance 50,000 VND
2. User thực hiện transaction 
3. **Observer Pattern triggers**: NotificationObserver hiển thị "Low balance warning"
4. **UI Update**: Wallet balance hiển thị màu đỏ

### 7.3 Scenario 3: Real-time UI Sync
1. User thêm transaction mới
2. **Observer Pattern triggers**: UIUpdateObserver refresh TableView
3. **Result**: Dữ liệu hiển thị ngay lập tức mà không cần reload

---

## 8. LỢI ÍCH CỦA OBSERVER PATTERN TRONG MONEYKEEPER

### 8.1 Technical Benefits
- **Loose Coupling**: Model classes không phụ thuộc vào specific UI components
- **Open/Closed Principle**: Có thể thêm observers mới mà không modify existing code
- **Single Responsibility**: Mỗi observer có một nhiệm vụ cụ thể
- **Real-time Updates**: UI luôn sync với data state

### 8.2 Business Benefits
- **Better UX**: User nhận thông báo real-time
- **Financial Awareness**: Cảnh báo khi chi tiêu vượt mức
- **Data Consistency**: UI luôn hiển thị dữ liệu chính xác
- **Scalability**: Dễ dàng thêm loại thông báo mới

---

## 9. TESTING

### 9.1 Unit Tests
File: `ObserverPatternTest.java`
- Test observer registration/unregistration
- Test notification delivery
- Test multiple observers handling same event

### 9.2 Integration Tests
File: `SimpleObserverValidation.java`
- Console-based validation
- End-to-end observer flow testing

---

## 10. KẾT LUẬN

Observer Pattern trong MoneyKeeper đã được implement thành công với:

✅ **Complete Implementation**: Đầy đủ các thành phần Observer, Subject, ConcreteObserver, ConcreteSubject

✅ **Real-world Application**: Giải quyết vấn đề thực tế về notification và UI updates

✅ **Best Practices**: Sử dụng interfaces, abstract classes, và design principles

✅ **Scalable Architecture**: Dễ dàng extend cho các features mới

✅ **Integration**: Tích hợp tốt với JavaFX và MVC architecture

### Contribution to Project:
Observer Pattern đóng vai trò quan trọng trong việc tạo ra một ứng dụng responsive, user-friendly và maintainable, đảm bảo người dùng luôn được thông báo kịp thời về tình hình tài chính của họ.

---

## 11. DEMO CODE EXAMPLES

### Quick Demo Setup:
```java
// Tạo budget và wallet
Budget budget = new Budget("Monthly Budget", 1000000);
Wallet wallet = new Wallet("Main Wallet", 500000);

// Đăng ký observers
budget.addObserver(new NotificationObserver());
budget.addObserver(new UIUpdateObserver());

// Trigger events
budget.addTransaction(new Transaction("Coffee", 850000));
// → "Budget Warning: 80% spent" notification
// → UI updates automatically
```

---

*Báo cáo được chuẩn bị bởi: [Tên của bạn]*  
*Ngày: [Ngày hiện tại]*  
*Project: MoneyKeeper - OOD Course*
