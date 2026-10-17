package com.blakebr0.mysticalautomation.client.screen;

import com.blakebr0.mysticalautomation.container.InfuserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class InfuserScreen extends AbstractContainerScreen<InfuserMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("mysticalautomation", "textures/gui/infuser.png");
    private static final String[] TARGETS = {
            "", "Prudentium", "Tertium", "Imperium", "Supremium", "Insanium"
    };

    public InfuserScreen(InfuserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 217;
        imageHeight = 194;
        inventoryLabelY = 100;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("<"), button -> changeTier(-1))
                .bounds(leftPos + 106, topPos + 80, 18, 18).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> changeTier(1))
                .bounds(leftPos + 186, topPos + 80, 18, 18).build());
    }

    private void changeTier(int step) {
        int next = menu.getTarget() + step;
        if (next < 1) next = 5;
        if (next > 5) next = 1;
        if (minecraft != null && minecraft.gameMode != null)
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, next);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 29, 6, 0x404040, false);
        graphics.drawString(font, Component.literal("FE: " + menu.getEnergy() + "/" + menu.getMaximumEnergy()),
                24, 15, 0x404040, false);
        graphics.drawString(font, Component.literal("Target: " + TARGETS[Math.max(1, Math.min(5, menu.getTarget()))]),
                125, 86, 0x404040, false);
        graphics.drawString(font, Component.literal("Progress: " + menu.getProgress() + "/" + menu.getOperationTicks()),
                102, 65, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 29, 100, 0x404040, false);
        for (int i = 0; i < 6; i++) {
            int count = menu.getEssenceCount(i);
            if (count <= 1) continue;
            graphics.pose().pushPose();
            graphics.pose().translate(103 + i * 18, 45, 220);
            graphics.pose().scale(0.70f, 0.70f, 1);
            String shown = Integer.toString(count);
            graphics.drawString(font, shown,
                    Math.max(0, (int)(18 / 0.70f) - font.width(shown)),
                    0, 0xFFFFFF, true);
            graphics.pose().popPose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
