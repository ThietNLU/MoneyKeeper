package ood.application.moneykeeper.main;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class MainView extends Application {    @Override
    public void start(javafx.stage.Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ood/application/moneykeeper/mainview_test.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("MoneyKeeper - Expense Tracker");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
