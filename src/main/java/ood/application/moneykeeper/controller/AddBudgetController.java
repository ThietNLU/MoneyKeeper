package ood.application.moneykeeper.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class AddBudgetController {

    @FXML
    private Button saveButton;

    @FXML
    private TextField budgetNameField;

    @FXML
    private TextField limitField;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);

        saveButton.setOnAction(event -> handleSave());
    }

    private void handleSave() {
        errorLabel.setVisible(false);
        errorLabel.setText("");

        // Lấy dữ liệu từ các trường
        String name = budgetNameField.getText().trim();
        String limitText = limitField.getText().trim();
        LocalDate startDates = startDate.getValue();
        LocalDate endDates = endDate.getValue();
        String category = categoryComboBox.getValue();

        // Kiểm tra dữ liệu có trống hay không
        if (name.isEmpty() || limitText.isEmpty() || startDates == null || endDates == null || category == null) {
            errorLabel.setText("Vui lòng điền đầy đủ thông tin.");
            errorLabel.setVisible(true);
            return;
        }

        //  biến limit
        double limit;

        // try catch kiểm tra limit
        try {
            limit = Double.parseDouble(limitText);
            // limit < 0 ném ngoại lệ
            if (limit < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            errorLabel.setText("Số tiền giới hạn không hợp lệ !!!");
            errorLabel.setVisible(true);
            return;
        }

        // kiểm tra ngày bắt đầu và kết thúc
        if (startDates.isAfter(endDates)) {
            errorLabel.setText("Ngày bắt đầu không được thiết lập sau ngày kết thúc");
            errorLabel.setVisible(true);
            return;
        }

        // Lưu dữ liệu ngân sách
        System.out.println("Ngân sách được lưu:");
        System.out.println("- Tên: " + name);
        System.out.println("- Giới hạn: " + limit);
        System.out.println("- Bắt đầu: " + startDates);
        System.out.println("- Kết thúc: " + endDates);
        System.out.println("- Hạng mục: " + category);

        clearForm();
    }

    // thiết lập trạng thái lại form
    private void clearForm() {
        budgetNameField.clear();
        limitField.clear();
        startDate.setValue(null);
        endDate.setValue(null);
        categoryComboBox.setValue(null);
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }
}
