package com.hoursofza.personalutility.view;

import javax.swing.*;

import org.springframework.stereotype.Component;

import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;

@Component
public class MainView {
    JFrame mainFrame = new JFrame();

    static {
        FlatLightLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {

        }
    }

    MainView(WallpaperView wallpaperView) {
        mainFrame.setTitle("Personal Utility");
        JTabbedPane tabbedPane = new JTabbedPane();
        int metaKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, metaKey), "doEnterAction");
        tabbedPane.getActionMap().put("doEnterAction", new CloseAction());
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
