package com.hoursofza.personalutility.services;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.hoursofza.personalutility.utils.ShellUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WallpaperManager{
    List<String> wallpapers;
    String directory;
    Random rand = new Random();
    private int currentIndex = 0;

    private static final String[] VALID_EXTENSIONS = new String[]{"jpg", "jpeg", "png"};

    public void reset() {
        currentIndex = 0;
    }
    public void setRandomWallpaper() {
        int item = rand.nextInt(wallpapers.size());
        setCurrentWallpaper(directory + wallpapers.get(item));
    }

    public void setNextWallpaper() {
        if (currentIndex >= wallpapers.size()) currentIndex = 0;
        String wallpaper = wallpapers.get(currentIndex);
        setCurrentWallpaper(directory + wallpaper);
        currentIndex++;
    }

    public void setDirectory(String dir) {
        if (!dir.endsWith("/")) {
            dir += "/";
        }
        this.directory = dir;
        setWallpaperBasedOnDir();
    }
 
 
    /**
     * Ensure setDirectory has been called.
     */
    public static void setCurrentWallpaper(String fullPath) {
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

    private void setWallpaperBasedOnDir() {
        String getWallpapersCmd = "cd " + directory + "&& ls";
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
