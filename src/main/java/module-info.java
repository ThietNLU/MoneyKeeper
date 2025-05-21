module ood.application.moneykeeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens ood.application.moneykeeper to javafx.fxml;
    exports ood.application.moneykeeper.view;
    opens ood.application.moneykeeper.controller to javafx.fxml;
}