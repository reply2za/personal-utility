package com.hoursofza.personalutility.utils;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeUtils {
    
        /**
     * 
     * @param timeTxt
     * @return hour, min, second in array format
     */
    public static int[] convertTimeToArr(String timeTxt) {
        int startHour = 0;
        int startMinute = 0;
        int startSec = 0;
        boolean isPM = false;
        timeTxt = timeTxt.toLowerCase();
        if (timeTxt.contains("am")) {
            timeTxt = timeTxt.replace("am", "");
        } else if (timeTxt.contains("pm")){
            timeTxt = timeTxt.replace("pm", "");
            isPM = true;
        }
        String[] vals = timeTxt.split(":");
        if (vals.length != 2) {
            throw new IllegalStateException("invalid start time format");
        }
        startSec = 0;
        try {
            startHour = Integer.parseInt(vals[0].trim());
            startMinute = Integer.parseInt(vals[1].trim());
        } catch(Exception e) {
            throw new IllegalStateException("Cannot parse start time");
        }
        if (isPM && startHour < 12) startHour += 12;
        return new int[]{startHour, startMinute, startSec};
    }

        public static long timeToMS(int startHour, int startMin, int startSec) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextRun = now.withHour(startHour).withMinute(startMin).withSecond(startSec);
        if (now.compareTo(nextRun) > 0) nextRun = nextRun.plusDays(1);

        Duration duration = Duration.between(now, nextRun);
        long initialDelay = duration.getSeconds() * 1000;
        return initialDelay;
    }
}
