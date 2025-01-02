package com.hoursofza.personalutility.view.panels;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.*;

import com.hoursofza.personalutility.view.SystemTray;
import org.springframework.stereotype.Component;

import com.hoursofza.personalutility.services.MouseService;
import com.hoursofza.personalutility.services.Scheduler;
import com.hoursofza.personalutility.utils.TimeUtils;

import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;


@Component
@Slf4j
public class MousePanelView {
    private static final String MOVE_MOUSE = "move mouse";
    private static final String STOP_MOUSE = "stop mouse";
    private final MouseService mouseService;
    ScheduledExecutorService SERVICE = Scheduler.getService();
    ScheduledFuture<?> cancelFuture;
    Preferences preferences = Preferences.userRoot(); 
    JPanel panel;

    
    MousePanelView(MouseService mouseService) {
        this.mouseService = mouseService;
                try {
            mouseService.init();
        } catch (AWTException e) {
            log.error("could not initialize mouse service: ", e);
        }
    }

    public JPanel getPanel() {
        if (panel == null) panel = initPanel();
        return panel;
    }
    
      private JPanel initPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("", "[][]"));
        JButton moveMouseBtn = new JButton(MOVE_MOUSE);
        JPanel intervalPanel = new JPanel();
        JLabel delayLabel = new JLabel("interval (seconds): ");
        JTextField delayTF = new JTextField(8);
        intervalPanel.add(delayLabel);
        intervalPanel.add(delayTF);

        JPanel endTimePanel = new JPanel();
        JLabel endTimeLabel = new JLabel("end time:");
        JTextField endTimeTF = new JTextField(8);
        endTimePanel.add(endTimeLabel);
        endTimePanel.add(endTimeTF);

        JPanel zenPanel = new JPanel();
        JLabel zenLabel = new JLabel("zen jiggle:");
        JCheckBox zenCheckBox = new JCheckBox();
        zenPanel.add(zenLabel);
        zenPanel.add(zenCheckBox);


        mainPanel.add(intervalPanel, "wrap");
        mainPanel.add(endTimePanel, "wrap");
        mainPanel.add(zenPanel, "wrap");
        mainPanel.add(moveMouseBtn, "wrap");

        Runnable stopAction = () -> {
            moveMouseBtn.setText(MOVE_MOUSE);
            delayTF.setEditable(true);
            endTimeTF.setEditable(true);
            zenCheckBox.setEnabled(true);
            mouseService.stop();
        };
        moveMouseBtn.addActionListener((ae) -> {
            if (moveMouseBtn.getText().contains(MOVE_MOUSE)) {
                if (!endTimeTF.getText().isBlank()) {
                    int[] res;
                    try {
                        res = TimeUtils.convertTimeToArr(endTimeTF.getText());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    long initialDelay = TimeUtils.timeToMS(res[0], res[1], res[2]);
                    cancelFuture = SERVICE.schedule(() -> {
                        try {
                            stopAction.run();
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    },
                            initialDelay,
                            TimeUnit.MILLISECONDS);
                }
                long delay;
                try {
                    delay = Long.parseLong(delayTF.getText());
                } catch (Exception ignored) {
                    delay = -1;
                }
                if (delay < 1) {
                    stopAction.run();
                    JOptionPane.showMessageDialog(mainPanel, "invalid interval", "error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                long finalDelay = delay;
                try {
                    mouseService.schedule(finalDelay, zenCheckBox.isSelected());
                } catch (Exception e) {
                    stopAction.run();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(mainPanel, e.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                preferences.put("mouse.zen", Boolean.toString(zenCheckBox.isSelected()));
                preferences.put("mouse.interval", delayTF.getText());
                preferences.put("mouse.endTime", endTimeTF.getText());
                moveMouseBtn.setText(STOP_MOUSE);
                delayTF.setEditable(false);
                endTimeTF.setEditable(false);
                zenCheckBox.setEnabled(false);
            } else {
                stopAction.run();
            }
        });
        zenCheckBox.setSelected(Boolean.parseBoolean(preferences.get("mouse.zen", "false")));
        delayTF.setText(preferences.get("mouse.interval", ""));
        endTimeTF.setText(preferences.get("mouse.endTime", ""));
        return mainPanel;
    }
}
