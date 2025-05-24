package ood.application.moneykeeper.main;

import ood.application.moneykeeper.dao.*;
import ood.application.moneykeeper.model.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DBTest {

    public static void main(String[] args) {
        try {
            // Khởi tạo DAO
            TransactionDAO transactionDAO = new TransactionDAO();
            WalletDAO walletDAO = new WalletDAO();
            UserDAO userDAO = new UserDAO();
            CategoryDAO categoryDAO = new CategoryDAO();

            // Tạo dữ liệu người dùng
            User testUser = new User("test_user", "Test User");
            userDAO.save(testUser);

            // Tạo ví
            Wallet testWallet = new Wallet("test_wallet", "Test Wallet", 500.0, testUser);
            walletDAO.save(testWallet);

            // Tạo danh mục
            Category testCategory = new Category("test_category", "Food", true);
            categoryDAO.save(testCategory);

            // Tạo giao dịch
            String transId = UUID.randomUUID().toString();
            ExpenseTransaction transaction = new ExpenseTransaction(transId, testWallet, 150.0, testCategory, "Lunch");
            transaction.setDateTime(LocalDateTime.now());

            // Lưu giao dịch
            boolean saveResult = transactionDAO.save(transaction);
            System.out.println("Save result: " + saveResult);

            // Lấy lại giao dịch
            ATransaction fetched = transactionDAO.get(transId);
            System.out.println("Fetched transaction:");
            if (fetched != null) {
                System.out.println("  ID: " + fetched.getTId());
                System.out.println("  Wallet: " + fetched.getWallet().getName());
                System.out.println("  Amount: " + fetched.getAmount());
                System.out.println("  Category: " + fetched.getCategory().getName());
                System.out.println("  Description: " + fetched.getDescription());
                System.out.println("  Is Expense: " + fetched.isExpense());
            } else {
                System.out.println("  -> Transaction not found");
            }

            // Cập nhật mô tả giao dịch
            transaction.setDescription("Lunch Updated");
            transaction.setAmount(180.0);
            boolean updateResult = transactionDAO.update(transaction);
            System.out.println("Update result: " + updateResult);

            // Xóa giao dịch
            boolean deleteResult = transactionDAO.delete(transaction);
            System.out.println("Delete result: " + deleteResult);

            // In tất cả giao dịch còn lại
            List<ATransaction> allTransactions = transactionDAO.getAll();
            System.out.println("All transactions left: " + allTransactions.size());

            // Xóa dữ liệu phụ
            walletDAO.delete(testWallet);
            userDAO.delete(testUser);
            categoryDAO.delete(testCategory);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

