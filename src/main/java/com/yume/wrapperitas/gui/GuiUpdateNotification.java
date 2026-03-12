package com.yume.wrapperitas.gui;

import com.yume.wrapperitas.check.UpdateChecker;
import com.yume.wrapperitas.config.WrapperConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiUpdateNotification {

    public static final String PREFIX_TEXT = "You have a new update for Celeritas! ";
    public static final String CLICK_TEXT = "[\u00A7aClick here to install it\u00A7r]";
    public static final String FULL_TEXT = PREFIX_TEXT + CLICK_TEXT;

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (UpdateChecker.latestUpdate == null) return;

        GuiScreen gui = event.getGui();
        boolean isMainMenu = gui instanceof GuiMainMenu;
        boolean isModList = gui.getClass().getName().contains("GuiModList");

        if (WrapperConfig.notificationLocation == WrapperConfig.NotificationLocation.NONE) return;

        if ((isMainMenu && WrapperConfig.notificationLocation == WrapperConfig.NotificationLocation.MAIN_MENU) ||
            (isModList && WrapperConfig.notificationLocation == WrapperConfig.NotificationLocation.MOD_LIST)) {
            renderNotification(gui, event.getMouseX(), event.getMouseY(), isModList);
        }
    }

    private void renderNotification(GuiScreen gui, int mouseX, int mouseY, boolean isModList) {
         int fullWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(FULL_TEXT);
         
         int x = gui.width / 2 - fullWidth / 2;
         int y = isModList ? gui.height - 55 : 20;
         
         int prefixWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(PREFIX_TEXT);
         boolean hoverClick = mouseX >= x + prefixWidth && mouseX <= x + fullWidth && mouseY >= y && mouseY <= y + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

         gui.drawString(Minecraft.getMinecraft().fontRenderer, PREFIX_TEXT, x, y, 0xFFFFFF);
         gui.drawString(Minecraft.getMinecraft().fontRenderer, CLICK_TEXT, x + prefixWidth, y, hoverClick ? 0xFFFFFF : 0xAAAAAA);
    }

    private boolean isHoveringClickText(GuiScreen gui, int mouseX, int mouseY, boolean isModList) {
         int fullWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(FULL_TEXT);
         int x = gui.width / 2 - fullWidth / 2;
         int y = isModList ? gui.height - 55 : 20;

         int prefixWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(PREFIX_TEXT);
         
         return mouseX >= x + prefixWidth && mouseX <= x + fullWidth && mouseY >= y && mouseY <= y + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }

    @SubscribeEvent
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (UpdateChecker.latestUpdate == null) return;

        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if (gui == null) return;

        boolean isMainMenu = gui instanceof GuiMainMenu;
        boolean isModList = gui.getClass().getName().contains("GuiModList");

        if (WrapperConfig.notificationLocation == WrapperConfig.NotificationLocation.NONE) return;

        if ((isMainMenu && WrapperConfig.notificationLocation == WrapperConfig.NotificationLocation.MAIN_MENU) ||
            (isModList && WrapperConfig.notificationLocation == WrapperConfig.NotificationLocation.MOD_LIST)) {

            int mouseX = org.lwjgl.input.Mouse.getEventX() * gui.width / Minecraft.getMinecraft().displayWidth;
            int mouseY = gui.height - org.lwjgl.input.Mouse.getEventY() * gui.height / Minecraft.getMinecraft().displayHeight - 1;

            if (org.lwjgl.input.Mouse.getEventButton() == 0 && org.lwjgl.input.Mouse.getEventButtonState() && isHoveringClickText(gui, mouseX, mouseY, isModList)) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiDownloadPrompt(gui));
            }
        }
    }
}
