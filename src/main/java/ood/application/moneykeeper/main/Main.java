package ood.application.moneykeeper.main;

import java.time.LocalDateTime;

import ood.application.moneykeeper.model.ATransaction;
import ood.application.moneykeeper.model.Budget;
import ood.application.moneykeeper.model.Category;
import ood.application.moneykeeper.model.ExpenseTransactionFactory;
import ood.application.moneykeeper.model.IncomeTransactionFactory;
import ood.application.moneykeeper.model.NotificationObserver;
import ood.application.moneykeeper.model.User;
import ood.application.moneykeeper.model.Wallet;

public class Main {
    public static void main(String[] args) {
        // Tạo người dùng
//        User user = new User("Thiet");
//
//        // Tạo ví với số dư nhỏ để dễ kiểm tra số dư âm
//        Wallet wallet = new Wallet("Tiền mặt", 10000.0, user); // 10.000đ
//
//        // Tạo ngân sách với giới hạn nhỏ để dễ vượt quá
//        Category category = new Category("Ăn uống", true);
//        Budget budget = new Budget("Ăn uống", 5000.0, LocalDateTime.now(), LocalDateTime.now().plusDays(30), category); // Giới
//        // hạn
//        // 5.000đ
//        user.addBudget(budget);
//
//        // Gắn observers
//        NotificationObserver walletObserver = new NotificationObserver("WalletObserver");
//        NotificationObserver budgetObserver = new NotificationObserver("BudgetObserver");
//        wallet.addObserver(walletObserver);
//        budget.addObserver(budgetObserver);
//
//        // Tạo factories
//        ExpenseTransactionFactory expenseFactory = new ExpenseTransactionFactory();
//        IncomeTransactionFactory incomeFactory = new IncomeTransactionFactory();
//
//        // Kiểm tra 1: Giao dịch chi tiêu nhỏ, không vượt ngân sách, không âm ví
//        System.out.println("=== Kiểm tra 1: Giao dịch nhỏ ===");
//        ATransaction smallExpense = expenseFactory.createTransaction(wallet, 2000.0, category, "Mua nước");
//        smallExpense.processTrans();
//        System.out.println("Số dư ví: " + wallet.getBalance()); // 8.000đ
//        System.out.println("Chi tiêu ngân sách: " + budget.getSpent()); // 2.000đ
//        System.out.println("Số giao dịch: " + wallet.countTransactions()); // 1
//
//        // Kiểm tra 2: Giao dịch chi tiêu lớn, vượt ngân sách và làm ví âm
//        System.out.println("\n=== Kiểm tra 2: Giao dịch lớn ===");
//        ATransaction largeExpense = expenseFactory.createTransaction(wallet, 15000.0, category, "Mua cơm");
//        largeExpense.processTrans();
//        System.out.println("Số dư ví: " + wallet.getBalance()); // -7.000đ
//        System.out.println("Chi tiêu ngân sách: " + budget.getSpent()); // 17.000đ
//        System.out.println("Số giao dịch: " + wallet.countTransactions()); // 2
//
//        // Kiểm tra 3: Giao dịch thu nhập, không ảnh hưởng ngân sách
//        System.out.println("\n=== Kiểm tra 3: Giao dịch thu nhập ===");
//        ATransaction income = incomeFactory.createTransaction(wallet, 5000.0, new Category("Lương", false), "Nhận lương");
//        income.processTrans();
//        System.out.println("Số dư ví: " + wallet.getBalance()); // -2.000đ
//        System.out.println("Chi tiêu ngân sách: " + budget.getSpent()); // Vẫn 17.000đ
//        System.out.println("Số giao dịch: " + wallet.countTransactions()); // 3
//
//        // In lịch sử giao dịch
//        System.out.println("\n=== Lịch sử giao dịch ===");
//        System.out.println(wallet.printTransactions());
    }
}