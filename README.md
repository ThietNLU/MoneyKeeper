# OOD Project 
Tôi sẽ giải thích mẫu Observer Pattern trong project MoneyKeeper này cho bạn hiểu. Hãy để tôi phân tích cấu trúc và cách hoạt động của nó. Hãy để tôi tiếp tục tìm hiểu các interface và classes cốt lõi để giải thích đầy đủ hơn: Bây giờ tôi có đủ thông tin để giải thích chi tiết mẫu Observer Pattern trong project MoneyKeeper. Đây là một implementation rất hoàn chỉnh và phức tạp của mẫu Observer Pattern:

## 🎯 **Observer Pattern trong MoneyKeeper - Giải Thích Chi Tiết**

### **1. CẤU TRÚC CỐT LÕI (Core Structure)**

#### **a) Interface IObserver** 
```java
public interface IObserver {
    void update(NotificationData notificationData);  // Phương thức chính
    void update(String message);                     // Phương thức cũ (backward compatibility)
}
```

#### **b) Interface ISubject**
```java
public interface ISubject {
    void addObserver(IObserver observer);            // Thêm observer
    void removeObserver(IObserver observer);         // Xóa observer  
    void notifyObservers(NotificationData data);     // Thông báo tới tất cả observers
}
```

### **2. CÁC LOẠI SUBJECT (Observable Objects)**

#### **a) Wallet - Ví tiền** 
```java
public class Wallet implements ISubject {
    private List<IObserver> observers = new ArrayList<>();
    
    // Khi số dư thay đổi
    public void income(double amount) {
        double oldBalance = this.balance;
        this.balance += amount;
        notifyBalanceChanged(oldBalance, this.balance, "income");
    }
    
    // Thông báo khi số dư thay đổi
    private void notifyBalanceChanged(double oldBalance, double newBalance, String type) {
        NotificationData notification = new NotificationData(
            NotificationType.WALLET_UPDATE,
            String.format("Wallet '%s' balance changed from %.2f to %.2f (%s)", 
                         name, oldBalance, newBalance, type),
            "Wallet", this
        );
        notifyObservers(notification);
        
        // Kiểm tra số dư thấp
        if (isLowBalance() && !wasLowBalance(oldBalance)) {
            NotificationData lowBalanceAlert = new NotificationData(
                NotificationType.LOW_BALANCE,
                String.format("Wallet '%s' has low balance: %.2f", name, newBalance),
                "Wallet", this
            );
            notifyObservers(lowBalanceAlert);
        }
    }
}
```

#### **b) Budget - Ngân sách**
```java
public class Budget implements ISubject {
    private List<IObserver> observers = new ArrayList<>();
    
    // Khi cập nhật số tiền đã chi
    public void updateSpent(double newSpent) {
        this.spent = newSpent;
        checkBudgetLimits();  // Kiểm tra giới hạn
        
        NotificationData notification = new NotificationData(
            NotificationType.INFO,
            String.format("Budget '%s' updated. Spent: %.2f / Limit: %.2f", 
                         name, spent, limit),
            "Budget", this
        );
        notifyObservers(notification);
    }
    
    // Kiểm tra và cảnh báo khi vượt giới hạn
    private void checkBudgetLimits() {
        if (isOverLimit()) {
            NotificationData alert = new NotificationData(
                NotificationType.BUDGET_ALERT,
                String.format("Budget '%s' is over limit! Spent: %.2f / Limit: %.2f", 
                             name, spent, limit),
                "Budget", this
            );
            notifyObservers(alert);
        }
    }
}
```

### **3. CÁC LOẠI OBSERVER (Observers)**

#### **a) NotificationObserver - Observer cơ bản**
```java
public class NotificationObserver implements IObserver {
    private String observerName;
    
    @Override
    public void update(NotificationData notificationData) {
        System.out.println(String.format("[%s] %s", observerName, notificationData.toString()));
    }
}
```

#### **b) UINotificationObserver - Observer cho giao diện**
```java
public class UINotificationObserver implements IObserver {
    private Label statusLabel;
    private ObservableList<String> notificationList;
    
    @Override
    public void update(NotificationData notificationData) {
        Platform.runLater(() -> {
            // Cập nhật status label
            if (statusLabel != null) {
                statusLabel.setText(notificationData.getMessage());
                // Đặt màu theo loại thông báo
                switch (notificationData.getType()) {
                    case ERROR: statusLabel.setStyle("-fx-text-fill: red;"); break;
                    case WARNING: statusLabel.setStyle("-fx-text-fill: orange;"); break;
                    case INFO: statusLabel.setStyle("-fx-text-fill: green;"); break;
                }
            }
            
            // Thêm vào danh sách thông báo
            if (notificationList != null) {
                notificationList.add(0, notificationData.toString());
            }
            
            // Hiển thị alert popup nếu cần
            if (shouldShowAlert(notificationData.getType())) {
                showAlert(notificationData);
            }
        });
    }
}
```

#### **c) BudgetAlertObserver - Observer chuyên biệt cho ngân sách**
```java
public class BudgetAlertObserver implements IObserver {
    private Consumer<Budget> onBudgetOverLimit;    // Callback khi vượt giới hạn
    private Consumer<Budget> onBudgetNearLimit;    // Callback khi gần giới hạn
    
    @Override
    public void update(NotificationData notificationData) {
        if (notificationData.getData() instanceof Budget) {
            Budget budget = (Budget) notificationData.getData();
            
            switch (notificationData.getType()) {
                case BUDGET_ALERT:
                    if (budget.isOverLimit() && onBudgetOverLimit != null) {
                        onBudgetOverLimit.accept(budget);
                    }
                    break;
            }
        }
    }
}
```

#### **d) WalletUpdateObserver - Observer chuyên biệt cho ví**
```java
public class WalletUpdateObserver implements IObserver {
    private Consumer<Wallet> onBalanceChanged;     // Callback khi số dư thay đổi
    private Consumer<Wallet> onLowBalance;         // Callback khi số dư thấp
    
    @Override
    public void update(NotificationData notificationData) {
        switch (notificationData.getType()) {
            case WALLET_UPDATE:
                if (notificationData.getData() instanceof Wallet && onBalanceChanged != null) {
                    onBalanceChanged.accept((Wallet) notificationData.getData());
                }
                break;
            case LOW_BALANCE:
                if (notificationData.getData() instanceof Wallet && onLowBalance != null) {
                    onLowBalance.accept((Wallet) notificationData.getData());
                }
                break;
        }
    }
}
```

### **4. NOTIFICATION MANAGER - Singleton quản lý tập trung**

```java
public class NotificationManager {
    private static NotificationManager instance;
    private List<IObserver> globalObservers;              // Observers toàn cục
    private ObservableList<String> notificationHistory;   // Lịch sử thông báo
    private UINotificationObserver uiObserver;            // Observer cho UI
    
    // Singleton pattern
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }
    
    // Broadcast thông báo tới tất cả global observers
    public void broadcast(NotificationData notificationData) {
        for (IObserver observer : globalObservers) {
            observer.update(notificationData);
        }
    }
    
    // Setup observers cho wallet
    public void setupWalletObservers(Wallet wallet) {
        WalletUpdateObserver walletObserver = new WalletUpdateObserver(
            // Khi số dư thay đổi
            changedWallet -> broadcast(NotificationType.WALLET_UPDATE, 
                "Wallet balance updated", "WalletManager"),
            // Khi số dư thấp  
            lowBalanceWallet -> broadcast(NotificationType.LOW_BALANCE,
                "Low balance alert", "WalletManager"),
            // Khi thêm giao dịch
            transaction -> broadcast(NotificationType.TRANSACTION_ADDED,
                "Transaction added", "TransactionManager")
        );
        
        wallet.addObserver(walletObserver);
        // Thêm tất cả global observers
        for (IObserver globalObserver : globalObservers) {
            wallet.addObserver(globalObserver);
        }
    }
}
```

### **5. NOTIFICATION DATA - Dữ liệu thông báo**

```java
public class NotificationData {
    private NotificationType type;        // Loại thông báo
    private String message;              // Nội dung
    private String source;               // Nguồn gửi
    private LocalDateTime timestamp;      // Thời gian
    private Object data;                 // Dữ liệu đi kèm (Wallet, Budget, Transaction...)
}

public enum NotificationType {
    INFO, WARNING, ERROR,
    BUDGET_ALERT,           // Cảnh báo ngân sách
    WALLET_UPDATE,          // Cập nhật ví
    TRANSACTION_ADDED,      // Thêm giao dịch
    LOW_BALANCE            // Số dư thấp
}
```

### **6. CÁCH HOẠT ĐỘNG TRONG DEMO**

#### **a) Trong HomeController:**
```java
public class HomeController implements Initializable {
    private NotificationManager notificationManager;
    
    @Override
    public void initialize() {
        // Khởi tạo notification manager
        notificationManager = NotificationManager.getInstance();
        notificationManager.initializeUIObserver(statusLabel);
        
        // Setup observers cho tất cả wallets và budgets có sẵn
        setupObservers();
        
        // Setup demo UI
        setupObserverDemo();
    }
    
    // Xử lý khi thêm giao dịch test
    @FXML
    private void handleAddTestTransaction() {
        // Tạo transaction mới
        Transaction testTransaction = new Transaction();
        testTransaction.setWallet(selectedWallet);
        testTransaction.setAmount(amount);
        testTransaction.setStrategy(new ExpenseTransactionStrategy());
        
        // Lưu transaction (sẽ trigger observers)
        transactionDAO.save(testTransaction);
        
        // Thông báo thành công
        if (enableAlertsCheckBox.isSelected()) {
            showAlert("Giao dịch đã được thêm! Observer pattern đã thông báo các thay đổi.");
        }
    }
}
```

### **7. LUỒNG HOẠT ĐỘNG (Flow)**

1. **Khởi tạo:** NotificationManager được tạo (Singleton)
2. **Setup Observers:** Tất cả wallets và budgets được gắn observers
3. **Sự kiện xảy ra:** User thêm transaction hoặc cập nhật budget
4. **Subject thông báo:** Wallet/Budget gọi `notifyObservers()`
5. **Observers nhận thông báo:** Tất cả observers được gọi `update()`
6. **Xử lý thông báo:** 
   - Console observer in ra console
   - UI observer cập nhật giao diện
   - Specialized observers thực hiện hành động cụ thể
7. **UI cập nhật:** ListView hiển thị thông báo mới, status label thay đổi màu

### **8. LỢI ÍCH CỦA IMPLEMENTATION NÀY**

- **🔄 Real-time updates:** UI tự động cập nhật khi dữ liệu thay đổi
- **🔗 Loose coupling:** Components không phụ thuộc trực tiếp vào nhau  
- **📊 Centralized management:** NotificationManager quản lý tất cả thông báo
- **🎯 Specialized observers:** Mỗi loại observer xử lý riêng loại thông báo
- **📱 Multi-channel notifications:** Console, UI, alerts đồng thời
- **🔧 Extensible:** Dễ dàng thêm loại observer và notification mới
- **🌐 Type-safe:** Sử dụng enum và strong typing
- **📜 History tracking:** Lưu lịch sử tất cả thông báo

Đây là một implementation rất tinh tế và professional của Observer Pattern, kết hợp nhiều design patterns khác như Singleton, Strategy, và sử dụng functional programming với lambda expressions!