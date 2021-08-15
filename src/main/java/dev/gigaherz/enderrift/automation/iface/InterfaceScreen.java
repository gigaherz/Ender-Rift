package dev.gigaherz.enderrift.automation.iface;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class InterfaceScreen extends AbstractContainerScreen<InterfaceContainer>
{
    private static final ResourceLocation guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/interface.png");
    private static final String textFilters = "text.enderrift.interface.filters";

    public InterfaceScreen(InterfaceContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageHeight = 176;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY)
    {
        super.renderLabels(matrixStack, mouseX, mouseY);
        font.draw(matrixStack, I18n.get(textFilters), 8, 20, 0x404040);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, guiTextureLocation);

        blit(matrixStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}