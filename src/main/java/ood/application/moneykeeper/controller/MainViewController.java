package ood.application.moneykeeper.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainViewController {
    @FXML
    private BorderPane mainPane;

    @FXML
    private void loadHome() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/home_test.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void loadWallet() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/wallet_test.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void loadBudget() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/budget_test.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void loadTransaction() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/transaction_test.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void loadReport() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/report.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void loadSettings() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/settings_test.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void loadCategory() throws IOException {
        Node view = FXMLLoader.load(getClass().getResource("/ood/application/moneykeeper/category_test.fxml"));
        mainPane.setCenter(view);
    }

    @FXML
    private void openTransactionWindow() {
        try {
            // Sử dụng DialogPane thay vì Stage riêng biệt
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ood/application/moneykeeper/add_transaction.fxml"));
            DialogPane dialogPane = loader.load();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Thêm giao dịch mới");
            dialog.showAndWait();

            // Nếu đang ở Home hoặc Transaction view, làm mới dữ liệu
            if (mainPane.getCenter() != null && mainPane.getCenter().getId() != null) {
                String currentView = mainPane.getCenter().getId();
                if (currentView.contains("home") || currentView.contains("transaction")) {
                    try {
                        // Làm mới view hiện tại
                        if (currentView.contains("home")) {
                            loadHome();
                        } else if (currentView.contains("transaction")) {
                            loadTransaction();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở cửa sổ thêm giao dịch: " + e.getMessage());
            alert.showAndWait();
        }
    }
}

