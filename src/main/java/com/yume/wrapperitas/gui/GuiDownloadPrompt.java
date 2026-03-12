package com.yume.wrapperitas.gui;

import com.yume.wrapperitas.check.DownloaderService;
import com.yume.wrapperitas.check.UpdateChecker;
import com.yume.wrapperitas.util.GameRestarter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;

public class GuiDownloadPrompt extends GuiScreen implements GuiYesNoCallback {

    private static final int OPEN_RELEASE_LINK_ID = 9102;

    private final GuiScreen parentScreen;
    private GuiButton downloadButton;
    private GuiButton cancelButton;
    private GuiButton restartButton;
    private GuiButton continueButton;

    private static final ResourceLocation CELERITAS_ICON = new ResourceLocation("wrapperitas", "textures/gui/icon.png");
    private int modNameX;
    private int modNameY;
    private int modNameWidth;

    public GuiDownloadPrompt(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        downloadButton = new GuiButton(0, centerX - 100, centerY + 30, 95, 20, "Download");
        cancelButton = new GuiButton(1, centerX + 5, centerY + 30, 95, 20, "Cancel");

        restartButton = new GuiButton(2, centerX - 100, centerY + 30, 95, 20, "Restart Game");
        continueButton = new GuiButton(3, centerX + 5, centerY + 30, 95, 20, "Continue");

        restartButton.visible = false;
        continueButton.visible = false;

        this.buttonList.add(downloadButton);
        this.buttonList.add(cancelButton);
        this.buttonList.add(restartButton);
        this.buttonList.add(continueButton);
    }

    private void drawDirtBox(int x, int y, int width, int height) {
        // Darkened background for the whole screen
        drawRect(0, 0, this.width, this.height, 0x66000000);

        // Gray border
        drawRect(x - 2, y - 2, x + width + 2, y + height + 2, 0xFF888888);
        // Inner black border
        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF000000);

        // Dirt repeating texture
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/options_background.png"));
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x, y + height, 0.0D).tex(0.0D, height / 32.0D).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(width / 32.0D, height / 32.0D).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(width / 32.0D, 0.0D).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
    }

    public void drawFancyProgressBar(int x, int y, int width, int height, float progress) {
        // Outline
        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF000000);
        // Background
        drawRect(x, y, x + width, y + height, 0xFF333333);
        
        // Progress Fill
        if (progress > 0) {
            int fillWidth = (int) (width * progress);
            this.drawGradientRect(x, y, x + fillWidth, y + height, 0xFF55FF55, 0xFF229922);
        }
        
        // Shine overlay on the top half of the bar
        drawRect(x, y, x + width, y + height / 2, 0x15FFFFFF);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.parentScreen != null) {
            GlStateManager.pushMatrix();
            this.parentScreen.drawScreen(-1, -1, partialTicks);
            GlStateManager.popMatrix();
            GlStateManager.clear(256);
        } else {
            this.drawDefaultBackground();
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        int boxWidth = 220;
        int boxHeight = 110;
        int boxX = centerX - boxWidth / 2;
        int boxY = centerY - boxHeight / 2;

        drawDirtBox(boxX, boxY, boxWidth, boxHeight);

        // Celeritas Icon
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        this.mc.getTextureManager().bindTexture(CELERITAS_ICON);
        int iconSize = 48;
        int iconX = boxX + 15;
        int iconY = boxY + 12; // Top aligned closer with text
        GuiScreen.drawModalRectWithCustomSizedTexture(iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        GlStateManager.disableBlend();

        int textX = iconX + iconSize + 10;
        
        boolean isOldBuild = UpdateChecker.latestUpdate != null && UpdateChecker.currentVersion != null
                && UpdateChecker.latestUpdate.version.equals(UpdateChecker.currentVersion);

        if (DownloaderService.downloaded) {
             this.drawString(this.fontRenderer, "Download Successful!", textX, boxY + 14, 0x7CFC96);
             this.drawString(this.fontRenderer, "Celeritas " + UpdateChecker.latestUpdate.version, textX, boxY + 30, 0xA8FFB8);
             this.drawString(this.fontRenderer, "Please restart game.", textX, boxY + 46, 0xC8FAD0);

            downloadButton.visible = false;
            cancelButton.visible = false;
            restartButton.visible = true;
            continueButton.visible = true;
        } else if (DownloaderService.progress > 0.0f && !DownloaderService.error) {
             this.drawString(this.fontRenderer, "Downloading...", textX, boxY + 20, 0x7FDBFF);
             this.drawString(this.fontRenderer, String.format("%.0f%%", DownloaderService.progress * 100), textX, boxY + 36, 0xB8EDFF);
            
            int barWidth = boxWidth - 30;
            int barHeight = 10;
            int barX = boxX + 15;
            int barY = boxY + 70;
            
            drawFancyProgressBar(barX, barY, barWidth, barHeight, DownloaderService.progress);
            
            downloadButton.visible = false;
            cancelButton.visible = false;
        } else if (DownloaderService.error) {
             this.drawString(this.fontRenderer, "Download Failed.", textX, boxY + 20, 0xFF6B6B);
             this.drawString(this.fontRenderer, "Check logs.", textX, boxY + 36, 0xFFC1C1);
            downloadButton.visible = false;
            cancelButton.visible = true;
        } else {
             String modName = "\u00A7nCeleritas\u00A7r";
             modNameWidth = this.fontRenderer.getStringWidth("Celeritas");
             modNameX = textX;
             modNameY = boxY + 14;
             boolean hoverModName = isHoveringModName(mouseX, mouseY);
             int linkColor = hoverModName ? 0x5555FF : 0xFFFFFF;
             this.drawString(this.fontRenderer, modName, modNameX, modNameY, linkColor);

             drawRainbowString("Update Available", textX, boxY + 30);

             String versionLine;
             if (UpdateChecker.currentVersion == null || "None".equals(UpdateChecker.currentVersion)) {
                 versionLine = "Version: none -> " + UpdateChecker.latestUpdate.version;
             } else {
                 versionLine = "Version: " + UpdateChecker.currentVersion + " -> " + UpdateChecker.latestUpdate.version;
             }
             this.drawString(this.fontRenderer, versionLine, textX, boxY + 46, 0x7FDBFF);
             this.drawString(this.fontRenderer, "Reason: " + buildReasonText(isOldBuild), textX, boxY + 58, 0xFFD166);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) { // Download
            downloadButton.enabled = false;
            cancelButton.enabled = false;
            DownloaderService.download(UpdateChecker.latestUpdate, () -> {});
        } else if (button.id == 1) { // Cancel
            this.mc.displayGuiScreen(parentScreen);
        } else if (button.id == 2) { // Restart Game
            GameRestarter.restart();
        } else if (button.id == 3) { // Continue Playing
            this.mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && UpdateChecker.latestUpdate != null && isHoveringModName(mouseX, mouseY)) {
            String link = getReleaseLink();
            if (link != null && !link.isEmpty()) {
                this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, link, OPEN_RELEASE_LINK_ID, false));
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (id == OPEN_RELEASE_LINK_ID && result) {
            String link = getReleaseLink();
            if (link != null && !link.isEmpty()) {
                try {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().browse(new URI(link));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        this.mc.displayGuiScreen(this);
    }

    private boolean isHoveringModName(int mouseX, int mouseY) {
        return mouseX >= modNameX && mouseX <= modNameX + modNameWidth
                && mouseY >= modNameY && mouseY <= modNameY + this.fontRenderer.FONT_HEIGHT;
    }

    private String getReleaseLink() {
        if (UpdateChecker.latestReleasePageUrl != null && !UpdateChecker.latestReleasePageUrl.isEmpty()) {
            return UpdateChecker.latestReleasePageUrl;
        }
        if (UpdateChecker.latestUpdate != null) {
            return UpdateChecker.latestUpdate.downloadUrl;
        }
        return null;
    }

    private String buildReasonText(boolean isOldBuild) {
        if (UpdateChecker.latestReason == null) {
            return isOldBuild ? "outdated" : "update";
        }

        switch (UpdateChecker.latestReason) {
            case NONE:
                return "not installed";
            case OUTDATED:
                return "outdated";
            case UPDATE:
            default:
                return "update";
        }
    }

    private void drawRainbowString(String text, int x, int y) {
        float baseHue = (System.currentTimeMillis() % 5000L) / 5000.0f;
        int cursorX = x;
        for (int i = 0; i < text.length(); i++) {
            String ch = text.substring(i, i + 1);
            float hue = (baseHue + (i * 0.045f)) % 1.0f;
            int color = Color.HSBtoRGB(hue, 0.72f, 1.0f);
            this.drawString(this.fontRenderer, ch, cursorX, y, color);
            cursorX += this.fontRenderer.getStringWidth(ch);
        }
    }
}
