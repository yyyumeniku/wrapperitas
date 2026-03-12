package com.yume.wrapperitas.util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameRestarter {

    public static void restart() {
        try {
            System.out.println("[Wrapperitas] Attempting to restart game...");
            List<String> commandArgs = buildRestartCommand();
            System.out.println("[Wrapperitas] Relaunch command constructed.");

            try {
                if (org.lwjgl.opengl.Display.isCreated()) {
                    org.lwjgl.opengl.Display.destroy();
                }
            } catch (Throwable ignored) {}

            new ProcessBuilder(commandArgs)
                    .directory(new File("."))
                    .inheritIO()
                    .start();

            shutdownCurrentMinecraftInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("[Wrapperitas] Restart failed. Please restart manually.");
        }
    }

    private static List<String> buildRestartCommand() {
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(getJavaExecutable());
        commandArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        
        String classpath = System.getProperty("java.class.path", "");
        if (!classpath.isEmpty()) {
            commandArgs.add("-cp");
            commandArgs.add(classpath);
        }

        String sunCmd = System.getProperty("sun.java.command", "");
        if (sunCmd.contains("org.prismlauncher.EntryPoint")) {
            commandArgs.add("net.minecraft.launchwrapper.Launch");
            commandArgs.addAll(readLaunchArgsFromBlackboard());
        } else {
            commandArgs.addAll(splitCommandLine(sunCmd));
        }

        return commandArgs;
    }

    private static List<String> readLaunchArgsFromBlackboard() {
        List<String> args = new ArrayList<>();
        try {
            Class<?> launchClass = Class.forName("net.minecraft.launchwrapper.Launch");
            Field blackboardField = launchClass.getField("blackboard");
            @SuppressWarnings("unchecked")
            Map<String, Object> blackboard = (Map<String, Object>) blackboardField.get(null);
            
            Object launchArgsObj = blackboard.get("launchArgs");
            if (launchArgsObj == null) {
                for (Map.Entry<String, Object> entry : blackboard.entrySet()) {
                    if (entry.getKey() != null && entry.getKey().toLowerCase().contains("launch")) {
                        launchArgsObj = entry.getValue();
                        break;
                    }
                }
            }

            if (launchArgsObj instanceof String[]) {
                for (String arg : (String[]) launchArgsObj) args.add(arg);
            } else if (launchArgsObj instanceof List) {
                for (Object arg : (List<?>) launchArgsObj) args.add(String.valueOf(arg));
            } else if (launchArgsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapArgs = (Map<String, Object>) launchArgsObj;
                for (Map.Entry<String, Object> entry : mapArgs.entrySet()) {
                    args.add(entry.getKey().startsWith("--") ? entry.getKey() : "--" + entry.getKey());
                    if (entry.getValue() != null && !String.valueOf(entry.getValue()).isEmpty()) {
                        args.add(String.valueOf(entry.getValue()));
                    }
                }
            }
        } catch (Throwable ignored) {}
        return args;
    }

    private static List<String> splitCommandLine(String commandLine) {
        List<String> parts = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean inQuotes = false;
        for (char c : commandLine.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(c) && !inQuotes) {
                if (token.length() > 0) {
                    parts.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(c);
            }
        }
        if (token.length() > 0) parts.add(token.toString());
        return parts;
    }

    private static String getJavaExecutable() {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        return System.getProperty("os.name").toLowerCase().contains("win") ? javaBin + ".exe" : javaBin;
    }

    private static void shutdownCurrentMinecraftInstance() {
        try {
            Class<?> mcClass = Class.forName("net.minecraft.client.Minecraft");
            Method getMinecraft = mcClass.getMethod("getMinecraft");
            Object minecraft = getMinecraft.invoke(null);
            Method addScheduledTask = mcClass.getMethod("addScheduledTask", Runnable.class);
            addScheduledTask.invoke(minecraft, (Runnable) () -> {
                try {
                    mcClass.getMethod("shutdown").invoke(minecraft);
                } catch (Throwable ignored) {}
            });
        } catch (Throwable ignored) {}
    }
}
