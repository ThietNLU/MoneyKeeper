package ood.application.moneykeeper.main;

import ood.application.moneykeeper.observer.Observer;

public class ObserverTest {
    public static void main(String[] args) {
        // Tạo subject mock
        SimpleSubject subject = new SimpleSubject();

        // Tạo observer test
        TestObserver observer = new TestObserver();

        // Đăng ký observer vào subject
        subject.addObserver(observer);

        // Gửi notification
        subject.notifyObservers("Hello Observer!", null);

        // Kết quả
        System.out.println("Notification count: " + observer.getNotificationCount());
        System.out.println("Last message: " + observer.getLastMessage());

        // Huỷ đăng ký
        subject.removeObserver(observer);

        // Gửi notification nữa (không nhận được)
        subject.notifyObservers("Should not be received", null);

        // Kết quả
        System.out.println("After remove, notification count: " + observer.getNotificationCount());
    }

    // --- Subject mock đơn giản ---
    static class SimpleSubject {
        private Observer observer;

        public void addObserver(Observer o) {
            observer = o;
        }

        public void removeObserver(Observer o) {
            if (observer == o) observer = null;
        }

        public void notifyObservers(String message, Object data) {
            if (observer != null) observer.update(message, data);
        }
    }

    // --- Observer test đơn giản ---
    static class TestObserver implements Observer {
        private int notificationCount = 0;
        private String lastMessage = "";

        @Override
        public void update(String message, Object data) {
            notificationCount++;
            lastMessage = message;
            System.out.println("Observer received: " + message);
        }

        public int getNotificationCount() {
            return notificationCount;
        }

        public String getLastMessage() {
            return lastMessage;
        }
    }
}
