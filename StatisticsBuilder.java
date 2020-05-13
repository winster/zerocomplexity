package com.example;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Slf4j
public class StatisticsBuilder {

    private int milliSeconds[] = new int[1000];     // Every millisecond value
    private int seconds[] = new int[60];            // Every second gets its value by summing millis array
    private int minutes[] = new int[60];            // Every minute gets its value by summing seconds array
    private int hour[] = new int[24];               // Every hour gets its value by summing minutes array
    private int day[] = new int[365];               // Every hour gets its value by summing minutes array
    private int currentSecond = Calendar.getInstance().get(Calendar.SECOND);
    private int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
    private int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
    private String currentSecondsLevelHash = currentDay+"-"+currentHour+"-"+currentMinute+"-"+currentSecond;
    private String currentMinutesLevelHash = currentDay+"-"+currentHour+"-"+currentMinute;
    private String currentHourLevelHash = currentDay+"-"+currentHour;

    public void count() {
        Calendar calendar = Calendar.getInstance();
        int newMilliSecond = calendar.get(Calendar.MILLISECOND);
        int newSecond = calendar.get(Calendar.SECOND);
        int newMinute = calendar.get(Calendar.MINUTE);
        int newHour = calendar.get(Calendar.HOUR_OF_DAY);
        int newDay = calendar.get(Calendar.DAY_OF_YEAR);
        String newSecondsLevelHash = newDay+"-"+newHour+"-"+newMinute+"-"+newSecond;
        String newMinutesLevelHash = newDay+"-"+newHour+"-"+newMinute;
        String newHourLevelHash = newDay+"-"+newHour;
        if(!newSecondsLevelHash.equals(currentSecondsLevelHash)){
            int requestsPerSecond = sum(milliSeconds, 1000);
            milliSeconds = new int[1000];
            if(newDay == currentDay) {
                day[currentDay] += requestsPerSecond;
            } else {
                int requestsPerDay = sum(hour, 24);
                day[currentDay] = requestsPerDay;
                hour = new int[60];
            }
            if(newHourLevelHash.equals(currentHourLevelHash)) {
                hour[currentHour] += requestsPerSecond;
            } else {
                int requestsPerHour = sum(minutes, 60);
                hour[currentHour] = requestsPerHour;
                minutes = new int[60];
            }
            if(newMinutesLevelHash.equals(currentMinutesLevelHash)) {
                minutes[currentMinute] += requestsPerSecond;
            } else {
                int requestsPerMinute = sum(seconds, 60);
                minutes[currentMinute] = requestsPerMinute;
                seconds = new int[60];
            }
            seconds[currentSecond] = requestsPerSecond;
        }
        milliSeconds[newMilliSecond]++;
        currentSecond = newSecond;
        currentMinute = newMinute;
        currentHour = newHour;
        currentSecondsLevelHash = currentDay+"-"+currentHour+"-"+currentMinute+"-"+currentSecond;
        currentMinutesLevelHash = currentDay+"-"+currentHour+"-"+currentMinute;
        currentHourLevelHash = currentDay+"-"+currentHour;
    }

    private int sum(int[] array, int size) {
        int result = 0;
        for(int i=0;i<size; ++i) {
            result += array[i];
        }
        return result;
    }

    @ToString
    class Statistics {
        int lastSecond;
        int lastMinute;
        int lastHour;
        int lastDay;
    }

    private Statistics getStatistics(){
        Calendar calendar = Calendar.getInstance();
        int newSecond = calendar.get(Calendar.SECOND);
        int newMinute = calendar.get(Calendar.MINUTE);
        int newHour = calendar.get(Calendar.HOUR_OF_DAY);
        int newDay = calendar.get(Calendar.DAY_OF_YEAR);
        Statistics statistics = new Statistics();
        statistics.lastSecond = seconds[newSecond];
        statistics.lastMinute = minutes[newMinute];
        statistics.lastHour = hour[newHour];
        statistics.lastDay = day[newDay];
        return statistics;
    }

    public static void main(String[] args) {
        StatisticsBuilder statisticsBuilder = new StatisticsBuilder();
        TimerTask task1 = new TimerTask() {
            public void run() {
                statisticsBuilder.count(); // markup
            }
        };
        TimerTask task2 = new TimerTask() {
            public void run() {
                log.info("Statistics {}", statisticsBuilder.getStatistics());
            }
        };
        Timer timer = new Timer("Timer");
        timer.scheduleAtFixedRate(task1, 0L, 500L); // runs twice a second
        timer.scheduleAtFixedRate(task2, 5000L, 5000L); // runs every 5 seconds - should count 10 on every run
    }
}
