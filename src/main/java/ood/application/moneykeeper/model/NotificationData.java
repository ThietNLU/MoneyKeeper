package ood.application.moneykeeper.model;

import java.time.LocalDateTime;

/**
 * Data class to hold notification information
 */
public class NotificationData {
    private NotificationType type;
    private String message;
    private String source;
    private LocalDateTime timestamp;
    private Object data;

    public NotificationData(NotificationType type, String message, String source) {
        this.type = type;
        this.message = message;
        this.source = source;
        this.timestamp = LocalDateTime.now();
    }

    public NotificationData(NotificationType type, String message, String source, Object data) {
        this.type = type;
        this.message = message;
        this.source = source;
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }

    // Getters and setters
    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s)", 
                type.getType(), 
                timestamp.toString(), 
                message, 
                source);
    }
}
