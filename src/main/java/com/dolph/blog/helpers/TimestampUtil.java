package com.dolph.blog.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampUtil {

    public static String getTimestampWithOffset(int minutesToAdd) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime futureDateTime = currentDateTime.plusMinutes(minutesToAdd);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return futureDateTime.format(formatter);
    }

    public static String getTimestamp(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }
}
