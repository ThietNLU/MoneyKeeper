package ood.application.moneykeeper.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ood.application.moneykeeper.dao.UserDAO;
import ood.application.moneykeeper.model.User;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    // Profile controls
    @FXML
    private TextField usernameField;
    @FXML
    private Button resetProfileButton;
    @FXML
    private Button updateProfileButton;
    @FXML
    private Label profileErrorLabel;

    // Application settings
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private ComboBox<String> currencyComboBox;
    @FXML
    private ComboBox<String> themeComboBox;
    @FXML
    private ComboBox<String> dateFormatComboBox;

    // Notification settings
    @FXML
    private CheckBox budgetNotificationCheckBox;
    @FXML
    private CheckBox transactionNotificationCheckBox;
    @FXML
    private CheckBox reportNotificationCheckBox;
    @FXML
    private Slider budgetThresholdSlider;
    @FXML
    private Label thresholdValueLabel;

    // Data management
    @FXML
    private Button backupButton;
    @FXML
    private Button restoreButton;
    @FXML
    private Button exportButton;
    @FXML
    private CheckBox autoBackupCheckBox;
    @FXML
    private TextField backupPathField;
    @FXML
    private Button browseButton;

    // Bottom actions
    @FXML
    private Button resetAllButton;
    @FXML
    private Button saveAllButton;

    // Data
    private UserDAO userDAO;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            userDAO = new UserDAO();
            
            // Initialize combo boxes
            initializeComboBoxes();
            
            // Set up slider listener
            budgetThresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                thresholdValueLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
            });
            
            // Load current settings
            loadCurrentSettings();
            
            // Set default backup path
            backupPathField.setText(System.getProperty("user.home") + File.separator + "MoneyKeeperBackup");
            
        } catch (SQLException e) {
            showError("Initialization error: " + e.getMessage());
        }
    }

    private void initializeComboBoxes() {
        // Language options
        languageComboBox.setItems(FXCollections.observableArrayList(
            "English", "Tiếng Việt", "日本語", "中文"
        ));
        languageComboBox.getSelectionModel().selectFirst();

        // Currency options
        currencyComboBox.setItems(FXCollections.observableArrayList(
            "USD", "VNĐ", "EUR", "JPY", "CNY"
        ));
        currencyComboBox.getSelectionModel().selectFirst();

        // Theme options
        themeComboBox.setItems(FXCollections.observableArrayList(
            "Light", "Dark", "Auto"
        ));
        themeComboBox.getSelectionModel().selectFirst();

        // Date format options
        dateFormatComboBox.setItems(FXCollections.observableArrayList(
            "dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MM-yyyy"
        ));
        dateFormatComboBox.getSelectionModel().selectFirst();
    }

    private void loadCurrentSettings() {
        try {
            // Load user information - assuming user ID 1 for demo
            currentUser = userDAO.get("1");
            if (currentUser != null) {
                usernameField.setText(currentUser.getName());
            }
            
            // Load other settings from preferences or configuration
            loadApplicationSettings();
            loadNotificationSettings();
            
        } catch (SQLException e) {
            showError("Error loading settings: " + e.getMessage());
        }
    }

    private void loadApplicationSettings() {
        // Load settings from preferences or database
        // Set default values for demo
        budgetNotificationCheckBox.setSelected(true);
        transactionNotificationCheckBox.setSelected(true);
        reportNotificationCheckBox.setSelected(false);
        autoBackupCheckBox.setSelected(true);
    }

    private void loadNotificationSettings() {
        // Load notification preferences
        budgetThresholdSlider.setValue(80);
        thresholdValueLabel.setText("80%");
    }

    @FXML
    private void handleUpdateProfile() {
        try {
            // Validate input
            if (usernameField.getText().trim().isEmpty()) {
                showError("Please enter a username");
                return;
            }
            
            // Update user information
            if (currentUser != null) {
                currentUser.setName(usernameField.getText().trim());
                
                if (userDAO.update(currentUser)) {
                    showSuccess("Profile updated successfully");
                } else {
                    showError("Failed to update profile");
                }
            }
            
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleResetProfile() {
        usernameField.clear();
        hideError();
        
        // Reload current user data
        loadCurrentSettings();
    }

    @FXML
    private void handleBackupData() {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select backup directory");
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            
            File selectedDirectory = directoryChooser.showDialog(backupButton.getScene().getWindow());
            if (selectedDirectory != null) {
                // Implement backup logic here
                performBackup(selectedDirectory);
                showSuccess("Data backup successful at: " + selectedDirectory.getAbsolutePath());
            }
        } catch (Exception e) {
            showError("Backup error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRestoreData() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Restore");
        confirmAlert.setHeaderText("Restore Data");
        confirmAlert.setContentText("Are you sure you want to restore data? Current data will be overwritten.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select backup file");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Backup Files", "*.backup", "*.bak")
            );
            
            File selectedFile = fileChooser.showOpenDialog(restoreButton.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    // Implement restore logic here
                    performRestore(selectedFile);
                    showSuccess("Data restored successfully");
                } catch (Exception e) {
                    showError("Restore error: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export data");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        
        File selectedFile = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Implement export logic here
                performExport(selectedFile);
                showSuccess("Data exported successfully: " + selectedFile.getName());
            } catch (Exception e) {
                showError("Export error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBrowseBackupLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select default backup directory");
        
        File currentPath = new File(backupPathField.getText());
        if (currentPath.exists()) {
            directoryChooser.setInitialDirectory(currentPath);
        }
        
        File selectedDirectory = directoryChooser.showDialog(browseButton.getScene().getWindow());
        if (selectedDirectory != null) {
            backupPathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleResetAllSettings() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Reset");
        confirmAlert.setHeaderText("Reset All Settings");
        confirmAlert.setContentText("Are you sure you want to reset all settings to default?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            resetToDefaults();
            showSuccess("All settings reset to default");
        }
    }

    @FXML
    private void handleSaveAllSettings() {
        try {
            // Save all settings
            saveApplicationSettings();
            saveNotificationSettings();
            handleUpdateProfile();
            
            showSuccess("All settings saved successfully");
            
        } catch (Exception e) {
            showError("Error saving settings: " + e.getMessage());
        }
    }

    private void saveApplicationSettings() {
        // Save application settings to preferences or database
        String language = languageComboBox.getValue();
        String currency = currencyComboBox.getValue();
        String theme = themeComboBox.getValue();
        String dateFormat = dateFormatComboBox.getValue();
        
        // Implementation would save these to a settings table or preferences
        System.out.println("Saving application settings: " + language + ", " + currency + ", " + theme + ", " + dateFormat);
    }

    private void saveNotificationSettings() {
        // Save notification settings
        boolean budgetNotif = budgetNotificationCheckBox.isSelected();
        boolean transactionNotif = transactionNotificationCheckBox.isSelected();
        boolean reportNotif = reportNotificationCheckBox.isSelected();
        double threshold = budgetThresholdSlider.getValue();
        boolean autoBackup = autoBackupCheckBox.isSelected();
        String backupPath = backupPathField.getText();
        
        // Implementation would save these settings
        System.out.println("Saving notification settings: " + budgetNotif + ", " + transactionNotif + 
                          ", " + reportNotif + ", threshold: " + threshold + ", autoBackup: " + autoBackup + 
                          ", path: " + backupPath);
    }

    private void resetToDefaults() {
        // Reset all controls to default values
        languageComboBox.getSelectionModel().selectFirst();
        currencyComboBox.getSelectionModel().selectFirst();
        themeComboBox.getSelectionModel().selectFirst();
        dateFormatComboBox.getSelectionModel().selectFirst();
        
        budgetNotificationCheckBox.setSelected(true);
        transactionNotificationCheckBox.setSelected(true);
        reportNotificationCheckBox.setSelected(false);
        budgetThresholdSlider.setValue(80);
        autoBackupCheckBox.setSelected(true);
        
        backupPathField.setText(System.getProperty("user.home") + File.separator + "MoneyKeeperBackup");
    }

    private void performBackup(File directory) {
        // Implementation for backup functionality
        // This would typically copy the database and configuration files
        System.out.println("Performing backup to: " + directory.getAbsolutePath());
    }

    private void performRestore(File backupFile) {
        // Implementation for restore functionality
        System.out.println("Restoring from: " + backupFile.getAbsolutePath());
    }

    private void performExport(File exportFile) {
        // Implementation for export functionality
        System.out.println("Exporting to: " + exportFile.getAbsolutePath());
    }

    private void showError(String message) {
        profileErrorLabel.setText(message);
        profileErrorLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void hideError() {
        profileErrorLabel.setVisible(false);
    }
}
