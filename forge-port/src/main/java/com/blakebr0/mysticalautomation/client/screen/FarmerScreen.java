package com.blakebr0.mysticalautomation.client.screen;

import com.blakebr0.mysticalautomation.container.FarmerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Original 176x194 Farmer texture, inventory layout and stage indicator. */
public final class FarmerScreen extends AbstractContainerScreen<FarmerMenu> {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("mysticalautomation", "textures/gui/farmer.png");

    public FarmerScreen(FarmerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 194;
        inventoryLabelY = 100;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int maximum = menu.getMaximumStageProgress();
        int filled = maximum <= 0 ? 0 :
                Math.max(0, Math.min(16, (int)((long)menu.getStageProgress() * 16 / maximum)));
        // Original FarmerStageProgressWidget's two atlas sprites.
        graphics.blit(BACKGROUND, leftPos + 79, topPos + 30, 177, 0, 2, 16);
        if (filled > 0)
            graphics.blit(BACKGROUND, leftPos + 79, topPos + 46 - filled,
                    179, 16 - filled, 2, filled);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        drawFitted(graphics, menu.getEnergy() + "/" + menu.getMaximumEnergy() + " FE", 22, 18, 55);
        drawFitted(graphics, "Stage " + menu.getStages(), 86, 18, 59);
        drawFitted(graphics, "Progress: " + menu.getProgress() + "/" + menu.getOperationTicks(), 81, 89, 91);
        graphics.drawString(font, playerInventoryTitle, 8, 100, 0x404040, false);
    }

    private void drawFitted(GuiGraphics graphics, String label, int x, int y, int width) {
        int length = font.width(label);
        if (length <= 0) return;
        float scale = Math.min(1.0f, (float)width / length);
        graphics.pose().pushPose();
        graphics.pose().translate(x + width / 2.0f, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, Component.literal(label), -length / 2, 0, 0x404040, false);
        graphics.pose().popPose();
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (mouseX >= leftPos + 79 && mouseX < leftPos + 81
                && mouseY >= topPos + 30 && mouseY < topPos + 46
                && menu.getStages() > 0) {
            int stage = menu.getMaximumStageProgress() <= 0 ? 0 :
                    menu.getStageProgress() * menu.getStages() / menu.getMaximumStageProgress();
            graphics.renderTooltip(font, Component.literal("Stage " + stage + "/"
                    + menu.getStages()), mouseX, mouseY);
        }
    }
}
