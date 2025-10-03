package com.dark.videostreaming.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TimeUtil {
    
    static final List<ChronoUnit> UNITS = List.of(
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.WEEKS,
            ChronoUnit.DAYS
    );
    
    public static String timeSince(LocalDate date) {
        LocalDate currentTime = LocalDate.now();
        
        for (ChronoUnit unit: UNITS) {
            long diff = unit.between(date, currentTime);
            if ( diff >= 1) {
                return diff + "+ " + unit.name().toLowerCase();
            }
        }
        return "now.";
    }
}
