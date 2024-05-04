package com.hoursofza.personalutility.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.hoursofza.personalutility.view.SystemTray;

public class ShellUtils {
        public static String sendCommandToShell(String command) throws InterruptedException, IOException {
        String[] args;
        if (SystemTray.windows) {
            args = new String[]{ "powershell.exe", command };
        } else {
            args = new String[]{ "/bin/bash", "-c", command };
        }
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
