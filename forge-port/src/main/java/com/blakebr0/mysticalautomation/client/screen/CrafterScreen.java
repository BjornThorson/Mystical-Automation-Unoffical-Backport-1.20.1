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
        graphics.drawString(font, Component.literal("Progress: " + menu.getProgress()
                        + "/" + menu.getOperationTicks()), 100, 75, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, 123, 0x404040, false);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
