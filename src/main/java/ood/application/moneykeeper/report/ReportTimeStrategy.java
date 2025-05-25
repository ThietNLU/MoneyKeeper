package ood.application.moneykeeper.report;

import java.time.LocalDateTime;

public interface ReportTimeStrategy {
    LocalDateTime[] calculateReportPeriod();
}