# 🎯 **HƯỚNG DẪN SỬ DỤNG OBSERVER PATTERN TRONG MONEYKEEPER**

## **TỔNG QUAN**

Observer Pattern trong MoneyKeeper được thiết kế để theo dõi và thông báo các thay đổi trong hệ thống tài chính một cách thời gian thực. Pattern này giúp tách biệt logic nghiệp vụ khỏi logic hiển thị và thông báo.

---

## **1. CẤU TRÚC CỐT LÕI**

### **Interface IObserver**
```java
public interface IObserver {
    void update(NotificationData notificationData);  // Phương thức chính
    void update(String message);                     // Backward compatibility
}
```

### **Interface ISubject**  
```java
public interface ISubject {
    void addObserver(IObserver observer);            // Thêm observer
    void removeObserver(IObserver observer);         // Xóa observer  
    void notifyObservers(NotificationData data);     // Thông báo observers
}
```

### **Class NotificationData**
```java
public class NotificationData {
    private NotificationType type;     // Loại thông báo
    private String message;            // Nội dung thông báo
    private String source;             // Nguồn thông báo
    private LocalDateTime timestamp;   // Thời gian
    private Object data;               // Dữ liệu kèm theo
}
```

### **Enum NotificationType**
```java
public enum NotificationType {
    INFO, WARNING, ERROR,           // Thông báo cơ bản
    BUDGET_ALERT,                   // Cảnh báo ngân sách
    WALLET_UPDATE,                  // Cập nhật ví
    TRANSACTION_ADDED,              // Thêm giao dịch  
    LOW_BALANCE                     // Số dư thấp
}
```

---

## **2. CÁC SUBJECT CHÍNH**

### **2.1. Wallet (Subject)**
Wallet là Subject chính theo dõi:
- Thay đổi số dư
- Thêm giao dịch mới
- Cảnh báo số dư thấp

**Cách sử dụng:**
```java
// Tạo wallet
Wallet wallet = new Wallet("Ví chính", 1000000, user);

// Thêm observer
UINotificationObserver uiObserver = new UINotificationObserver(statusLabel);
wallet.addObserver(uiObserver);

// Khi có thay đổi, observers sẽ được thông báo tự động
wallet.updateBalance(500000);  // Tự động thông báo observers
```

### **2.2. Budget (Subject)**
Budget theo dõi:
- Thay đổi số tiền đã chi
- Vượt quá giới hạn ngân sách
- Cập nhật trạng thái ngân sách

**Cách sử dụng:**
```java
// Tạo budget
Budget budget = new Budget("Ăn uống", 2000000, startDate, endDate, category);

// Thêm observer
budget.addObserver(uiObserver);

// Cập nhật số tiền đã chi
budget.updateSpent(1500000);  // Tự động thông báo observers
```

---

## **3. CÁC LOẠI OBSERVER**

### **3.1. UINotificationObserver**
Observer chuyên biệt cho JavaFX UI:

```java
// Khởi tạo với status label
UINotificationObserver uiObserver = new UINotificationObserver(statusLabel);

// Khởi tạo với notification list
ObservableList<String> notifications = FXCollections.observableArrayList();
UINotificationObserver listObserver = new UINotificationObserver(notifications);

// Khởi tạo đầy đủ
UINotificationObserver fullObserver = new UINotificationObserver(statusLabel, notifications);
fullObserver.setShowAlerts(true);  // Hiển thị popup alerts
```

**Tính năng:**
- Cập nhật status label với màu sắc theo loại thông báo
- Thêm thông báo vào danh sách hiển thị
- Hiển thị popup alerts cho thông báo quan trọng
- Chạy trên JavaFX Application Thread

### **3.2. NotificationObserver**
Observer cơ bản in ra console:

```java
NotificationObserver consoleObserver = new NotificationObserver("ConsoleLogger");
wallet.addObserver(consoleObserver);
```

### **3.3. WalletUpdateObserver**  
Observer chuyên biệt cho wallet với callback functions:

```java
WalletUpdateObserver walletObserver = new WalletUpdateObserver(
    wallet -> System.out.println("Balance changed: " + wallet.getBalance()),
    wallet -> showLowBalanceAlert(wallet),
    transaction -> logTransaction(transaction)
);
```

---

## **4. NOTIFICATION MANAGER**

NotificationManager quản lý observers toàn cục:

```java
// Singleton pattern
NotificationManager manager = NotificationManager.getInstance();

// Khởi tạo UI observer
manager.initializeUIObserver(statusLabel);

// Thêm global observer
manager.addGlobalObserver(consoleObserver);

// Broadcast thông báo toàn cục
manager.broadcast(new NotificationData(
    NotificationType.INFO, 
    "System started", 
    "Application"
));

// Setup observers cho wallet/budget tự động
manager.setupWalletObservers(wallet);
manager.setupBudgetObservers(budget);
```

---

## **5. CÁCH SỬ DỤNG TRONG CONTROLLER**

### **5.1. Khởi tạo trong Controller**

```java
public class YourController implements Initializable {
    @FXML private Label statusLabel;
    @FXML private ListView<String> notificationListView;
    
    private NotificationManager notificationManager;
    private ObservableList<String> notifications;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo notification manager
        notificationManager = NotificationManager.getInstance();
        
        // Setup UI notifications
        notifications = FXCollections.observableArrayList();
        notificationListView.setItems(notifications);
        
        // Khởi tạo UI observer
        UINotificationObserver uiObserver = new UINotificationObserver(statusLabel, notifications);
        uiObserver.setShowAlerts(true);
        notificationManager.addGlobalObserver(uiObserver);
        
        // Setup observers cho dữ liệu có sẵn
        setupExistingObservers();
    }
    
    private void setupExistingObservers() {
        try {
            // Setup cho tất cả wallets
            List<Wallet> wallets = walletDAO.getAll();
            for (Wallet wallet : wallets) {
                notificationManager.setupWalletObservers(wallet);
            }
            
            // Setup cho tất cả budgets  
            List<Budget> budgets = budgetDAO.getAll();
            for (Budget budget : budgets) {
                notificationManager.setupBudgetObservers(budget);
            }
        } catch (SQLException e) {
            showError("Error setting up observers: " + e.getMessage());
        }
    }
}
```

### **5.2. Xử lý events với Observer**

```java
@FXML
private void addTransaction() {
    try {
        // Tạo transaction
        Transaction transaction = new Transaction(amount, description, category, wallet);
        
        // Thêm vào wallet - sẽ tự động notify observers
        wallet.addTransaction(transaction);
        
        // Lưu vào database
        transactionDAO.save(transaction);
        
        // Thông báo toàn cục
        notificationManager.broadcast(new NotificationData(
            NotificationType.INFO,
            "Transaction added successfully",
            "Controller"
        ));
        
    } catch (Exception e) {
        // Thông báo lỗi
        notificationManager.broadcast(new NotificationData(
            NotificationType.ERROR,
            "Failed to add transaction: " + e.getMessage(),
            "Controller"
        ));
    }
}

@FXML 
private void updateBudget() {
    try {
        Budget budget = budgetComboBox.getValue();
        double newSpent = Double.parseDouble(budgetSpentField.getText());
        
        // Cập nhật budget - sẽ tự động notify observers
        budget.updateSpent(newSpent);
        
        // Lưu vào database
        budgetDAO.update(budget);
        
    } catch (NumberFormatException e) {
        notificationManager.broadcast(new NotificationData(
            NotificationType.ERROR,
            "Invalid amount format",
            "Controller"
        ));
    }
}
```

---

## **6. DEMO OBSERVER PATTERN**

### **6.1. Chạy Demo trong ứng dụng**

1. **Mở trang chủ**: Tìm section "Demo Observer Pattern - Đầy đủ tính năng"
2. **Thêm giao dịch**: Quan sát thông báo real-time khi balance thay đổi  
3. **Cập nhật ngân sách**: Xem cảnh báo khi vượt giới hạn
4. **Test observers**: Thử nghiệm các loại observer khác nhau
5. **Mô phỏng lỗi**: Kiểm tra xử lý lỗi

### **6.2. Kiểm tra Observer hoạt động**

```java
// Test programmatically
@FXML
private void testObservers() {
    // Tạo test notification
    NotificationData testData = new NotificationData(
        NotificationType.INFO,
        "Testing observer pattern at " + LocalDateTime.now(),
        "Test"
    );
    
    // Broadcast test
    notificationManager.broadcast(testData);
    
    // Test wallet observer
    if (!wallets.isEmpty()) {
        Wallet testWallet = wallets.get(0);
        testWallet.updateBalance(testWallet.getBalance() + 1000);
    }
}
```

---

## **7. BEST PRACTICES**

### **7.1. Khi tạo Subject mới**
```java
// ✅ Luôn khởi tạo observers list
public class MySubject implements ISubject {
    private List<IObserver> observers = new ArrayList<>();
    
    // ✅ Thêm observer setup trong NotificationManager
    // ✅ Notify với NotificationData thay vì String
}
```

### **7.2. Khi tạo Observer mới**
```java
// ✅ Implement IObserver interface
public class MyObserver implements IObserver {
    @Override
    public void update(NotificationData data) {
        // ✅ Xử lý theo type của notification
        switch (data.getType()) {
            case WALLET_UPDATE:
                // Handle wallet update
                break;
            case BUDGET_ALERT:
                // Handle budget alert  
                break;
        }
    }
}
```

### **7.3. Error Handling**
```java
@Override
public void notifyObservers(NotificationData data) {
    for (IObserver observer : observers) {
        try {
            observer.update(data);
        } catch (Exception e) {
            // ✅ Log lỗi nhưng không dừng notify chain
            System.err.println("Observer error: " + e.getMessage());
        }
    }
}
```

### **7.4. Memory Management**
```java
// ✅ Remove observers khi không cần
@Override
public void dispose() {
    wallet.removeObserver(this);
    budget.removeObserver(this);
}
```

---

## **8. TROUBLESHOOTING**

### **8.1. Observer không được notify**
- Kiểm tra observer đã được add chưa: `wallet.addObserver(observer)`
- Kiểm tra method notify có được gọi: `notifyObservers(data)`
- Kiểm tra exception trong observer update method

### **8.2. UI không cập nhật**
- Đảm bảo UI observer chạy trên JavaFX thread: `Platform.runLater()`
- Kiểm tra statusLabel và notificationList đã được inject chưa
- Verify observer được add vào NotificationManager

### **8.3. Performance issues**
- Limit số lượng observers
- Tránh heavy operations trong update methods
- Consider async processing cho observers phức tạp

---

## **9. EXAMPLE IMPLEMENTATIONS**

Xem các file sau để hiểu cách implement:
- `ObserverTestController.java` - Demo controller đầy đủ
- `HomeController_fixed.java` - Integration vào trang chủ
- `NotificationManager.java` - Quản lý observers toàn cục
- `UINotificationObserver.java` - UI observer implementation

**Chạy ứng dụng và mở Observer Demo để xem pattern hoạt động trực tiếp!**
