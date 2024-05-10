package com.hoursofza.personalutility.services;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.hoursofza.personalutility.utils.ShellUtils;
import com.hoursofza.personalutility.view.SystemTray;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WallpaperManager{
    List<String> wallpapers;
    String directory;
    Random rand = new Random();
    private int currentIndex = 0;
    public static native int SystemParametersInfo(int uiAction,int uiParam,String pvParam,int fWinIni);
    private static final String SLASH;
    private static final String[] VALID_EXTENSIONS = new String[]{"jpg", "jpeg", "png"};

    static
    {
        if (SystemTray.windows) {
            System.loadLibrary("user32");
            SLASH = "\\";
        } else {
            SLASH = "/";
        }
    }

    public static interface User32 extends Library {
        User32 INSTANCE = (User32) Native.loadLibrary("user32",User32.class,W32APIOptions.DEFAULT_OPTIONS);        
        boolean SystemParametersInfo (int one, int two, String s ,int three);         
    }

    public void reset() {
        currentIndex = 0;
    }
    public String setRandomWallpaper() {
        int item = rand.nextInt(wallpapers.size());
        String wallpaper = wallpapers.get(item);
        setCurrentWallpaper(directory + wallpaper);
        return wallpaper;
    }

    public String setNextWallpaper() {
        if (currentIndex >= wallpapers.size()) currentIndex = 0;
        String wallpaper = wallpapers.get(currentIndex);
        setCurrentWallpaper(directory + wallpaper);
        currentIndex++;
        return wallpaper;
    }

    public void setDirectory(String dir) {
        if (!dir.endsWith(SLASH)) {
            dir += SLASH;
        }
        this.directory = dir;
        setWallpaperBasedOnDir();
    }
 
    /**
     * Ensure setDirectory has been called.
     */
    public static void setCurrentWallpaper(String fullPath) {
        if (SystemTray.windows) {
            if (fullPath.startsWith("/")) fullPath = fullPath.substring(1);
            User32.INSTANCE.SystemParametersInfo(0x0014, 0, fullPath , 1);
        } else {
        String script = """
            tell application "System Events"
            tell every desktop
                set picture to "%s"
            end tell
        end tell""".formatted(fullPath);
        String[] args = { "osascript", "-e", script };

        try {
            Process process = new ProcessBuilder(args).start();
            process.waitFor();
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    }

    private void setWallpaperBasedOnDir() {
        String getWallpapersCmd = SystemTray.windows ? "ls -n " + directory : "cd " + directory + " && ls";
        try {
            wallpapers = Stream.of(ShellUtils.sendCommandToShell(getWallpapersCmd).split("\n")).filter(item-> {
                boolean isValid = false;
                for (String ext: VALID_EXTENSIONS) {
                    if (item.endsWith("." + ext)) return true;
                }
                return isValid;
            }).toList();
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage());
        }
    }




 }
