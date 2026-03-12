package com.yume.wrapperitas.core;

import com.yume.wrapperitas.check.UpdateChecker;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Wrapperitas Core")
@IFMLLoadingPlugin.SortingIndex(1001)
public class WrapperitasLoadingPlugin implements IFMLLoadingPlugin {

    public WrapperitasLoadingPlugin() {
        System.out.println("[Wrapperitas] Executing Early CoreMod check...");
        checkAutoUpdate();
    }

    private void checkAutoUpdate() {
        File cfg = new File("config/wrapperitas.cfg");
        boolean autoDownloadEnabled = false;

        if (cfg.exists()) {
            try {
                List<String> lines = Files.readAllLines(cfg.toPath());
                for (String line : lines) {
                    if (line.contains("B:autoDownload=true") || line.contains("B:\"autoDownload\"=true")) {
                        autoDownloadEnabled = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (autoDownloadEnabled) {
            System.out.println("[Wrapperitas] Auto Download is enabled! Checking synchronously before game start...");
            UpdateChecker.checkOnlineSync(true);
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
