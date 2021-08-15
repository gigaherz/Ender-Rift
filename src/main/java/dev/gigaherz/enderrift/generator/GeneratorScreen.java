package dev.gigaherz.enderrift.generator;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorContainer>
{
    private static final int bar1x = 1;
    private static final int bar2x = 17;
    private static final int barWidth = 14;
    private static final int barHeight = 42;
    private final ClientFields clientFields;

    protected ResourceLocation guiTextureLocation;
    protected ResourceLocation energyTextureLocation;

    public GeneratorScreen(GeneratorContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageHeight = 165;
        guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/generator.png");
        energyTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/energy.png");
        this.clientFields = (container.fields instanceof ClientFields cf) ? cf : new ClientFields();
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

        String label;
        int generationPower = GeneratorBlockEntity.calculateGenerationPower(clientFields.heatLevel);
        if (generationPower > 0)
        {
            label = I18n.get("text.enderrift.generator.status.generating.label");
            font.draw(matrixStack, label, 8, 22, 0x404040);
            font.draw(matrixStack, String.format("%d RF/t", generationPower), 12, 32, 0x404040);
        }
        else if (clientFields.burnTimeRemaining > 0)
        {
            label = I18n.get("text.enderrift.generator.status.heating");
            font.draw(matrixStack, label, 8, 22, 0x404040);
        }
        else
        {
            label = I18n.get("text.enderrift.generator.status.idle");
            font.draw(matrixStack, label, 8, 22, 0x404040);
        }

        label = I18n.get("text.enderrift.generator.heat.label");
        font.draw(matrixStack, label, 8, 46, 0x404040);
        font.draw(matrixStack, String.format("%d C", clientFields.heatLevel), 12, 56, getHeatColor());

        String str = String.format("%d RF", clientFields.energy);
        font.draw(matrixStack, str, imageWidth - 8 - font.width(str), 64, 0x404040);

        drawBarTooltip(matrixStack, mouseX, mouseY, imageWidth - 14 - 8, 20);
    }

    private void drawBarTooltip(PoseStack matrixStack, int mx, int my, int ox, int oy)
    {
        int rx = mx - ox - leftPos;
        int ry = my - oy - topPos;

        if (rx < 0 || ry < 0 || rx > barWidth || ry > barHeight)
            return;

        List<Component> tooltip = Lists.newArrayList();
        tooltip.add(new TranslatableComponent("text.enderrift.generator.energy.label"));
        tooltip.add(new TextComponent(String.format("%d / %d RF", clientFields.energy, GeneratorBlockEntity.POWER_LIMIT)));

        // renderTooltip
        renderComponentTooltip(matrixStack, tooltip, mx - leftPos, my - topPos);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, guiTextureLocation);

        blit(matrixStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (clientFields.burnTimeRemaining > 0)
        {
            int k = getBurnLeftScaled(13);
            blit(matrixStack, leftPos + 80, topPos + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        drawEnergyBar(matrixStack, leftPos + imageWidth - 14 - 8, topPos + 20, clientFields.energy, GeneratorBlockEntity.POWER_LIMIT);
    }

    private void drawEnergyBar(PoseStack matrixStack, int x, int y, int powerLevel, int powerLimit)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, energyTextureLocation);

        int bar2height = 1 + powerLevel * (barHeight - 2) / powerLimit;
        int bar1height = barHeight - bar2height;

        blit(matrixStack, x, y, bar1x, 0, barWidth, bar1height, 32, 64);
        blit(matrixStack, x, y + bar1height, bar2x, bar1height, barWidth, bar2height, 32, 64);
    }

    private int getHeatColor()
    {
        int heatLevel = clientFields.heatLevel;
        if (heatLevel <= GeneratorBlockEntity.MIN_HEAT)
            return 0x404040;


        float p = (heatLevel - GeneratorBlockEntity.MIN_HEAT) / (float) (GeneratorBlockEntity.MAX_HEAT - GeneratorBlockEntity.MIN_HEAT);

        int r = 0xA0;
        int g = Math.round(0x40 + (0xA0 - 0x40) * (1 - p));
        int b = 0x40;

        return (r << 16) | (g << 8) | b;
    }

    private int getBurnLeftScaled(int pixels)
    {
        int i = clientFields.currentItemBurnTime;

        if (i == 0)
        {
            i = 200;
        }

        return clientFields.burnTimeRemaining * pixels / i;
    }
}