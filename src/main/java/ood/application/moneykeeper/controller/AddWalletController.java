package ood.application.moneykeeper.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AddWalletController {

    @FXML
    private Button saveButton;

    @FXML
    private TextField nameField;

    @FXML
    private TextField balanceField;

    @FXML
    private TextField ownerField;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        // Khởi tạo
        errorLabel.setVisible(false);

        // Gắn sự kiện cho nút "Lưu"
        saveButton.setOnAction(event -> handleSave());
    }

    private void handleSave() {
        // Reset lỗi
        errorLabel.setText("");
        errorLabel.setVisible(false);

        String name = nameField.getText().trim();
        String balanceText = balanceField.getText().trim();
        String owner = ownerField.getText().trim();

        // Kiểm tra dữ liệu nhập
        if (name.isEmpty() || balanceText.isEmpty() || owner.isEmpty()) {
            errorLabel.setText("Vui lòng điền đầy đủ thông tin.");
            errorLabel.setVisible(true);
            return;
        }


        // try catch kiểm tra số dư có nhỏ hơn 0 hay không
        double balance;
        try {
            balance = Double.parseDouble(balanceText);
            if (balance < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("SỐ DƯ LÀ SỐ ÂM. HÃY NHẬP LẠI !!!");
            errorLabel.setVisible(true);
            return;
        }

        // Lưu dữ liệu ví
        System.out.println("Lưu ví thành công:");
        System.out.println("- Tên: " + name);
        System.out.println("- Số dư: " + balance);
        System.out.println("- Chủ sở hữu: " + owner);

        // clear form || chuyển màn hình
        clearForm();
    }

    private void clearForm() {
        nameField.clear();
        balanceField.clear();
        ownerField.clear();
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}
