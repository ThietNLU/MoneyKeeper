# 🚀 **QUICK START: TÍCH HỢP OBSERVER PATTERN**

## **CÁCH NHANH NHẤT ĐỂ THÊM OBSERVER VÀO COMPONENT MỚI**

### **1. TẠO SUBJECT MỚI**

```java
public class MyNewSubject implements ISubject {
    private List<IObserver> observers = new ArrayList<>();
    private String data;
    
    // Implement required methods
    @Override
    public void addObserver(IObserver observer) {
        this.observers.add(observer);
    }
    
    @Override
    public void removeObserver(IObserver observer) {
        this.observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(NotificationData notificationData) {
        for (IObserver observer : observers) {
            try {
                observer.update(notificationData);
            } catch (Exception e) {
                System.err.println("Observer error: " + e.getMessage());
            }
        }
    }
    
    // Business method with notification
    public void updateData(String newData) {
        String oldData = this.data;
        this.data = newData;
        
        // Notify observers
        notifyObservers(new NotificationData(
            NotificationType.INFO,
            "Data updated from '" + oldData + "' to '" + newData + "'",
            "MyNewSubject",
            this
        ));
    }
}
```

### **2. TẠO OBSERVER MỚI**

```java
public class MyCustomObserver implements IObserver {
    private String observerName;
    
    public MyCustomObserver(String name) {
        this.observerName = name;
    }
    
    @Override
    public void update(NotificationData notificationData) {
        // Xử lý theo type
        switch (notificationData.getType()) {
            case INFO:
                handleInfo(notificationData);
                break;
            case WARNING:
                handleWarning(notificationData);
                break;
            case ERROR:
                handleError(notificationData);
                break;
            // Thêm các case khác nếu cần
        }
    }
    
    private void handleInfo(NotificationData data) {
        System.out.println("[INFO] " + observerName + ": " + data.getMessage());
    }
    
    private void handleWarning(NotificationData data) {
        System.out.println("[WARNING] " + observerName + ": " + data.getMessage());
    }
    
    private void handleError(NotificationData data) {
        System.err.println("[ERROR] " + observerName + ": " + data.getMessage());
    }
}
```

### **3. SETUP TRONG CONTROLLER**

```java
@FXML
public class MyController implements Initializable {
    @FXML private Label statusLabel;
    @FXML private ListView<String> notificationListView;
    
    private NotificationManager notificationManager;
    private MyNewSubject mySubject;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Get notification manager
        notificationManager = NotificationManager.getInstance();
        
        // 2. Setup UI observer
        ObservableList<String> notifications = FXCollections.observableArrayList();
        notificationListView.setItems(notifications);
        
        UINotificationObserver uiObserver = new UINotificationObserver(statusLabel, notifications);
        notificationManager.addGlobalObserver(uiObserver);
        
        // 3. Create and setup your subject
        mySubject = new MyNewSubject();
        MyCustomObserver customObserver = new MyCustomObserver("CustomLogger");
        mySubject.addObserver(customObserver);
        
        // 4. Also add to global notification manager if needed
        notificationManager.addGlobalObserver(customObserver);
    }
    
    @FXML
    private void onButtonClick() {
        // Update subject - observers will be notified automatically
        mySubject.updateData("New value at " + LocalDateTime.now());
    }
}
```

### **4. THÊM VÀO NOTIFICATION MANAGER (OPTIONAL)**

Nếu muốn quản lý tự động như Wallet/Budget:

```java
// Trong NotificationManager.java, thêm method:
public void setupMySubjectObservers(MyNewSubject subject) {
    // Add console observer
    if (consoleObserver != null) {
        subject.addObserver(consoleObserver);
    }
    
    // Add UI observer
    if (uiObserver != null) {
        subject.addObserver(uiObserver);
    }
    
    // Add any other global observers
    for (IObserver observer : globalObservers) {
        subject.addObserver(observer);
    }
}

// Sử dụng:
MyNewSubject subject = new MyNewSubject();
notificationManager.setupMySubjectObservers(subject);
```

### **5. CHECKLIST NHANH**

- [ ] ✅ Subject implement ISubject
- [ ] ✅ Observer implement IObserver  
- [ ] ✅ addObserver() được gọi
- [ ] ✅ notifyObservers() được gọi khi có thay đổi
- [ ] ✅ Exception handling trong notifyObservers()
- [ ] ✅ UI observer chạy trên JavaFX thread
- [ ] ✅ removeObserver() khi dispose

### **6. DEBUG TIPS**

```java
// Kiểm tra observers đã được add chưa
System.out.println("Observer count: " + observers.size());

// Log notifications
@Override
public void notifyObservers(NotificationData data) {
    System.out.println("Notifying " + observers.size() + " observers: " + data.getMessage());
    // ... existing code
}

// Test observer manually
mySubject.notifyObservers(new NotificationData(
    NotificationType.INFO,
    "Test notification",
    "Debug"
));
```

### **7. COMMON PATTERNS**

**Controller Integration:**
```java
// Trong initialize()
notificationManager = NotificationManager.getInstance();
notificationManager.initializeUIObserver(statusLabel);

// Setup existing data
loadDataAndSetupObservers();
```

**DAO Integration:**
```java
// Trong DAO save/update methods
public void save(MyEntity entity) {
    // Save to database
    database.save(entity);
    
    // Setup observers if it's a subject
    if (entity instanceof ISubject) {
        notificationManager.setupObservers((ISubject) entity);
    }
    
    // Broadcast save notification
    notificationManager.broadcast(new NotificationData(
        NotificationType.INFO,
        "Entity saved: " + entity.getId(),
        "DAO"
    ));
}
```

**Cleanup:**
```java
// Trong dispose/cleanup methods
@Override
public void dispose() {
    if (mySubject != null) {
        mySubject.removeObserver(myObserver);
    }
    notificationManager.removeGlobalObserver(myObserver);
}
```

---

**🎯 VÀO THẲNG DEMO:** Mở ứng dụng → Trang chủ → "Demo Observer Pattern" để xem hoạt động thực tế!
