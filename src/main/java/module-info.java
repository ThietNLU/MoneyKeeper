module ood.application.moneykeeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires java.sql;
    requires org.xerial.sqlitejdbc;

    requires static lombok;
    opens ood.application.moneykeeper.controller to javafx.fxml;
    opens ood.application.moneykeeper.model to javafx.fxml;
    opens ood.application.moneykeeper.report to javafx.fxml;
    opens ood.application.moneykeeper.main to javafx.fxml;
    exports ood.application.moneykeeper.controller;
    exports ood.application.moneykeeper.model;
    exports ood.application.moneykeeper.dao;
    exports ood.application.moneykeeper.main;
    exports ood.application.moneykeeper.utils;
}
