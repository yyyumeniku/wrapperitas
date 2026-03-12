package com.yume.wrapperitas.check;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    public static final String GITHUB_API_URL = "https://api.github.com/repos/kappa-maintainer/Celeritas-auto-build/releases";
    // this one will check for mod patterns to download
    public static final Pattern FILE_PATTERN = Pattern.compile("celeritas(?:-forge)?(?:-mc12\\.2)?-([a-zA-Z0-9\\.]+)(?:-dev)?\\.jar");

    public static CeleritasUpdateInfo latestUpdate = null;
    public static String currentVersion = "None";
    public static File currentJarFile = null;
    public static UpdateReason latestReason = null;
    public static String latestReleasePageUrl = null;

    public enum UpdateReason {
        NONE,
        OUTDATED,
        UPDATE
    }

    public static void findLocalVersion(File modsDir) {
        currentVersion = "None";
        currentJarFile = null;

        if (!modsDir.exists() || !modsDir.isDirectory()) return;

        File[] files = modsDir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".jar")) {
                Matcher matcher = FILE_PATTERN.matcher(f.getName());
                if (matcher.matches()) {
                    currentVersion = matcher.group(1);
                    currentJarFile = f;
                    System.out.println("[Wrapperitas] Found local Celeritas version: " + currentVersion);
                    break;
                }
            }
        }
    }

    public static void checkOnlineSync(boolean doSyncDownload) {
        findLocalVersion(new File("mods"));

        try {
            System.out.println("[Wrapperitas] Checking for Celeritas updates synchronously...");
            HttpURLConnection conn = (HttpURLConnection) new URL(GITHUB_API_URL).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (conn.getResponseCode() == 200) {
                JsonArray jsonArray = new JsonParser().parse(new InputStreamReader(conn.getInputStream())).getAsJsonArray();

                for (JsonElement releaseElement : jsonArray) {
                    JsonObject release = releaseElement.getAsJsonObject();
                    String publishDateStr = release.get("published_at").getAsString();
                    long publishTimestamp = Instant.parse(publishDateStr).toEpochMilli();
                    String releasePageUrl = release.has("html_url") ? release.get("html_url").getAsString() : null;

                    JsonArray assets = release.getAsJsonArray("assets");

                    for (JsonElement assetElement : assets) {
                        JsonObject asset = assetElement.getAsJsonObject();
                        String name = asset.get("name").getAsString();

                        Matcher matcher = FILE_PATTERN.matcher(name);
                        if (matcher.matches()) {
                            String version = matcher.group(1);
                            String downloadUrl = asset.get("browser_download_url").getAsString();
                            long size = asset.get("size").getAsLong();

                            boolean shouldUpdate;
                            UpdateReason reason;
                            
                            if (currentJarFile == null || currentVersion == null || currentVersion.equals("None")) {
                                shouldUpdate = true;
                                reason = UpdateReason.NONE;
                            } else if (!version.equals(currentVersion)) {
                                shouldUpdate = true;
                                reason = UpdateReason.UPDATE;
                            } else {
                                // Same version name, check if github published date is strictly newer than local file.
                                if (publishTimestamp > currentJarFile.lastModified() + 10000) { // Add 10s buffer to avoid loop
                                    shouldUpdate = true;
                                    reason = UpdateReason.OUTDATED;
                                } else {
                                    shouldUpdate = false;
                                    reason = null;
                                }
                            }

                            if (shouldUpdate) {
                                latestUpdate = new CeleritasUpdateInfo(version, downloadUrl, size);
                                latestReason = reason;
                                latestReleasePageUrl = releasePageUrl != null ? releasePageUrl : downloadUrl;
                                System.out.println("[Wrapperitas] New Celeritas update found: " + version);

                                // If sync mode triggers, we do AWT popup
                                System.out.println("[Wrapperitas] Downloading Update Sync...");
                                if (doSyncDownload) { DownloaderService.downloadSyncWithUI(latestUpdate); }
                                return; // Fetched the latest
                            } else {
                                latestUpdate = null;
                                latestReason = null;
                                latestReleasePageUrl = null;
                                System.out.println("[Wrapperitas] Celeritas is up to date.");
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Wrapperitas] Failed to check for Celeritas updates");
            e.printStackTrace();
        }
    }

    public static void checkOnline() {
        new Thread(() -> {
            checkOnlineSync(false); // Only check, NO sync download
        }, "CeleritasUpdateCheck").start();
    }

    public static class CeleritasUpdateInfo {
        public final String version;
        public final String downloadUrl;
        public final long sizeBytes;

        public CeleritasUpdateInfo(String version, String downloadUrl, long sizeBytes) {
            this.version = version;
            this.downloadUrl = downloadUrl;
            this.sizeBytes = sizeBytes;
        }

        public String getReadableSize() {
            return String.format("%.2f MB", sizeBytes / 1048576.0);
        }
    }
}
