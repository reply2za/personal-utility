package com.hoursofza.personalutility.services;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MouseService {

    ScheduledExecutorService SERVICE = Scheduler.getService();
    Point prevPosition = null; 
    ScheduledFuture<?> taskFuture;
    ScheduledFuture<?> pollingFuture;
    Robot robot;
    private static final int MIN_DIST = 2;
    private static final int MAX_DIST = 5;
    int pollingDelay = 10;
    private final static int INIT_POLL_DELAY_SEC = 2;
    Runnable pollRunnable;
    long moveMouseInterval;

    public void init() throws AWTException {
        try {
            robot = new Robot();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        }

    }
    public void schedule(long seconds) throws InterruptedException {
        Point starting;
        try {
            starting = getCurrentPos();
            robot = new Robot();
            if(starting.x == 10) {
                robot.mouseMove(20, 10);
            } else {
                robot.mouseMove(10,10);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Thread.sleep(30);
        Point testPos = getCurrentPos();
        robot.mouseMove(starting.x, starting.y);
        if (starting.equals(testPos)) {
            JOptionPane.showMessageDialog(null, "cannot move mouse: missing permissions");
            return;
        }
        this.moveMouseInterval = seconds;
        if (seconds > pollingDelay) {
            pollRunnable = () -> {
                pollingFuture = SERVICE.scheduleAtFixedRate(this::restartSchedulerIfMoved, INIT_POLL_DELAY_SEC, pollingDelay, TimeUnit.SECONDS);
            };
        } else pollRunnable = ()-> {};
        pollRunnable.run();
        startScheduler();
    }

    public void stop() {
        taskFuture.cancel(true);
    }

    private void singleRun() {
        if (pollingFuture != null) pollingFuture.cancel(true);
        Point currentPosition = getCurrentPos();
        if (isIdle(currentPosition)) {
            int pointX = currentPosition.x + getRandomOffset();
            int pointY = currentPosition.y + getRandomOffset();
            prevPosition = new Point(pointX, pointY);
            robot.mouseMove(pointX, pointY);
        } else {
            prevPosition = new Point(currentPosition.x, currentPosition.y);
        }
        pollRunnable.run();
    }

    private void restartSchedulerIfMoved() {
        if (!isIdle(MouseInfo.getPointerInfo().getLocation())) {
            taskFuture.cancel(true);
            startScheduler();
        }
    }

    private boolean isIdle(Point currentPos) {
        return (Math.abs(currentPos.getX() - prevPosition.x) < MIN_DIST && Math.abs(currentPos.getY() - prevPosition.y) < MIN_DIST);
    }

    private int getRandomOffset() {
        Random r = new Random();
        int sign = r.nextInt(2) > 0 ? 1 : -1;
        return (r.nextInt(MAX_DIST) + 1) * sign;
    }

    private void startScheduler() {
        prevPosition = getCurrentPos();
        taskFuture = SERVICE.scheduleAtFixedRate(()-> {
            try {
                singleRun();
            } catch(Exception e) {
                log.error("could not run single execution: {}", e.getMessage());
            }
        }
        , moveMouseInterval, moveMouseInterval, TimeUnit.SECONDS);
    }

    private Point getCurrentPos() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    
}
