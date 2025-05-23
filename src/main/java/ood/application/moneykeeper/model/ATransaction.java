package ood.application.moneykeeper.model;

import java.time.LocalDateTime;
import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import ood.application.moneykeeper.utils.UUIDUtils;

@Data
public abstract class ATransaction {
    protected Wallet wallet;
    protected String tId;
    protected double amount;
    protected LocalDateTime dateTime;
    protected Category category;
    protected String description;

    public ATransaction(Wallet wallet, double amount, Category category,
                        String description) {
        this.tId = UUIDUtils.generateShortUUID();
        this.wallet = wallet;
        this.amount = amount;
        this.dateTime = LocalDateTime.now();
        this.category = category;
        this.description = description;
    }

    public ATransaction(String id, Wallet wallet, double amount, LocalDateTime dateTime, Category category, String description) {
        this.tId = id;
        this.wallet = wallet;
        this.amount = amount;
        this.dateTime = dateTime;
        this.category = category;
        this.description = description;
    }


    public abstract boolean isExpense();

    public abstract String toString();

    public boolean isId(String id){
        return this.tId.equals(id);
    }
}
