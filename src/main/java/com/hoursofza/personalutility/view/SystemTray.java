package com.hoursofza.personalutility.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

@Slf4j
public class SystemTray {
    public static java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();              // set up a system tray icon.
    public static TrayIcon trayIcon;
    @Getter
    public static Image defaultTrayIconImage;
    public static Image activeTrayIconImage;
    public static MenuItem openMainMenu;
    public static MenuItem exitItem;
    final static PopupMenu popup=new PopupMenu();
    public static boolean windows;

    static {
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.awt.UIElement","true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty( "apple.awt.application.appearance", "system" );
            windows = false;
          } else {
            windows = true;
          }
        URL defaultImageURL = SystemTray.class.getClassLoader().getResource("square.png");
        URL activeImageURL = SystemTray.class.getClassLoader().getResource("square_active.png");

        try {
            if (defaultImageURL != null) {
                defaultTrayIconImage = ImageIO.read(defaultImageURL);
            }
            if (activeImageURL != null) {
                activeTrayIconImage = ImageIO.read(activeImageURL);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SystemTray() {
    }

    public static void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            Toolkit.getDefaultToolkit();
            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                log.error("No system tray support, application exiting.");
                return;
            }

            trayIcon = new TrayIcon(scaleImage(defaultTrayIconImage));
            openMainMenu = new MenuItem("Open Main Menu");

            // and select the exit option, this will shutdown JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).

            exitItem = new MenuItem("Quit");
            exitItem.addActionListener(event -> {
                tray.remove(trayIcon);
                System.exit(0);
            });
            // setup the popup menu for the application.
            popup.add(openMainMenu);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);

        } catch (Exception e) {
            log.error("Unable to init system tray: {}", e.getMessage());
        }
    }

    /**
     * Sets the tray icon.
     * @param isActive - Whether the mouse-service is active
     */
    public static void setTrayIcon(boolean isActive) {
        trayIcon.setImage(scaleImage(isActive ? activeTrayIconImage : defaultTrayIconImage));
    }

    private static Image scaleImage(Image image) {
        if (windows) {
            return image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        }
        return image;
    }
}
