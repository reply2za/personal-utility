package com.hoursofza.personalutility.view;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.springframework.stereotype.Component;

import com.formdev.flatlaf.FlatLightLaf;

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
        tabbedPane.addTab("single", wallpaperView.initSingleWallpaperSection());
        tabbedPane.addTab("interval", wallpaperView.initWallpaperCronSection());
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

}
