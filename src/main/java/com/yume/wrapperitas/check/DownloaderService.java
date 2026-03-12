package com.yume.wrapperitas.check;

import com.yume.wrapperitas.util.GameRestarter;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloaderService {
    public static float progress = 0.0f;
    public static boolean downloaded = false;
    public static boolean error = false;

    public static void downloadSyncWithUI(UpdateChecker.CeleritasUpdateInfo info) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JDialog dialog = new JDialog((Frame) null, "Wrapperitas Updating Celeritas", true);
        dialog.setSize(450, 160);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout(15, 15));

        // Attempt to add the icon
        try {
            URL iconURL = DownloaderService.class.getClassLoader().getResource("assets/wrapperitas/textures/gui/icon.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                dialog.setIconImage(icon.getImage());
                
                JLabel iconLabel = new JLabel(new ImageIcon(img));
                iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));
                dialog.add(iconLabel, BorderLayout.WEST);
            }
        } catch (Exception ignored) {}

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        boolean isOldBuild = UpdateChecker.currentVersion != null && info.version.equals(UpdateChecker.currentVersion);
        String reasonStr = isOldBuild ? "Reason: old build" : "Reason: outdated";

        JLabel statusLabel = new JLabel("Status: Downloading...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoLabel = new JLabel("Update Available: " + info.version + " (" + info.getReadableSize() + ")");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel reasonLabel = new JLabel(reasonStr);
        reasonLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        reasonLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textPanel.add(statusLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(infoLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(reasonLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 25));
        progressBar.setMaximumSize(new Dimension(300, 25));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(progressBar);
        
        dialog.add(textPanel, BorderLayout.CENTER);

        // Run UI in standard thread while download executes bg
        new Thread(() -> {
            try {
                System.out.println("Starting sync download for Celeritas update " + info.version + " from " + info.downloadUrl);
                URL url = new URL(info.downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                long fileSize = conn.getContentLengthLong();

                File tempFile = new File("mods", "celeritas-forge-mc12.2-" + info.version + "-dev.jar.tmp");
                try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                     FileOutputStream out = new FileOutputStream(tempFile)) {

                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    long totalRead = 0;

                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        out.write(dataBuffer, 0, bytesRead);
                        totalRead += bytesRead;
                        progress = (float) totalRead / fileSize;
                        
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue((int) (progress * 100));
                        });
                    }
                }

                File finalFile = new File("mods", "celeritas-forge-mc12.2-" + info.version + "-dev.jar");
                if (UpdateChecker.currentJarFile != null && UpdateChecker.currentJarFile.exists()) {
                    boolean deleted = UpdateChecker.currentJarFile.delete();
                    if (!deleted) {
                        UpdateChecker.currentJarFile.renameTo(new File(UpdateChecker.currentJarFile.getAbsolutePath() + ".old"));
                    }
                }

                tempFile.renameTo(finalFile);

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Status: Restarting...");
                });
                
                Thread.sleep(1500); 
                
                // CRUCIAL FIX: Relaunch via GameRestarter instead of just exiting with 87!
                GameRestarter.restart();
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dialog.dispose();
            }
        }, "CeleritasAutoSync").start();
        
        // This blocks the FML core thread until the dialog is disposed!
        dialog.setVisible(true);
    }

    public static void download(UpdateChecker.CeleritasUpdateInfo info, Runnable onComplete) {
        new Thread(() -> {
            try {
                System.out.println("Starting download for Celeritas update " + info.version + " from " + info.downloadUrl);
                URL url = new URL(info.downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                long fileSize = conn.getContentLengthLong();

                File tempFile = new File("mods", "celeritas-forge-mc12.2-" + info.version + "-dev.jar.tmp");
                try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                     FileOutputStream out = new FileOutputStream(tempFile)) {

                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    long totalRead = 0;

                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        out.write(dataBuffer, 0, bytesRead);
                        totalRead += bytesRead;
                        progress = (float) totalRead / fileSize;
                    }
                }

                File finalFile = new File("mods", "celeritas-forge-mc12.2-" + info.version + "-dev.jar");
                // Check and replace logic
                if (UpdateChecker.currentJarFile != null && UpdateChecker.currentJarFile.exists()) {
                    boolean deleted = UpdateChecker.currentJarFile.delete();
                    if (!deleted) {
                        System.out.println("Could not delete old jar. Ensure it is closed. Renaming it internally...");
                        UpdateChecker.currentJarFile.renameTo(new File(UpdateChecker.currentJarFile.getAbsolutePath() + ".old"));
                    }
                }

                tempFile.renameTo(finalFile);

                downloaded = true;
                System.out.println("Successfully installed Celeritas update " + info.version);
                if (onComplete != null) {
                    onComplete.run();
                }

            } catch (Exception e) {
                error = true;
                System.err.println("Failed to download Celeritas update"); 
                e.printStackTrace();
            }
        }, "CeleritasUpdateDownload").start();
    }
}
