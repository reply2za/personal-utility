package com.hoursofza.personalutility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WallpaperManager{
    List<String> wallpapers;
    String directory;
    Random rand = new Random();
    private static final String[] VALID_EXTENSIONS = new String[]{"jpg", "jpeg", "png"};
 
    public void setRandomWallpaper() {
        int item = rand.nextInt(wallpapers.size());
        setCurrentWallpaper(directory + wallpapers.get(item));
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
            wallpapers = Stream.of(sendCommandToShell(getWallpapersCmd).split("\n")).filter(item-> {
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

    private String sendCommandToShell(String command) throws InterruptedException, IOException {
        String[] args = { "/bin/bash", "-c", command };
        Process proc = new ProcessBuilder(args).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        proc.waitFor();
        return sb.toString();
    }


 }
