package com.yume.wrapperitas.util;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class GameRestarter {

    public static void restart() {
        System.out.println("[Wrapperitas] Attempting to restart the game...");

        try {
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                javaBin += ".exe";
            }

            List<String> commandArgs = new ArrayList<>();
            commandArgs.add(javaBin);

            // Inherit JVM arguments (memory limits, native paths)
            commandArgs.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());

            // Add classpath
            commandArgs.add("-cp");
            commandArgs.add(System.getProperty("java.class.path"));

            // We need to bypass PrismLauncher EntryPoint, use the actual game launch target
            commandArgs.add("net.minecraft.launchwrapper.Launch");

            // We must inject all standard arguments
            String[] rawArgs = (String[]) net.minecraft.launchwrapper.Launch.blackboard.get("launchArgs");
            if (rawArgs != null) {
                for (String arg : rawArgs) {
                    commandArgs.add(arg);
                }
            } else {
                commandArgs.add("--version");
                commandArgs.add("1.12.2");
                commandArgs.add("--gameDir");
                commandArgs.add(new File(".").getAbsolutePath());
                commandArgs.add("--assetsDir");
                commandArgs.add(new File("assets").getAbsolutePath());
                commandArgs.add("--assetIndex");
                commandArgs.add("1.12");
                commandArgs.add("--accessToken");
                commandArgs.add(Minecraft.getMinecraft().getSession().getToken());
                commandArgs.add("--uuid");
                commandArgs.add(Minecraft.getMinecraft().getSession().getPlayerID());
                commandArgs.add("--username");
                commandArgs.add(Minecraft.getMinecraft().getSession().getUsername());
                commandArgs.add("--userType");
                commandArgs.add("mojang");
                commandArgs.add("--versionType");
                commandArgs.add("Forge");
                commandArgs.add("--tweakClass");
                commandArgs.add("net.minecraftforge.fml.common.launcher.FMLTweaker");
            }

            // Execute completely detached to avoid Prism's Bad file descriptor stream lock.
            ProcessBuilder builder = new ProcessBuilder(commandArgs);
            File devNull = new File(System.getProperty("os.name").toLowerCase().contains("win") ? "NUL" : "/dev/null");
            builder.redirectOutput(devNull);
            builder.redirectError(devNull);
            builder.directory(new File("."));

            builder.start();

            // Force quit the current instance completely so the lock is released safely
            Minecraft.getMinecraft().shutdown();
            Runtime.getRuntime().halt(0);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[Wrapperitas] Critical error while attempting to restart.");
        }
    }
}
