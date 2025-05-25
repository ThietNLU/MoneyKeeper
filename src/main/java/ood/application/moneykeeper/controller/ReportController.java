package ood.application.moneykeeper.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import ood.application.moneykeeper.report.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportController implements Initializable {

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<String> timeStrategyComboBox;
    @FXML private HBox customDateContainer;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button generateReportButton;
    @FXML private Label reportTitleLabel;
    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;
    @FXML private TableView<ReportItem> dataTable;
    @FXML private TableColumn<ReportItem, String> categoryColumn;
    @FXML private TableColumn<ReportItem, Double> amountColumn;
    @FXML private TableColumn<ReportItem, Double> percentageColumn;
    @FXML private TableColumn<ReportItem, String> typeColumn;
    @FXML private Label totalIncomeLabel;
    @FXML private Label totalExpenseLabel;
    @FXML private Label balanceLabel;

    private Report currentReport;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup event listeners
        timeStrategyComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.equals("Tùy chỉnh")) {
                customDateContainer.setVisible(true);
            } else {
                customDateContainer.setVisible(false);
            }
        });

        // Default selection
        reportTypeComboBox.getSelectionModel().select(0);
        timeStrategyComboBox.getSelectionModel().select(0);

        // Setup table columns
        categoryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategory()));
        amountColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        percentageColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getPercentage()).asObject());
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType()));

        // Setup generate report button
        generateReportButton.setOnAction(event -> generateReport());
    }

    private void generateReport() {
        try {
            // Create appropriate report type
            String reportType = reportTypeComboBox.getValue();
            if (reportType.equals("Báo cáo hạng mục")) {
                currentReport = new CategoryReport();
            } else {
                currentReport = new BudgetReport();
            }

            // Set time strategy
            String timeStrategy = timeStrategyComboBox.getValue();
            if (timeStrategy.equals("Tháng hiện tại")) {
                currentReport.setTimeStrategy(new MonthlyTimeStrategy());
            } else if (timeStrategy.equals("Tùy chỉnh") &&
                    startDatePicker.getValue() != null &&
                    endDatePicker.getValue() != null) {
                LocalDateTime start = startDatePicker.getValue().atStartOfDay();
                LocalDateTime end = endDatePicker.getValue().atTime(23, 59, 59);
                currentReport.setTimeStrategy(new CustomTimeStrategy(start, end));
            }

            // Generate report data and update UI
            ReportData reportData = currentReport.generateReport();
            updateUI(reportData);

        } catch (SQLException e) {
            showError("Lỗi khi tạo báo cáo: " + e.getMessage());
        }
    }

    private void updateUI(ReportData reportData) {
        // Update title
        reportTitleLabel.setText(reportData.getTitle());

        // Clear previous data
        barChart.getData().clear();
        pieChart.getData().clear();

        // Update charts based on report type
        if (reportData.getExpensesByCategory() != null) {
            updateCategoryReport(reportData);
        } else if (reportData.getBudgetStatus() != null) {
            updateBudgetReport(reportData);
        }
    }

    private void updateCategoryReport(ReportData reportData) {
        // Bar chart for expenses
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Chi tiêu");

        // Pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Table data
        ObservableList<ReportItem> tableData = FXCollections.observableArrayList();

        double totalExpense = 0;
        double totalIncome = 0;

        // Process expense data
        for (Map.Entry<String, Double> entry : reportData.getExpensesByCategory().entrySet()) {
            expenseSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            tableData.add(new ReportItem(entry.getKey(), entry.getValue(), 0, "Chi tiêu"));
            totalExpense += entry.getValue();
        }

        // Process income data if available
        if (reportData.getIncomeByCategory() != null) {
            XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
            incomeSeries.setName("Thu nhập");

            for (Map.Entry<String, Double> entry : reportData.getIncomeByCategory().entrySet()) {
                incomeSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                tableData.add(new ReportItem(entry.getKey(), entry.getValue(), 0, "Thu nhập"));
                totalIncome += entry.getValue();
            }

            barChart.getData().add(incomeSeries);
        }

        // Calculate percentages
        for (ReportItem item : tableData) {
            if (item.getType().equals("Chi tiêu") && totalExpense > 0) {
                item.setPercentage(item.getAmount() / totalExpense * 100);
            } else if (item.getType().equals("Thu nhập") && totalIncome > 0) {
                item.setPercentage(item.getAmount() / totalIncome * 100);
            }
        }

        // Update UI elements
        barChart.getData().add(expenseSeries);
        pieChart.setData(pieChartData);
        dataTable.setItems(tableData);

        totalExpenseLabel.setText(String.format("%,.0f VNĐ", totalExpense));
        totalIncomeLabel.setText(String.format("%,.0f VNĐ", totalIncome));
        balanceLabel.setText(String.format("%,.0f VNĐ", totalIncome - totalExpense));
    }

    private void updateBudgetReport(ReportData reportData) {
        // Bar chart for budget status vs limits
        XYChart.Series<String, Number> spentSeries = new XYChart.Series<>();
        spentSeries.setName("Đã chi");

        XYChart.Series<String, Number> limitSeries = new XYChart.Series<>();
        limitSeries.setName("Giới hạn");

        // Table data
        ObservableList<ReportItem> tableData = FXCollections.observableArrayList();

        double totalSpent = 0;
        double totalLimit = 0;

        // Process budget data
        for (Map.Entry<String, Double> entry : reportData.getBudgetStatus().entrySet()) {
            String budgetName = entry.getKey();
            double spent = entry.getValue();
            double limit = reportData.getBudgetLimits().get(budgetName);

            spentSeries.getData().add(new XYChart.Data<>(budgetName, spent));
            limitSeries.getData().add(new XYChart.Data<>(budgetName, limit));

            double percentage = limit > 0 ? (spent / limit * 100) : 0;
            tableData.add(new ReportItem(budgetName, spent, percentage,
                    percentage > 100 ? "Vượt giới hạn" : "Trong giới hạn"));

            totalSpent += spent;
            totalLimit += limit;
        }

        // Update UI elements
        barChart.getData().add(spentSeries);
        barChart.getData().add(limitSeries);
        dataTable.setItems(tableData);

        totalExpenseLabel.setText(String.format("%,.0f VNĐ", totalSpent));
        totalIncomeLabel.setText(String.format("%,.0f VNĐ", totalLimit));
        balanceLabel.setText(String.format("%,.0f VNĐ", totalLimit - totalSpent));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper class for table data
    public static class ReportItem {
        private final String category;
        private final double amount;
        private double percentage;
        private final String type;

        public ReportItem(String category, double amount, double percentage, String type) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
            this.type = type;
        }

        public String getCategory() { return category; }
        public double getAmount() { return amount; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
        public String getType() { return type; }
    }
}