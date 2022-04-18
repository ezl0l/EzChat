package com.ezlol.ezchat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {
    public static String timestampToDatetime(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp * 1000);
        return new SimpleDateFormat("HH:mm", Locale.ROOT).format(calendar.getTime());
    }
}
