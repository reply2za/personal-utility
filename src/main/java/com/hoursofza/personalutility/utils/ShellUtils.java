package com.hoursofza.personalutility.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellUtils {
        public static String sendCommandToShell(String command) throws InterruptedException, IOException {
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
