package ood.application.moneykeeper.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeUtils {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String COMPACT_DATE_TIME_FORMAT = "yyyyMMddHHmmss";
    public static final String READABLE_DATE_FORMAT = "dd/MM/yyyy";
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static LocalDateTime parse(String dateTimeString, String pattern) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }

        // Try to parse as a formatted date string
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            // If that fails, try to parse as milliseconds since epoch
            try {
                long epochMilli = Long.parseLong(dateTimeString);
                return LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(epochMilli),
                        java.time.ZoneId.systemDefault()
                );
            } catch (NumberFormatException ex) {
                throw new DateTimeParseException(
                        "Text '" + dateTimeString + "' could not be parsed as DateTime or epoch millis",
                        dateTimeString, 0, e
                );
            }
        }
    }
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatDefault(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_DATE_TIME_FORMAT);
    }

}