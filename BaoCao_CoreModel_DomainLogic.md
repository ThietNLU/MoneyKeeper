# 📋 BÁO CÁO PHẦN 1: CORE MODEL & DOMAIN LOGIC

**Người báo cáo:** [Tên của bạn]  
**Thời gian báo cáo:** 10-15 phút  
**Phần phụ trách:** Core Domain Classes và Business Logic

---

## 🎯 1. GIỚI THIỆU PHẦN (2-3 phút)

### **Vai trò trong hệ thống MoneyKeeper:**
Phần Core Model & Domain Logic chính là **trái tim** của ứng dụng MoneyKeeper, bao gồm:
- **Domain Entities**: Đại diện cho các thực thể nghiệp vụ chính
- **Business Logic**: Quy tắc và logic nghiệp vụ của ứng dụng
- **Object Relationships**: Mối quan hệ giữa các đối tượng trong hệ thống

### **Files phụ trách:**
- `User.java` - Quản lý thông tin người dùng
- `Wallet.java` - Quản lý ví tiền và số dư
- `Transaction.java` - Giao dịch thu chi
- `Budget.java` - Ngân sách và giới hạn chi tiêu
- `Category.java` - Phân loại giao dịch

### **Mục tiêu:**
- Cung cấp foundation objects cho toàn bộ hệ thống
- Đảm bảo tính toàn vẹn dữ liệu (data integrity)
- Implement business rules một cách nhất quán

---

## 💻 2. DEMO CODE CHÍNH (5-7 phút)

### **2.1 Class User - Người dùng hệ thống**

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String name;
    private WalletManager wallets;    // Quản lý nhiều ví
    private BudgetManager budgets;    // Quản lý nhiều ngân sách
    
    // Constructor chính
    public User(String name) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.wallets = new WalletManager();
        this.budgets = new BudgetManager();
    }
}
```

**Key Features:**
- **Unique ID**: Tự động generate UUID cho mỗi user
- **Aggregate Root**: User là root entity, quản lý wallets và budgets
- **Business Methods**: `createWallet()`, `createBudget()`, `createTransaction()`

### **2.2 Class Wallet - Ví tiền**

```java
public class Wallet extends AbstractSubject {
    private String id;
    private String name;
    private double balance;                    // Số dư hiện tại
    private List<Transaction> transactions;    // Lịch sử giao dịch
    private User owner;                        // Chủ sở hữu
    private LocalDateTime creationDate;        // Ngày tạo
    
    // Business methods cho thu chi
    public void income(double amount) {
        double oldBalance = this.balance;
        this.balance += amount;
        checkBalanceConditions(oldBalance);    // Kiểm tra điều kiện số dư
    }
    
    public void expense(double amount) {
        double oldBalance = this.balance;
        this.balance -= amount;
        checkBalanceConditions(oldBalance);
    }
}
```

**Key Features:**
- **Observer Pattern**: Extends `AbstractSubject` để notify về thay đổi số dư
- **Balance Management**: Logic quản lý số dư với kiểm tra điều kiện
- **Business Rules**: 
  - Low balance warning (< 100,000 VND hoặc < 10% số dư)
  - Critical balance alert (< 50,000 VND)

### **2.3 Class Transaction - Giao dịch**

```java
public class Transaction {
    private String tId;
    private Wallet wallet;                     // Ví liên quan
    private ITransactionStrategy strategy;     // Strategy pattern cho xử lý
    private double amount;                     // Số tiền
    private LocalDateTime dateTime;            // Thời gian giao dịch
    private Category category;                 // Danh mục
    private String description;                // Mô tả
    
    // Core business method
    public void processWallet() {
        strategy.processWallet(wallet, amount);
    }
    
    public void processBudget(BudgetManager budgetManager) {
        strategy.processBudget(budgetManager, this);
    }
    
    public boolean isExpense() {
        return strategy.isExpense();
    }
}
```

**Key Features:**
- **Strategy Pattern**: Sử dụng `ITransactionStrategy` để xử lý khác nhau cho thu/chi
- **Domain Logic**: `processWallet()` và `processBudget()` để update related entities
- **Type Safety**: `isExpense()` để xác định loại giao dịch

### **2.4 Class Budget - Ngân sách**

```java
@Data
@EqualsAndHashCode(callSuper=false)
public class Budget extends AbstractSubject {
    private String id;
    private String name;
    private double limit;                      // Giới hạn ngân sách
    private double spent;                      // Đã chi tiêu
    private LocalDateTime startDate;           // Ngày bắt đầu
    private LocalDateTime endDate;             // Ngày kết thúc
    private Category category;                 // Danh mục áp dụng
    private List<Transaction> transactions;    // Giao dịch thuộc ngân sách
    
    // Business rules
    public boolean isOverLimit() {
        return this.spent > this.limit;
    }
    
    public boolean isNearLimit() {
        return this.spent >= (this.limit * 0.8) && this.spent <= this.limit;
    }
    
    // Transaction management
    public void addTransaction(Transaction trans) {
        this.transactions.add(trans);
        processTrans(trans);
        checkBudgetLimits();    // Kiểm tra giới hạn sau khi thêm
    }
}
```

**Key Features:**
- **Observer Pattern**: Notify khi vượt giới hạn ngân sách
- **Business Rules**: Cảnh báo khi gần hết (80%) và vượt giới hạn (100%)
- **Period Management**: Quản lý ngân sách theo thời gian

### **2.5 Class Category - Phân loại**

```java
@Data
@AllArgsConstructor
public class Category {
    private String id;
    private String name;
    private boolean isExpense;    // true = chi tiêu, false = thu nhập
    
    public Category(String name, boolean isExpense) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.isExpense = isExpense;
    }
}
```

**Key Features:**
- **Simple Domain Object**: Đơn giản nhưng quan trọng cho phân loại
- **Type Distinction**: Phân biệt rõ ràng thu nhập và chi tiêu
- **Immutable Business Logic**: Loại category không thay đổi sau khi tạo

---

## 🏗️ 3. DESIGN DECISIONS (2-3 phút)

### **3.1 Tại sao chọn approach này?**

#### **Domain-Driven Design (DDD)**
- **User** là Aggregate Root, đảm bảo consistency
- Mỗi entity có identity duy nhất (UUID)
- Business logic được encapsulated trong domain objects

#### **Observer Pattern Integration**
- `Wallet` và `Budget` extends `AbstractSubject`
- Tự động notify khi có thay đổi quan trọng:
  - Số dư thấp trong ví
  - Vượt giới hạn ngân sách

#### **Strategy Pattern cho Transaction**
- `ExpenseTransactionStrategy` vs `IncomeTransactionStrategy`
- Dễ dàng extend cho các loại giao dịch mới
- Separation of concerns

### **3.2 Benefits và Tradeoffs**

**Benefits:**
- ✅ **High Cohesion**: Logic nghiệp vụ tập trung trong domain objects
- ✅ **Loose Coupling**: Các objects tương tác qua interfaces
- ✅ **Extensibility**: Dễ dàng thêm features mới
- ✅ **Testability**: Business logic có thể test độc lập

**Tradeoffs:**
- ⚠️ **Complexity**: Nhiều patterns có thể phức tạp cho beginner
- ⚠️ **Memory**: Observer pattern có thể tạo memory leaks nếu không manage đúng

### **3.3 Design Patterns Used**

1. **Aggregate Pattern**: User quản lý Wallets và Budgets
2. **Observer Pattern**: Notification system cho business events
3. **Strategy Pattern**: Transaction processing logic
4. **Factory Pattern**: UUID generation trong constructors

---

## 🔗 4. INTEGRATION (1-2 phút)

### **4.1 Cách kết nối với other components:**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controllers   │───▶│   Core Models    │◀───│   DAO Layer     │
│   (View Layer)  │    │  (Domain Logic)  │    │ (Data Access)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                               │
                               ▼
                       ┌──────────────────┐
                       │  Observer System │
                       │   (Notifications)│
                       └──────────────────┘
```

### **4.2 Data Flow Example:**

**User Story**: "User tạo transaction mới"

1. **Controller** nhận input từ UI
2. **Controller** tạo `Transaction` object (Core Model)
3. **Transaction** gọi `processWallet()` → Update số dư ví
4. **Wallet** check business rules → Trigger observers nếu cần
5. **Transaction** gọi `processBudget()` → Update ngân sách
6. **Budget** check limits → Trigger observers nếu cần
7. **DAO Layer** persist changes to database

### **4.3 Dependencies:**

**Incoming Dependencies:**
- Controllers sử dụng core models để xử lý business logic
- DAO layer map database records thành domain objects
- Observer system listen to domain events

**Outgoing Dependencies:**
- Utils package (UUIDUtils, DateTimeUtils)
- Observer package (AbstractSubject)
- Strategy interfaces (ITransactionStrategy)

---

## ❓ 5. CÂU HỎI CÓ THỂ ĐƯỢC HỎI & TRẢ LỜI

### **Q1: Tại sao User lại quản lý cả Wallets và Budgets?**
**A:** User là Aggregate Root trong DDD. Điều này đảm bảo:
- Consistency: Tất cả thay đổi đi qua User
- Business rules: User có thể enforce rules across wallets/budgets
- Transaction boundaries: Atomic operations

### **Q2: Observer pattern có bị memory leak không?**
**A:** Chúng tôi manage bằng cách:
- Unregister observers khi objects bị destroy
- Sử dụng WeakReference nếu cần thiết
- Clear observers list trong cleanup methods

### **Q3: Tại sao không dùng JPA entities?**
**A:** 
- Domain objects should be independent của persistence layer
- Business logic không bị ảnh hưởng bởi database concerns
- Dễ dàng test mà không cần database

### **Q4: Strategy pattern có overkill không cho chỉ 2 types?**
**A:** Không, vì:
- Chuẩn bị cho tương lai (transfer, investment transactions)
- Separation of concerns rõ ràng
- Testable và maintainable

### **Q5: UUID có performance issue không?**
**A:** 
- UUID đảm bảo uniqueness across distributed systems
- Performance trade-off chấp nhận được cho business apps
- Có thể optimize sau nếu cần (database indexes)

---

## 📊 6. DEMO SCENARIO

**Scenario**: "User tạo ví mới và thêm giao dịch chi tiêu"

```java
// 1. Tạo user
User user = new User("John Doe");

// 2. Tạo ví với số dư ban đầu
Wallet wallet = user.createWallet("Main Wallet", 1000000.0);

// 3. Tạo category
Category foodCategory = new Category("Food", true);

// 4. Tạo transaction chi tiêu
Transaction trans = user.createTransaction(wallet, 150000.0, foodCategory, "Lunch");

// 5. Business logic tự động:
// - Wallet balance: 1,000,000 → 850,000
// - Observer notification nếu low balance
// - Budget update nếu có budget cho Food category
```

---

## 🎯 7. KẾT LUẬN

### **Core Model & Domain Logic cung cấp:**
- **Solid foundation** cho toàn bộ application
- **Business rules** được enforce consistently
- **Clean architecture** dễ maintain và extend
- **Event-driven** system thông qua Observer pattern

### **Ready for integration với:**
- **Design Patterns & Managers** (Strategy, Factory)
- **Observer System** (Notifications)  
- **Data Access Layer** (Persistence)
- **UI Controllers** (User interactions)

---

**🕐 Thời gian ước tính: 12-15 phút**

**📋 Checklist trước khi báo cáo:**
- [ ] Hiểu rõ từng class và method chính
- [ ] Chuẩn bị demo scenario flow
- [ ] Biết cách connect với other components  
- [ ] Chuẩn bị trả lời questions về design choices
- [ ] Practice demo code walkthrough
