package com.hoursofza.personalutility.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Scheduler {
    private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(2);


    public static ScheduledExecutorService getService() {
        return SERVICE;
    }
}
