package ood.application.moneykeeper.examples;

import ood.application.moneykeeper.model.*;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * VÍ DỤ THỰC TẾ: Cách sử dụng Observer Pattern trong MoneyKeeper
 */
public class ObserverPatternExamples {
    
    /**
     * VÍ DỤ 1: Thiết lập Observer cơ bản cho Wallet
     */
    public static void basicWalletObserverExample() {
        System.out.println("=== VÍ DỤ 1: BASIC WALLET OBSERVER ===");
        
        // Tạo wallet
        Wallet wallet = new Wallet("Ví tiết kiệm", 1000000, null);
        
        // Tạo observer đơn giản
        NotificationObserver consoleObserver = new NotificationObserver("WalletLogger");
        
        // Thêm observer vào wallet
        wallet.addObserver(consoleObserver);
        
        // Thực hiện các thao tác - observers sẽ được thông báo tự động
        System.out.println("Cập nhật số dư wallet...");
        wallet.updateBalance(1500000);  // Tăng số dư
        
        wallet.updateBalance(800000);   // Giảm số dư (có thể trigger low balance)
        
        // Kết quả: Console sẽ in ra các thông báo về thay đổi số dư
    }
    
    /**
     * VÍ DỤ 2: Observer cho Budget với cảnh báo vượt giới hạn
     */
    public static void budgetObserverExample() {
        System.out.println("\n=== VÍ DỤ 2: BUDGET OBSERVER ===");
        
        // Tạo category và budget
        Category foodCategory = new Category("Ăn uống", true);
        Budget monthlyFood = new Budget("Ngân sách ăn uống tháng 5", 2000000, 
                                      java.time.LocalDateTime.now(),
                                      java.time.LocalDateTime.now().plusMonths(1),
                                      foodCategory);
        
        // Tạo observer cho budget
        NotificationObserver budgetObserver = new NotificationObserver("BudgetTracker");
        monthlyFood.addObserver(budgetObserver);
        
        // Mô phỏng chi tiêu dần dần
        System.out.println("Mô phỏng chi tiêu hàng ngày...");
        monthlyFood.updateSpent(500000);    // Chi 500k
        monthlyFood.updateSpent(1200000);   // Chi thêm -> tổng 1.2M
        monthlyFood.updateSpent(1800000);   // Chi thêm -> tổng 1.8M
        monthlyFood.updateSpent(2100000);   // Chi thêm -> VƯỢT GIỚI HẠN!
        
        // Kết quả: Sẽ có cảnh báo khi vượt giới hạn ngân sách
    }
    
    /**
     * VÍ DỤ 3: UI Observer với JavaFX components
     */
    public static void uiObserverExample(Label statusLabel, ObservableList<String> notificationList) {
        System.out.println("\n=== VÍ DỤ 3: UI OBSERVER ===");
        
        // Tạo UI observer với cả status label và notification list
        UINotificationObserver uiObserver = new UINotificationObserver(statusLabel, notificationList);
        uiObserver.setShowAlerts(true);  // Bật popup alerts
        
        // Tạo wallet và budget
        Wallet wallet = new Wallet("Ví chính", 500000, null);
        Category expenseCategory = new Category("Chi tiêu", true);
        Budget budget = new Budget("Ngân sách tháng", 1000000,
                                 java.time.LocalDateTime.now(),
                                 java.time.LocalDateTime.now().plusMonths(1),
                                 expenseCategory);
        
        // Thêm UI observer vào cả wallet và budget
        wallet.addObserver(uiObserver);
        budget.addObserver(uiObserver);
        
        // Thực hiện các thao tác
        System.out.println("Thực hiện giao dịch...");
        wallet.updateBalance(300000);    // Số dư thấp -> cảnh báo màu cam
        budget.updateSpent(1200000);     // Vượt ngân sách -> cảnh báo màu đỏ
        
        // Kết quả: 
        // - Status label sẽ thay đổi màu sắc theo loại thông báo
        // - Notification list sẽ có thêm các thông báo mới
        // - Có thể hiển thị popup alerts cho thông báo quan trọng
    }
    
    /**
     * VÍ DỤ 4: WalletUpdateObserver với custom callbacks
     */
    public static void customWalletObserverExample() {
        System.out.println("\n=== VÍ DỤ 4: CUSTOM WALLET OBSERVER ===");
        
        // Tạo wallet observer với custom callback functions
        WalletUpdateObserver customObserver = new WalletUpdateObserver(
            // Callback khi balance thay đổi
            wallet -> {
                System.out.println("💰 Số dư đã thay đổi: " + wallet.getName() + 
                                 " -> " + String.format("%,.0f VND", wallet.getBalance()));
                // Có thể cập nhật chart, dashboard, etc.
            },
            
            // Callback khi số dư thấp
            wallet -> {
                System.out.println("⚠️ CẢNH BÁO: Số dư thấp trong ví " + wallet.getName() + 
                                 " (" + String.format("%,.0f VND", wallet.getBalance()) + ")");
                // Có thể gửi email, SMS, push notification
            },
            
            // Callback khi có transaction mới
            transaction -> {
                System.out.println("📝 Giao dịch mới: " + transaction.getDescription() + 
                                 " - " + String.format("%,.0f VND", transaction.getAmount()));
                // Có thể update report, analytics
            }
        );
        
        // Tạo wallet và thêm observer
        Wallet wallet = new Wallet("Ví demo", 100000, null);
        wallet.addObserver(customObserver);
        
        // Test các scenarios
        System.out.println("Testing balance changes...");
        wallet.updateBalance(150000);  // Tăng số dư
        wallet.updateBalance(50000);   // Giảm số dư -> low balance
        wallet.updateBalance(80000);   // Tăng lại
        
        // Test transaction (cần tạo transaction object và add vào wallet)
        // Transaction newTransaction = new Transaction(20000, "Mua coffee", category, wallet);
        // wallet.addTransaction(newTransaction);
    }
    
    /**
     * VÍ DỤ 5: Sử dụng NotificationManager cho quản lý toàn cục
     */
    public static void notificationManagerExample(Label statusLabel) {
        System.out.println("\n=== VÍ DỤ 5: NOTIFICATION MANAGER ===");
        
        // Lấy singleton instance
        NotificationManager manager = NotificationManager.getInstance();
        
        // Khởi tạo UI observer cho toàn ứng dụng
        manager.initializeUIObserver(statusLabel);
        
        // Thêm console observer toàn cục
        NotificationObserver globalConsole = new NotificationObserver("GlobalLogger");
        manager.addGlobalObserver(globalConsole);
        
        // Tạo một số đối tượng business
        Wallet wallet1 = new Wallet("Ví 1", 1000000, null);
        Wallet wallet2 = new Wallet("Ví 2", 500000, null);
        
        Category category = new Category("Test", true);
        Budget budget = new Budget("Ngân sách test", 800000,
                                 java.time.LocalDateTime.now(),
                                 java.time.LocalDateTime.now().plusMonths(1),
                                 category);
        
        // Setup observers tự động cho tất cả wallets và budgets
        manager.setupWalletObservers(wallet1);
        manager.setupWalletObservers(wallet2);
        manager.setupBudgetObservers(budget);
        
        // Broadcast thông báo toàn cục
        manager.broadcast(new NotificationData(
            NotificationType.INFO,
            "Hệ thống đã khởi tạo thành công",
            "System"
        ));
        
        // Thực hiện các thao tác - tất cả observers sẽ nhận được thông báo
        System.out.println("Thực hiện thay đổi dữ liệu...");
        wallet1.updateBalance(1200000);
        wallet2.updateBalance(200000);  // Low balance
        budget.updateSpent(900000);     // Over budget
        
        // Broadcast thông báo kết thúc
        manager.broadcast(new NotificationData(
            NotificationType.INFO,
            "Demo hoàn thành",
            "System"
        ));
        
        // Kết quả: Tất cả thông báo sẽ được gửi đến cả UI observer và console observer
    }
    
    /**
     * VÍ DỤ 6: Xử lý lỗi trong Observer Pattern
     */
    public static void errorHandlingExample() {
        System.out.println("\n=== VÍ DỤ 6: ERROR HANDLING ===");
        
        // Tạo observer có thể bị lỗi
        IObserver problematicObserver = new IObserver() {
            @Override
            public void update(NotificationData notificationData) {
                if (notificationData.getMessage().contains("error")) {
                    throw new RuntimeException("Simulated observer error");
                }
                System.out.println("Observer OK: " + notificationData.getMessage());
            }
        };
        
        // Tạo observer bình thường
        NotificationObserver normalObserver = new NotificationObserver("NormalObserver");
        
        // Tạo wallet và thêm cả 2 observers
        Wallet wallet = new Wallet("Test wallet", 100000, null);
        wallet.addObserver(problematicObserver);
        wallet.addObserver(normalObserver);
        
        // Test với thông báo bình thường - cả 2 observers sẽ hoạt động
        System.out.println("Test normal notification...");
        wallet.notifyObservers(new NotificationData(
            NotificationType.INFO,
            "Normal message",
            "Test"
        ));
        
        // Test với thông báo gây lỗi - chỉ normalObserver hoạt động
        System.out.println("Test error notification...");
        wallet.notifyObservers(new NotificationData(
            NotificationType.ERROR,
            "This will cause error in problematic observer",
            "Test"
        ));
        
        // Kết quả: Hệ thống vẫn hoạt động bình thường dù có observer bị lỗi
        // NotificationManager và các Subject đã handle exceptions properly
    }
    
    /**
     * Main method để chạy tất cả ví dụ
     */
    public static void main(String[] args) {
        System.out.println("🎯 OBSERVER PATTERN EXAMPLES - MONEYKEEPER");
        System.out.println("============================================");
        
        // Chạy các ví dụ cơ bản (không cần JavaFX)
        basicWalletObserverExample();
        budgetObserverExample();
        customWalletObserverExample();
        errorHandlingExample();
        
        // NotificationManager example (cần label)
        // notificationManagerExample(someLabel);
        
        System.out.println("\n✅ Tất cả ví dụ đã hoàn thành!");
        System.out.println("Để xem UI examples, hãy chạy ứng dụng JavaFX và mở Observer Demo.");
    }
}
