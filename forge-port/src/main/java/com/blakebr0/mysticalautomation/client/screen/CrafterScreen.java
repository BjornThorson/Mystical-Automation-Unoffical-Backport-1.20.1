package com.blakebr0.mysticalautomation.client.screen;

import com.blakebr0.mysticalautomation.container.CrafterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class CrafterScreen extends AbstractContainerScreen<CrafterMenu> {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("mysticalautomation", "textures/gui/crafter.png");

    public CrafterScreen(CrafterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 217;
        inventoryLabelY = 123;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0x404040, false);
        graphics.drawString(font, Component.literal(menu.getEnergy() + "/" + menu.getMaximumEnergy() + " FE"),
                24, 16, 0x404040, false);
        // The recipe blueprint occupies x=56..108, y=30..82. Keep the
        // progress text in the clear strip beneath it and above ingredient stock.
        drawFittedProgress(graphics, "Progress: " + menu.getProgress()
                + "/" + menu.getOperationTicks(), 88, 87, 81);
        graphics.drawString(font, playerInventoryTitle, 8, 123, 0x404040, false);
    }

    /** Center in a bounded strip; shrink long readouts before they reach GUI slots. */
    private void drawFittedProgress(GuiGraphics graphics, String label, int x, int y, int width) {
        int textWidth = font.width(label);
        if (textWidth <= 0) return;
        float scale = Math.min(1.0f, (float) width / textWidth);
        graphics.pose().pushPose();
        graphics.pose().translate(x + width / 2.0f, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(font, Component.literal(label), -textWidth / 2, 0, 0x404040, false);
        graphics.pose().popPose();
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
