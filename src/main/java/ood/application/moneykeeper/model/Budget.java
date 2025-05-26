package ood.application.moneykeeper.model;

import lombok.Data;
import ood.application.moneykeeper.utils.DateTimeUtils;
import ood.application.moneykeeper.utils.UUIDUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Budget {
    private String id;
    private String name;
    private double limit;
    private double spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Category category;
    // Đã xóa danh sách observers
    private List<Transaction> transactions = new ArrayList<>();

    public Budget(String name, double limit, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        this.id = UUIDUtils.generateShortUUID();
        this.name = name;
        this.limit = limit;
        this.spent = 0.0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
    }

    public Budget(String id, String name, double limit, double spent, LocalDateTime startDate, LocalDateTime endDate, Category category) {
        this.id = id;
        this.name = name;
        this.limit = limit;
        this.spent = spent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.transactions = new ArrayList<>();
    }

    // Các phương thức của Observer pattern đã được xóa

    public boolean isOverLimit() {
        return this.spent > this.limit;
    }

    public boolean isNearLimit() {
        return this.spent >= (this.limit * 0.8) && this.spent <= this.limit;
    }

    public String getInfo() {
        return this.name + "\n" + this.limit + "\n" + this.spent + "\n" + this.startDate
                + "\n" + this.endDate + "\n" + this.category.getName();
    }

    public void addTransaction(Transaction trans) {
        this.transactions.add(trans);
        processTrans(trans);

        // Kiểm tra giới hạn ngân sách sau khi thêm giao dịch
        checkBudgetLimits();
    }

    public void processTrans(Transaction trans) {
        spent += trans.getAmount();
    }

    /**
     * Kiểm tra giới hạn ngân sách
     * Đã xóa code thông báo cho observers
     */
    private void checkBudgetLimits() {
        // Chỉ giữ lại logic kiểm tra
    }

    /**
     * Cập nhật số tiền đã chi của ngân sách
     * Đã xóa code thông báo cho observers
     */
    public void updateSpent(double newSpent) {
        this.spent = newSpent;
        checkBudgetLimits();
    }

    public String getPeriod() {
        if (startDate != null && endDate != null) {
            return DateTimeUtils.formatDefault(startDate) + " - " + DateTimeUtils.formatDefault(endDate);
        }
        return "";
    }

    public String toString() {
        return "Id: " + id + "\nName: " + name + "\nLimit: " + limit + "\nSpent: "
                + spent + "\nStart: " + DateTimeUtils.formatDefault(startDate) + "\nEnd: "
                + DateTimeUtils.formatDefault(endDate) + "\nCategory: " + category.getName();
    }
}
