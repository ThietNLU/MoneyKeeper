package ood.application.moneykeeper.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ViewController {
    @FXML private BorderPane mainPane;
    @FXML private Button addTrans;

    @FXML
    private void loadHome() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/HomeScreen.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void loadStats() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/report.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void loadSettings() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/settings.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void openTransactionWindow() {
        try {
            // Tải FXML cho cửa sổ thêm giao dịch
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ood/application/moneykeeper/add_trans.fxml"));
            BorderPane root = loader.load();

            // Tạo một Stage mới (cửa sổ popup)
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Giao Dịch");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPane.getScene().getWindow());

            // Thiết lập cửa sổ
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Hiển thị cửa sổ
            dialogStage.showAndWait();
        } catch (IOException e) {
            // Hiển thị thông báo lỗi nếu không thể mở cửa sổ
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể mở cửa sổ thêm giao dịch");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}