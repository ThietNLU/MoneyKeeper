package ood.application.moneykeeper.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ood.application.moneykeeper.observer.AbstractSubject;
import ood.application.moneykeeper.utils.DateTimeUtils;
import ood.application.moneykeeper.utils.UUIDUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
public class Budget extends AbstractSubject {
    private String id;
    private String name;
    private double limit;
    private double spent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;    private Category category;
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
        this.category = category;        this.transactions = new ArrayList<>();
    }

    public boolean isOverLimit() {
        return this.spent > this.limit;
    }

    public boolean isNearLimit() {
        return this.spent >= (this.limit * 0.8) && this.spent <= this.limit;
    }    public String getInfo() {
        return this.name + "\n" + this.limit + "\n" + this.spent + "\n" + this.startDate
                + "\n" + this.endDate + "\n" + (this.category != null ? this.category.getName() : "N/A");
    }

    public void addTransaction(Transaction trans) {
        this.transactions.add(trans);
        processTrans(trans);

        // Kiểm tra giới hạn ngân sách sau khi thêm giao dịch
        checkBudgetLimits();
    }

    public void removeTransaction(Transaction trans) {
        this.transactions.remove(trans);
        processTransRemoval(trans);

        // Kiểm tra giới hạn ngân sách sau khi xóa giao dịch
        checkBudgetLimits();
    }

    public void processTrans(Transaction trans) {
        spent += trans.getAmount();
    }

    public void processTransRemoval(Transaction trans) {
        spent -= trans.getAmount();
        // Đảm bảo spent không bị âm
        if (spent < 0) {
            spent = 0;
        }
    } 
    
    private void checkBudgetLimits() {
        if (isOverLimit()) {
            notifyObservers("BUDGET_EXCEEDED", this);
        } else if (isNearLimit()) {
            notifyObservers("BUDGET_WARNING", this);
        }
    }

    public void updateSpent(double newSpent) {
        this.spent = newSpent;
        checkBudgetLimits();
    }

    public String getPeriod() {
        if (startDate != null && endDate != null) {
            return DateTimeUtils.formatDefault(startDate) + " - " + DateTimeUtils.formatDefault(endDate);
        }
        return "";
    }    public String toString() {
        return "Id: " + id + "\nName: " + name + "\nLimit: " + limit + "\nSpent: "
                + spent + "\nStart: " + DateTimeUtils.formatDefault(startDate) + "\nEnd: "
                + DateTimeUtils.formatDefault(endDate) + "\nCategory: " + (category != null ? category.getName() : "N/A");
    }
}
