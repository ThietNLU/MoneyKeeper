### **👨‍💻 PHẦN 1: CORE MODEL & DOMAIN LOGIC**
**Người báo cáo 1**

**📁 Files phụ trách:**
- User.java
- Wallet.java
- Transaction.java
- Budget.java
- Category.java

**🎯 Nội dung báo cáo:**
- Core domain classes và business logic
- Object relationships & attributes
- Business rules và validation
- Lifecycle management của các objects

---

### **🏗️ PHẦN 2: DESIGN PATTERNS & MANAGERS**
**Người báo cáo 2**

**📁 Files phụ trách:**
- WalletManager.java
- BudgetManager.java
- ITransactionStrategy.java
- `src/main/java/ood/application/moneykeeper/model/*TransactionStrategy.java`
- `src/main/java/ood/application/moneykeeper/model/*Factory.java`

**🎯 Nội dung báo cáo:**
- Strategy Pattern implementation
- Manager Pattern cho business logic
- Factory Pattern cho object creation
- Pattern benefits và design rationale

---

### **🔔 PHẦN 3: OBSERVER PATTERN & NOTIFICATION SYSTEM**
**Người báo cáo 3**

**📁 Files phụ trách:**
- observer (toàn bộ package)
- Integration với Budget.java và Wallet.java (phần Observer)

**🎯 Nội dung báo cáo:**
- Observer Pattern implementation
- Notification system architecture
- Real-time updates và event handling
- ObserverManager và centralized management

---

### **🗄️ PHẦN 4: DATA ACCESS LAYER & DATABASE**
**Người báo cáo 4**

**📁 Files phụ trách:**
- dao (toàn bộ package)
- moneykeeper.db (database file)
- Database schema analysis

**🎯 Nội dung báo cáo:**
- DAO Pattern implementation
- Database design và relationships
- CRUD operations
- Connection management (Singleton pattern)

---

### **🎮 PHẦN 5: USER INTERFACE & CONTROLLERS**
**Người báo cáo 5**

**📁 Files phụ trách:**
- controller (toàn bộ package)
- `src/main/resources/ood/application/moneykeeper/*.fxml`
- main (application entry point)

**🎯 Nội dung báo cáo:**
- MVC Pattern trong JavaFX
- Controller implementation
- FXML views và UI components
- Event handling và user interactions

---

### **📊 PHẦN 6: REPORTING, UTILITIES & TESTING**
**Người báo cáo 6**

**📁 Files phụ trách:**
- report (toàn bộ package)
- utils (toàn bộ package)
- java (test cases)
- pom.xml, module-info.java
- Project configuration files

**🎯 Nội dung báo cáo:**
- Reporting system architecture
- Utility classes và helper methods
- Testing strategies
- Project configuration và dependencies

---

## **📝 HƯỚNG DẪN CHO TỪNG NGƯỜI BÁO CÁO**

### **⏱️ Thời gian báo cáo:** 10-15 phút/người

### **📊 Cấu trúc báo cáo đề xuất:**

1. **Giới thiệu phần (2-3 phút)**
   - Vai trò của phần này trong hệ thống
   - Mục tiêu và responsibility

2. **Demo code chính (5-7 phút)**
   - Walk through key classes/methods
   - Highlight important features
   - Show relationships với other components

3. **Design decisions (2-3 phút)**
   - Giải thích tại sao chọn approach này
   - Benefits và tradeoffs
   - Design patterns used

4. **Integration (1-2 phút)**
   - Cách phần này tương tác với other parts
   - Dependencies và data flow

### **💡 Tips cho báo cáo thành công:**

#### **🔗 Coordination giữa các người:**
- **Người 1 & 2**: Model classes và Managers (core business logic)
- **Người 2 & 3**: Managers và Observer pattern integration
- **Người 3 & 4**: Observer notifications và Database persistence
- **Người 4 & 5**: Data layer và Controller data binding
- **Người 5 & 6**: UI components và Reporting integration

#### **📋 Checklist cho mỗi người:**
- [ ] Hiểu rõ code trong phần phụ trách
- [ ] Biết cách phần mình connect với other parts
- [ ] Chuẩn bị 2-3 examples cụ thể
- [ ] Practice demo trước khi báo cáo
- [ ] Chuẩn bị trả lời questions về design choices

#### **🎯 Scenario demo xuyên suốt:**
Tất cả có thể dùng cùng một user story: **"User tạo wallet → Add transaction → Check budget → View report"** nhưng mỗi người focus vào phần của mình.

### **❓ Questions có thể được hỏi:**

**Phần 1**: Object modeling, business rules, domain logic
**Phần 2**: Design patterns, why use managers, strategy benefits
**Phần 3**: Observer pattern benefits, notification system design
**Phần 4**: Database design, data integrity, DAO pattern benefits
**Phần 5**: MVC implementation, UI/UX decisions, event handling
**Phần 6**: Reporting algorithms, testing approach, project structure
