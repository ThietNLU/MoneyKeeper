package ood.application.moneykeeper.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject interface - định nghĩa các method để quản lý observers
 */
public interface Subject {
    /**
     * Thêm observer vào danh sách
     * @param observer Observer cần thêm
     */
    void addObserver(Observer observer);
    
    /**
     * Xóa observer khỏi danh sách
     * @param observer Observer cần xóa
     */
    void removeObserver(Observer observer);
    
    /**
     * Thông báo đến tất cả observers
     * @param message Thông điệp thông báo
     * @param data Dữ liệu đi kèm
     */
    void notifyObservers(String message, Object data);
}
