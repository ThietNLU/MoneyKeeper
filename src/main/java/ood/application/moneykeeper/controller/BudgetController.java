package ood.application.moneykeeper.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class BudgetController {
    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    public void initialize() {
        sortComboBox.setValue("Lọc từ A-Z");
    }

}
