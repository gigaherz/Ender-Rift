package dev.gigaherz.enderrift.automation.iface;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InterfaceScreen extends AbstractContainerScreen<InterfaceContainer>
{
    private static final ResourceLocation guiTextureLocation = EnderRiftMod.location("textures/gui/interface.png");
    private static final String textFilters = "text.enderrift.interface.filters";

    public InterfaceScreen(InterfaceContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageHeight = 176;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, I18n.get(textFilters), 8, 20, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.blit(RenderType::guiTextured, guiTextureLocation, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }
}