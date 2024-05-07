package com.hoursofza.personalutility.view;

import javax.swing.*;

import org.springframework.stereotype.Component;

import com.formdev.flatlaf.FlatLightLaf;
import com.hoursofza.personalutility.view.panels.MousePanelView;
import com.hoursofza.personalutility.view.panels.WallpaperView;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.ServerSocket;

@Component
public class MainView {
    JFrame mainFrame = new JFrame();
    static int PORT = 47181;


    static {
        FlatLightLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {

        }
        assertNoOtherInstanceRunning();
    }

    	public static void assertNoOtherInstanceRunning() {     
		new Thread(() -> {
		final AutoCloseable ac;
			try {
				ac = new ServerSocket(47181).accept();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "app is already running...");
				System.exit(1);
				return;
			}
			Runtime.getRuntime().addShutdownHook(new Thread(()-> {
				try {
					ac.close();
				} catch (Exception ignored) {
					
				}
			})); 
		}).start();  

	}

    MainView(WallpaperView wallpaperView, MousePanelView mousePanelView) {
        mainFrame.setTitle("Personal Utility");
        JTabbedPane tabbedPane = new JTabbedPane();
        int metaKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, metaKey),
                "doQuitAction");
        tabbedPane.getActionMap().put("doQuitAction", new CloseAction());
        tabbedPane.addTab("general", mousePanelView.getPanel());
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
            toFront();
            mainFrame.requestFocus();
            mainFrame.repaint();
            toFront();
        });
        mainFrame.setVisible(true);
    }

    public void toFront() {
        int sta = mainFrame.getExtendedState() & ~JFrame.ICONIFIED & JFrame.NORMAL;
        mainFrame.setExtendedState(sta);
        mainFrame.setAlwaysOnTop(true);
        mainFrame.toFront();
        mainFrame.requestFocus();
        mainFrame.setAlwaysOnTop(false);
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
