package dev.gigaherz.enderrift.automation.iface;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class InterfaceScreen extends AbstractContainerScreen<InterfaceContainer>
{
    private static final Identifier guiTextureLocation = EnderRiftMod.location("textures/gui/interface.png");
    private static final String textFilters = "text.enderrift.interface.filters";

    public InterfaceScreen(InterfaceContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title, 176, 176);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.extractBackground(graphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
    {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, I18n.get(textFilters), 8, 20, 0x404040, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, guiTextureLocation, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }
}