package ood.application.moneykeeper.report;

import java.time.LocalDateTime;

public class CustomTimeStrategy implements ReportTimeStrategy {
    private LocalDateTime start;
    private LocalDateTime end;

    public CustomTimeStrategy(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public LocalDateTime[] calculateReportPeriod() {
        return new LocalDateTime[] {start, end};
    }
}