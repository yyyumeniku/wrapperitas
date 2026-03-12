package com.yume.wrapperitas.gui;

import com.yume.wrapperitas.Tags;
import com.yume.wrapperitas.check.UpdateChecker;
import com.yume.wrapperitas.config.WrapperConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class GuiWrapperSetting extends GuiConfig {
    public GuiWrapperSetting(GuiScreen parent) {
        super(parent,
              ConfigElement.from(WrapperConfig.class).getChildElements(),
              Tags.MOD_ID,
              false,
              false,
              "Wrapperitas Menu");
    }

    @Override
    public void initGui() {
        super.initGui();
        int doneWidth = Math.max(mc.fontRenderer.getStringWidth(net.minecraft.client.resources.I18n.format("gui.done")) + 20, 100);
        int resetWidth = mc.fontRenderer.getStringWidth(" " + net.minecraft.client.resources.I18n.format("fml.configgui.tooltip.resetToDefault")) + mc.fontRenderer.getStringWidth(" \u21BA") * 2 + 20;
        int undoWidth = mc.fontRenderer.getStringWidth(" " + net.minecraft.client.resources.I18n.format("fml.configgui.tooltip.undoChanges")) + mc.fontRenderer.getStringWidth(" \u21B6") * 2 + 20;
        int buttonWidthHalf = (doneWidth + 5 + undoWidth + 5 + resetWidth) / 2;

        GuiButton updateBtn = new GuiButton(3000, this.width / 2 - 100, this.height - 55, 200, 20, "Open Update Menu");
        if (UpdateChecker.latestUpdate == null) {
            updateBtn.enabled = false;
            updateBtn.displayString = "No Update Available";
        }
        this.buttonList.add(updateBtn);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 3000 && UpdateChecker.latestUpdate != null) {
            this.mc.displayGuiScreen(new GuiDownloadPrompt(this));
        } else {
            super.actionPerformed(button);
        }
    }
}