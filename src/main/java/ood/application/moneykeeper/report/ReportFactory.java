package ood.application.moneykeeper.report;

import java.sql.SQLException;

public class ReportFactory {
    public static Report createReport(ReportType type) throws SQLException {
        switch (type) {
            case CATEGORY:
                return new CategoryReport();
            case BUDGET:
                return new BudgetReport();
            default:
                throw new IllegalArgumentException("Unsupported report type");
        }
    }

    public enum ReportType {
        CATEGORY,
        BUDGET
    }
}
