package org.cookpro.utils;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }

}
