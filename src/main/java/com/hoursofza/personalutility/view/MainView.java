package com.hoursofza.personalutility.view;

import javax.swing.*;

import org.springframework.stereotype.Component;

import com.formdev.flatlaf.FlatLightLaf;
import com.hoursofza.personalutility.services.MouseService;

import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MainView {
    JFrame mainFrame = new JFrame();
    private static String MOVE_MOUSE = "move mouse";
    private static String STOP_MOUSE = "stop mouse";
    private MouseService mouseService;

    static {
        FlatLightLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {

        }
    }

    MainView(WallpaperView wallpaperView, MouseService mouseService) {
        this.mouseService = mouseService;
        try {
            mouseService.init();
        } catch (AWTException e) {
            log.error("could not initialize mouse service: ", e);
        }
        mainFrame.setTitle("Personal Utility");
        JTabbedPane tabbedPane = new JTabbedPane();
        int metaKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, metaKey), "doEnterAction");
        tabbedPane.getActionMap().put("doEnterAction", new CloseAction());
        tabbedPane.addTab("general", generalPanel());
        tabbedPane.addTab("single", wallpaperView.initSingleWallpaperSection());
        tabbedPane.addTab("interval", wallpaperView.initIntervalWallpaperSection());
        mainFrame.add(tabbedPane);
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setSize(mainFrame.getWidth(), mainFrame.getHeight() + 50);
        mainFrame.setLocationRelativeTo(null);
        SystemTray.addAppToTray();
        SystemTray.openMainMenu.addActionListener((ae) -> {
            mainFrame.setVisible(true);
        });
        mainFrame.setVisible(true);
    }

    private JPanel generalPanel() {
        JPanel mainPanel = new JPanel(new MigLayout("", "[][]"));
        JButton moveMouseBtn = new JButton(MOVE_MOUSE);
        JLabel delayLabel = new JLabel("interval (seconds): ");
        JTextField delayTF = new JTextField(10);
        moveMouseBtn.addActionListener((ae) -> {
            if (moveMouseBtn.getText().contains(MOVE_MOUSE)) {
                long delay; 
                try {
                    delay = Long.parseLong(delayTF.getText());
                } catch (Exception ignored) {
                    delay = -1;
                }
                if (delay < 1) {
                    JOptionPane.showMessageDialog(mainPanel, "invalid interval");
                    return;
                }
                moveMouseBtn.setText(STOP_MOUSE);
                long finalDelay = delay;
                CompletableFuture.runAsync(()-> {
                    try {
                        mouseService.schedule(finalDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                moveMouseBtn.setText(MOVE_MOUSE);
                mouseService.stop();
            }
        });
        mainPanel.add(delayLabel);
        mainPanel.add(delayTF, "wrap");
        mainPanel.add(moveMouseBtn);
        return mainPanel;
    }

    private final class CloseAction implements Action {

        @Override
        public Object getValue(String s) {
            return null;
        }

        @Override
        public void putValue(String s, Object o) {

        }

        @Override
        public void setEnabled(boolean b) {
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {

        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {

        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            mainFrame.setVisible(false);
        }
    }

}
