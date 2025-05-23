package ood.application.moneykeeper.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class Wallet {

    @FXML
    private FlowPane walletFlowPane; // Liên kết với fx:id="walletFlowPane" trong FXML


    @FXML
    public void initialize() {
        // Thêm một số ví để test
        addWalletCard("Ví chính", "Tiền mặt", "5,000,000");
        addWalletCard("Ví tiết kiệm", "Ngân hàng", "20,000,000");
        addWalletCard("Ví phụ", "Ví điện tử", "1,200,000");
    }

    public void addWalletCard(String name, String type, String balance) {
        VBox card = new VBox(8);
        card.setPrefSize(180, 120);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 10;" +
                "-fx-background-radius: 10; -fx-padding: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label balanceLabel = new Label("Số dư: " + balance + " đ");
        balanceLabel.setStyle("-fx-text-fill: #28a745;");

        Label typeLabel = new Label("Loại: " + type);
        typeLabel.setStyle("-fx-text-fill: #888888;");

        card.getChildren().addAll(nameLabel, balanceLabel, typeLabel);
        walletFlowPane.getChildren().add(card);
    }

}
