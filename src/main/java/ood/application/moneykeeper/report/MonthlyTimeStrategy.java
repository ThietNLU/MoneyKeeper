package ood.application.moneykeeper.report;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class MonthlyTimeStrategy implements ReportTimeStrategy {
    @Override
    public LocalDateTime[] calculateReportPeriod() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23).withMinute(59).withSecond(59);
        return new LocalDateTime[] {startOfMonth, endOfMonth};
    }
}