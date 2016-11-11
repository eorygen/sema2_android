package com.orygenapps.sema.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ashemah on 15/02/2015.
 */
public class TimeConverter {

    public static long currentTimeToMsTimestamp() {
        long ms = System.currentTimeMillis();
        return ms;
    }

    public static long addMinutesToTimestamp(long timestamp, int minutes) {
        return timestamp + (minutes * 60 * 1000L);
    }

    public static int minutesUntilTimestamp(long timestamp) {
        long millisToTimestamp = timestamp - currentTimeToMsTimestamp();
        double minsToTimestamp = millisToTimestamp / 60000.0f;
        return (int) Math.ceil(minsToTimestamp);
    }

    public static int secondsUntilTimestamp(long timestamp) {
        long millisToTimestamp = timestamp - currentTimeToMsTimestamp();
        double secondsToTimestamp = millisToTimestamp / 1000.0f;
        return (int) Math.ceil(secondsToTimestamp);
    }

    public static String convertTimestampToDateString(long timeStamp) {
        Date date = new Date(timeStamp);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String dateString = df.format(date);

        return dateString;
    }

    public static int getDayFromTimestamp(long timeStamp) {
        Date date = new Date(timeStamp);

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static long minutesSinceTimestamp(long ts) {
        long seconds = (currentTimeToMsTimestamp() - ts) / 1000;
        long minutes = (long)Math.floor(seconds / 60);
        return minutes;
    }

    public static String wordedTimeSinceTimestamp(long ts) {

        if (ts == 0) {
            return "";
        }

        long minutesSinceTimestamp = TimeConverter.minutesSinceTimestamp(ts);

        String string;
        if (minutesSinceTimestamp == 0) {
            string = "moments ago";
        }
        else if (minutesSinceTimestamp == 1) {
            string = "1 minute ago";
        }
        else if (minutesSinceTimestamp <= 10) {
            string = minutesSinceTimestamp + " minutes ago";
        }
        else {

            Date date = new Date(ts);

            DateFormat timeDF = new SimpleDateFormat("h:mm a");
            String timeString = timeDF.format(date);

            DateFormat dateDF = new SimpleDateFormat("MMM dd");
            String dateString = dateDF.format(date);

            string = "at " + timeString + " on " +  dateString;
        }

        return string;
    }
}
