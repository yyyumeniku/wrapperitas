package com.yume.wrapperitas;

import com.yume.wrapperitas.check.UpdateChecker;
import com.yume.wrapperitas.gui.GuiUpdateNotification;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", guiFactory = "com.yume.wrapperitas.gui.WrapperConfigGuiFactory"
)
public class WrapperitasMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.Instance(Tags.MOD_ID)
    public static WrapperitasMod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Wrapperitas Pre-Initialization starting.");
        // We will trigger the background HTTP check for Celeritas here
        UpdateChecker.checkOnline();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Wrapperitas Initialization.");
        MinecraftForge.EVENT_BUS.register(new GuiUpdateNotification());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("Wrapperitas Post-Initialization.");
    }
}
