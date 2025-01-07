package com.hoursofza.personalutility.view.panels;

import java.awt.Dimension;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.hoursofza.personalutility.services.Scheduler;
import com.hoursofza.personalutility.services.WallpaperManager;
import com.hoursofza.personalutility.utils.TimeUtils;

import net.miginfocom.swing.MigLayout;

@Slf4j
@Component
public class WallpaperView {
    private static final int ONE_SECOND_MS = 1000;
    private static final int ONE_MIN_MS = ONE_SECOND_MS * 60;
    private static final int ONE_HOUR_MS = ONE_MIN_MS * 60;
    private String lastChosenDir;
    Preferences p = Preferences.userRoot();
    String directory;
    WallpaperManager wallpaperTask;
    private JTextField hourTF;
    private JTextField minTF;
    private JTextField secTF;
    private final static String FORWARD_SLASH = "/";
    boolean isRunningInterval = false;
    JButton setSingleWallpaperBtn;
    JButton setIntervalWallpapersBtn;
    JCheckBox startTimeCB;
    JLabel startTimeLabel;
    JTextField startTimeTextField;
    private static final boolean DEBUG_LAYOUT = false;
    private static final String INIT_LAYOUT_TXT = DEBUG_LAYOUT ? "debug": "";
    ScheduledExecutorService SERVICE = Scheduler.getService();
    private ScheduledFuture<?> wallpaperFuture;
    private JCheckBox randomizeCB;
    private JLabel selectedWallpaperLabel;


    WallpaperView(WallpaperManager wallpaperTask) {
        this.wallpaperTask = wallpaperTask;
    }

    public JComponent initIntervalWallpaperSection() {
        JPanel mainPanel = new JPanel(new MigLayout(INIT_LAYOUT_TXT));
        lastChosenDir = p.get("wpDir", null);
        setIntervalWallpapersBtn = new JButton("set wallpapers");
        setIntervalWallpapersBtn.addActionListener((ae) -> startWallpaperIntervalAction(mainPanel));
        mainPanel.add(new JLabel("set wallpapers from a folder on an interval"), "shrinky,wrap,align 50%");
        addWallpaperDirectorySelection(mainPanel);
        selectedWallpaperLabel = new JLabel();
        mainPanel.add(getIntervalTimePanel(), "wrap");
        mainPanel.add(getStartTimePanel(), "wrap");
        mainPanel.add(setIntervalWallpapersBtn, "wrap");
        mainPanel.add(selectedWallpaperLabel);
        return new JScrollPane(mainPanel);
    }

    private JPanel getStartTimePanel() {
        JPanel startTimeJPanel = new JPanel(new MigLayout("hidemode 1, insets 0"));
        randomizeCB = new JCheckBox("randomize wallpapers");
        startTimeCB = new JCheckBox("start at specific time");
        startTimeCB.addActionListener((ae)-> {
            boolean startTimeEnabled = startTimeCB.isSelected();
            startTimeLabel.setVisible(startTimeEnabled);
            startTimeTextField.setVisible(startTimeEnabled);
        });
        startTimeLabel = new JLabel("start time (ex: 4:30 PM): ");
        startTimeTextField = new JTextField(8);
        startTimeLabel.setVisible(false);
        startTimeTextField.setVisible(false);
        startTimeJPanel.add(startTimeCB, "pad 0 -3.5 0 0");
        startTimeJPanel.add(randomizeCB, "wrap");
        startTimeJPanel.add(startTimeLabel);
        startTimeJPanel.add(startTimeTextField);
        return startTimeJPanel;
    }

    private JPanel getIntervalTimePanel() {
        JLabel intervalLabel = new JLabel("switch every:");
        JLabel hourLabel = new JLabel("hours");
        JLabel minLabel = new JLabel("minutes");
        JLabel secLabel = new JLabel("seconds");
        hourTF = new JTextField(2);
        minTF = new JTextField(2);
        secTF = new JTextField(2);
        JPanel timePanel = new JPanel(new MigLayout("insets 0"));
        timePanel.add(intervalLabel);
        String timeLabelConstraint = "pad 0 -4 0 0 ";
        timePanel.add(hourTF);
        timePanel.add(hourLabel, timeLabelConstraint);
        timePanel.add(minTF);
        timePanel.add(minLabel, timeLabelConstraint);
        timePanel.add(secTF);
        timePanel.add(secLabel, timeLabelConstraint);
        return timePanel;
    }

    private void addWallpaperDirectorySelection(JPanel attachTo) {
        JTextField wallpaperDirTF = new JTextField(20);
        wallpaperDirTF.setEditable(false);
        wallpaperDirTF.setFocusable(false);
        JButton setWallpaperDir = new JButton("set wallpaper folder");
        setWallpaperDir.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (lastChosenDir != null && !lastChosenDir.isBlank()) {
                fileChooser.setSelectedFile(new File(lastChosenDir));
            }
            fileChooser.setDialogTitle("Select Folder");
            fileChooser.setApproveButtonText("Select Folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int res = fileChooser.showOpenDialog(attachTo);
            if (res == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
                directory = getDirectory(fileChooser.getSelectedFile().getAbsolutePath());
                wallpaperDirTF.setText(directory);
            }
        });
        JPanel wallpaperDirPanel = new JPanel();
        wallpaperDirPanel.add(setWallpaperDir);
        wallpaperDirPanel.add(wallpaperDirTF);
        attachTo.add(wallpaperDirPanel, "wrap");
    }

    public JPanel initSingleWallpaperSection() {
        JPanel mainPanel = new JPanel(new MigLayout());
        mainPanel.add(new JLabel("set wallpaper immediately"), "wrap");
        setSingleWallpaperBtn = new JButton("set wallpaper");
        setSingleWallpaperBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (lastChosenDir != null)
                fileChooser.setCurrentDirectory(new File(lastChosenDir));
            int res = fileChooser.showOpenDialog(mainPanel);
            if (res == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
                lastChosenDir = fileChooser.getSelectedFile().getAbsolutePath();
                String[] directoryAndFile = fileChooser.getSelectedFile().getAbsolutePath().split(FORWARD_SLASH);
                if (directoryAndFile.length < 1) {
                    JOptionPane.showMessageDialog(mainPanel, "there was an issue setting the wallpaper");
                    return;
                }
                String file = directoryAndFile[directoryAndFile.length - 1];
                this.directory = getDirectory(fileChooser.getSelectedFile().getAbsolutePath());
                addWallpaper(mainPanel, file);
                p.put("wpDir", lastChosenDir);
            }
        });
        mainPanel.add(setSingleWallpaperBtn, "wrap");
        mainPanel.setPreferredSize(new Dimension(300, 100));
        return mainPanel;
    }

    private void startWallpaperIntervalAction(JPanel mainPanel) {
        if (isRunningInterval) {
            setIntervalWallpapersBtn.setText("set wallpapers");
            isRunningInterval = false;
            if (wallpaperFuture != null && !wallpaperFuture.isCancelled()) wallpaperFuture.cancel(false);
            wallpaperTask.reset();
        } else {
            if (directory == null) {
                JOptionPane.showMessageDialog(mainPanel, "must set a wallpaper directory", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int hours;
            int min;
            int sec;
            try {
                hours = resolveTime(hourTF);
                min = resolveTime(minTF);
                sec = resolveTime(secTF);
            } catch (IllegalArgumentException iArgumentException) {
                JOptionPane.showMessageDialog(mainPanel, "invalid interval time", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (hours < 0 || min < 0 || sec < 0) {
                JOptionPane.showMessageDialog(mainPanel, "interval time must be positive", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int intervalTime = (sec * ONE_SECOND_MS) + (min * ONE_MIN_MS) + (hours * ONE_HOUR_MS);
            if (intervalTime <= 0) {
                JOptionPane.showMessageDialog(mainPanel, "time must be postive", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int startHour;
            int startMinute;
            int startSec;
            if (startTimeCB.isSelected()) {
                try {
                    int[] res = TimeUtils.convertTimeToArr(startTimeTextField.getText().toLowerCase());
                    startHour = res[0];
                    startMinute = res[1];
                    startSec = res[2];
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "Error", JOptionPane.ERROR);
                    return;
                }
            } else {
                startHour = ZonedDateTime.now().getHour();
                startMinute = ZonedDateTime.now().getMinute();
                startSec = ZonedDateTime.now().getSecond() + 1;
            }
            if (startHour > 23 || startMinute > 59 || startSec > 59) {
                JOptionPane.showMessageDialog(mainPanel, "provided times are out-of-bounds", "Error", JOptionPane.ERROR_MESSAGE);
            }
            setIntervalWallpapersBtn.setText("cancel task");
            wallpaperScheduler(intervalTime, startHour, startMinute, startSec, randomizeCB.isSelected());
            isRunningInterval = true;
        }
    }



    private void addWallpaper(JComponent reference, String fileName) {
        if (fileName.isBlank()) {
            JOptionPane.showMessageDialog(reference, "invalid file name");
            return;
        }
        String fullPath = resolveFullFilepath(this.directory, fileName);
        WallpaperManager.setCurrentWallpaper(fullPath);
    }

    private String resolveFullFilepath(String directory, String file) {
        if (!directory.endsWith(FORWARD_SLASH)) {
            directory += FORWARD_SLASH;
        }
        if (file.startsWith(FORWARD_SLASH)) {
            file = file.substring(1);
        }
        return directory + file;
    }

    private String getDirectory(String path) {
        File f = new File(path);
        if (f.isDirectory())
            return path;
        return removeLastFilePathSection(path);
    }

    private String removeLastFilePathSection(String path) {
        String[] lastChosenDirArr = path.split(FORWARD_SLASH);
        return String.join(FORWARD_SLASH, Arrays.copyOf(lastChosenDirArr, lastChosenDirArr.length - 1));
    }

    private int resolveTime(JTextField component) throws IllegalArgumentException {
        String compText = component.getText();
        if (compText.isBlank()) {
            return 0;
        } else {
            return Integer.parseInt(compText);
        }
    }

    private void wallpaperScheduler(long intervalMS, int startHour, int startMin, int startSec, boolean isRandom) {
        long initialDelay = TimeUtils.timeToMS(startHour, startMin, startSec);
        String localDir = this.directory;
        Runnable setWallpaper;
        if (isRandom) {
            setWallpaper = ()-> {
                selectedWallpaperLabel.setText("last wallpaper: " + wallpaperTask.setRandomWallpaper());
            };
        } else {
            setWallpaper = ()-> {
                selectedWallpaperLabel.setText("last wallpaper: " + wallpaperTask.setNextWallpaper());
            };
        }
        wallpaperFuture = SERVICE.scheduleAtFixedRate(() -> {
            try {
                wallpaperTask.setDirectory(localDir);
                setWallpaper.run();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        },
        initialDelay,
        intervalMS,
        TimeUnit.MILLISECONDS
        );
    }
}
