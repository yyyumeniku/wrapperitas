package com.yume.wrapperitas.gui;

import com.yume.wrapperitas.check.DownloaderService;
import com.yume.wrapperitas.check.UpdateChecker;
import com.yume.wrapperitas.util.GameRestarter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiDownloadPrompt extends GuiScreen {

    private final GuiScreen parentScreen;
    private GuiButton downloadButton;
    private GuiButton cancelButton;
    private GuiButton restartButton;
    private GuiButton continueButton;

    private static final ResourceLocation CELERITAS_ICON = new ResourceLocation("wrapperitas", "textures/gui/icon.png");

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
             this.drawString(this.fontRenderer, "Download Successful!", textX, boxY + 14, 0x55FF55);
             this.drawString(this.fontRenderer, "Celeritas " + UpdateChecker.latestUpdate.version, textX, boxY + 30, 0x55FF55);
             this.drawString(this.fontRenderer, "Please restart game.", textX, boxY + 46, 0x55FF55);

            downloadButton.visible = false;
            cancelButton.visible = false;
            restartButton.visible = true;
            continueButton.visible = true;
        } else if (DownloaderService.progress > 0.0f && !DownloaderService.error) {
             this.drawString(this.fontRenderer, "Downloading...", textX, boxY + 20, 0x55FF55);
             this.drawString(this.fontRenderer, String.format("%.0f%%", DownloaderService.progress * 100), textX, boxY + 36, 0x55FF55);
            
            int barWidth = boxWidth - 30;
            int barHeight = 10;
            int barX = boxX + 15;
            int barY = boxY + 70;
            
            drawFancyProgressBar(barX, barY, barWidth, barHeight, DownloaderService.progress);
            
            downloadButton.visible = false;
            cancelButton.visible = false;
        } else if (DownloaderService.error) {
             this.drawString(this.fontRenderer, "Download Failed.", textX, boxY + 20, 0xFF5555);
             this.drawString(this.fontRenderer, "Check logs.", textX, boxY + 36, 0xFF5555);
            downloadButton.visible = false;
            cancelButton.visible = true;
        } else {
             this.drawString(this.fontRenderer, "Update Available", textX, boxY + 14, 0x55FF55);
             this.drawString(this.fontRenderer, UpdateChecker.latestUpdate.version + " (" + UpdateChecker.latestUpdate.getReadableSize() + ")", textX, boxY + 30, 0x55FF55);
             if (isOldBuild) {
                 this.drawString(this.fontRenderer, "Reason: old build.", textX, boxY + 46, 0x55FF55);
             } else {
                 this.drawString(this.fontRenderer, "Reason: outdated.", textX, boxY + 46, 0x55FF55);
             }
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
}
