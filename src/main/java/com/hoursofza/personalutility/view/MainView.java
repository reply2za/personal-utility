package com.hoursofza.personalutility.view;

import javax.swing.*;

import com.hoursofza.personalutility.utils.ShellUtils;
import org.springframework.stereotype.Component;

import com.formdev.flatlaf.FlatLightLaf;
import com.hoursofza.personalutility.view.panels.MousePanelView;
import com.hoursofza.personalutility.view.panels.WallpaperView;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

@Component
@Slf4j
public class MainView {
    private static final JFrame mainFrame = new JFrame();
    private static final int PORT = 47181;


    static {
        FlatLightLaf.setup();
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            log.error(e.getMessage());
        }
        assertNoOtherInstanceRunning();
    }

    public static void assertNoOtherInstanceRunning() {
        new Thread(() -> {
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(PORT);
                log.info("No other instance detected. Running as primary instance.");

                ServerSocket finalServerSocket = serverSocket;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        finalServerSocket.close();
                        log.info("Server socket closed.");
                    } catch (IOException e) {
                        log.error("Error closing server socket: " + e.getMessage());
                    }
                }));

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                        String message = in.readLine();
                        if ("BRING_TO_FRONT".equals(message)) {
                            log.info("Received BRING_TO_FRONT command.");
                            bringAppToFront();
                        }
                    } catch (IOException e) {
                        log.warn("Error handling client connection: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                // If ServerSocket creation fails, assume another instance is running
                log.info("Another instance detected. Attempting to send BRING_TO_FRONT command.");
                JOptionPane.showMessageDialog(null, "Application is already running.");
                sendBringToFrontCommand();
                System.exit(0);
            }
        }, "SingleInstanceThread").start();
    }

    private static void bringAppToFront() {
        mainFrame.setVisible(true);
        mainFrame.toFront();
    }

    /**
     * Sends a BRING_TO_FRONT command to the primary instance.
     */
    private static void sendBringToFrontCommand() {
        try (Socket socket = new Socket("localhost", PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println("BRING_TO_FRONT");
            log.info("BRING_TO_FRONT command sent to primary instance.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Another instance is running, but couldn't communicate with it.");
        }
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
        mainFrame.toFront();
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
