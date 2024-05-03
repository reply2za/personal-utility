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

import com.hoursofza.personalutility.utils.ShellUtils;

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
    private static final int ZEN_DIST = 3;
    int pollingDelay = 10;
    private final static int INIT_POLL_DELAY_SEC = 2;
    Runnable pollRunnable;
    long moveMouseInterval;
    boolean isZenJiggle;
    Random random = new Random();


    public void init() throws AWTException {
        try {
            robot = new Robot();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void schedule(long seconds, boolean isZenJiggle) throws InterruptedException {
        Point starting;
        this.isZenJiggle = isZenJiggle;
        try {
            starting = getCurrentPos();
            robot = new Robot();
            if (starting.x == 10) {
                robot.mouseMove(20, 10);
            } else {
                robot.mouseMove(10, 10);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Thread.sleep(100);
        Point testPos = getCurrentPos();
        robot.mouseMove(starting.x, starting.y);
        if (Math.abs(starting.x - testPos.x) < 10 && Math.abs(starting.y - testPos.y) < 10) {
            try {
                ShellUtils.sendCommandToShell("tccutil reset Accessibility \"com.hoursofza.personalutility\"");
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new RuntimeException("cannot move mouse: missing permissions");
        }
        this.moveMouseInterval = seconds;
        if (seconds > pollingDelay) {
            pollRunnable = () -> {
                pollingFuture = SERVICE.scheduleAtFixedRate(this::restartSchedulerIfMoved, INIT_POLL_DELAY_SEC,
                        pollingDelay, TimeUnit.SECONDS);
            };
        } else
            pollRunnable = () -> {
            };
        pollRunnable.run();
        startScheduler();
    }

    public void stop() {
        if (taskFuture != null)
            taskFuture.cancel(true);
    }

    private void singleRun() {
        if (pollingFuture != null)
            pollingFuture.cancel(true);
        Point initialPosition = getCurrentPos();
        if (isIdle(initialPosition)) {
            if (this.isZenJiggle) {
                moveMouseRandom(initialPosition, ZEN_DIST);
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                prevPosition = initialPosition;
                robot.mouseMove(initialPosition.x, initialPosition.y);
            } else {
                moveMouseRandom(initialPosition, MAX_DIST);
            }
        } else {
            prevPosition = new Point(initialPosition.x, initialPosition.y);
        }
        pollRunnable.run();
    }

    private void moveMouseRandom(Point initialPosition, int distance) {
        int pointX = initialPosition.x + getRandomOffset(distance);
        int pointY = initialPosition.y + getRandomOffset(distance);
        prevPosition = new Point(pointX, pointY);
        robot.mouseMove(pointX, pointY);
    }

    private void restartSchedulerIfMoved() {
        if (!isIdle(MouseInfo.getPointerInfo().getLocation())) {
            taskFuture.cancel(true);
            startScheduler();
        }
    }

    private boolean isIdle(Point currentPos) {
        return (Math.abs(currentPos.getX() - prevPosition.x) < MIN_DIST
                && Math.abs(currentPos.getY() - prevPosition.y) < MIN_DIST);
    }

    private int getRandomOffset(int distance) {
        int sign = random.nextInt(2) > 0 ? 1 : -1;
        return (random.nextInt(distance) + 1) * sign;
    }

    private void startScheduler() {
        prevPosition = getCurrentPos();
        taskFuture = SERVICE.scheduleAtFixedRate(() -> {
            try {
                singleRun();
            } catch (Exception e) {
                log.error("could not run single execution: {}", e.getMessage());
            }
        }, moveMouseInterval, moveMouseInterval, TimeUnit.SECONDS);
    }

    private Point getCurrentPos() {
        return MouseInfo.getPointerInfo().getLocation();
    }

}
