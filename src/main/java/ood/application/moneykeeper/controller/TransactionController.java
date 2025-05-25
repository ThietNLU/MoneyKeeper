package ood.application.moneykeeper.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import ood.application.moneykeeper.model.Wallet;

import java.net.URL;
import java.util.ResourceBundle;

public class TransactionController implements Initializable {
    private Wallet wallet;

    @FXML
    private TextField walletNameField;

    public void initData(Wallet wallet) {
        this.wallet = wallet;
        if (walletNameField != null && wallet != null) {
            walletNameField.setText(wallet.getName());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Any additional initialization if needed
    }
}

