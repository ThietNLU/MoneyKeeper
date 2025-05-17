package ood.application.moneykeeper.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddTransactionController {

    @FXML
    private TextField transactionName;

    @FXML
    private TextField amount;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void saveTransaction() {
        // Lấy dữ liệu từ các trường
        String name = transactionName.getText();
        String amountText = amount.getText();

        // Xử lý lưu giao dịch (ví dụ lưu vào database hoặc danh sách)
        System.out.println("Giao dịch " + name + " với số tiền " + amountText);

        // Đóng cửa sổ
        dialogStage.close();
    }

    @FXML
    private void closeWindow() {
        dialogStage.close();
    }
}