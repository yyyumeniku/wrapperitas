package com.yume.wrapperitas.config;

import com.yume.wrapperitas.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
public class WrapperConfig {

    @Config.Comment("Where to show the update notification. Options: MAIN_MENU, MOD_LIST, NONE")
    public static NotificationLocation notificationLocation = NotificationLocation.MAIN_MENU;

    @Config.Comment("If true, the update will download automatically in the background without asking (Requires restart to apply)")
    public static boolean autoDownload = false;

    public enum NotificationLocation {
        MAIN_MENU,
        MOD_LIST,
        NONE
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
