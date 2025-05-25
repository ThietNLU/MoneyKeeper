module ood.application.moneykeeper {    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // JDBC
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    // Lombok (dùng static vì nó không có mã byte tại runtime)
    requires static lombok;

    // Mở package cho FXML sử dụng
    opens ood.application.moneykeeper to javafx.fxml;
    opens ood.application.moneykeeper.controller to javafx.fxml;
    opens ood.application.moneykeeper.model to javafx.fxml;
    opens ood.application.moneykeeper.report to javafx.fxml;

    // Export package cho các phần khác sử dụng
    exports ood.application.moneykeeper.controller;
    exports ood.application.moneykeeper.model;
    exports ood.application.moneykeeper.dao;
    exports ood.application.moneykeeper.main;
}
