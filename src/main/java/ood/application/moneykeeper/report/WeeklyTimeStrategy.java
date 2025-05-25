package ood.application.moneykeeper.report;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class WeeklyTimeStrategy implements ReportTimeStrategy {
    @Override
    public LocalDateTime[] calculateReportPeriod() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .withHour(23).withMinute(59).withSecond(59);
        return new LocalDateTime[] {startOfWeek, endOfWeek};
    }
}