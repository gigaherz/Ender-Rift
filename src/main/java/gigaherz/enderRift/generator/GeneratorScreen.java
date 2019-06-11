package gigaherz.enderRift.generator;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class GeneratorScreen extends ContainerScreen<GeneratorContainer>
{
    private static final int bar1x = 1;
    private static final int bar2x = 17;
    private static final int barWidth = 14;
    private static final int barHeight = 42;

    protected ResourceLocation guiTextureLocation;
    protected ResourceLocation energyTextureLocation;

    public GeneratorScreen(GeneratorContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
        ySize = 165;
        guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/generator.png");
        energyTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/energy.png");
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        String name = I18n.format(container.tile.getName());
        minecraft.fontRenderer.drawString(name, (xSize - minecraft.fontRenderer.getStringWidth(name)) / 2, 6, 0x404040);
        minecraft.fontRenderer.drawString(playerInventory.getName().getFormattedText(), 8, ySize - 96 + 2, 0x404040);

        String label;
        if (container.tile.getGenerationPower() > 0)
        {
            label = I18n.format("text." + EnderRiftMod.MODID + ".generator.status.generating.label");
            minecraft.fontRenderer.drawString(label, 8, 22, 0x404040);
            minecraft.fontRenderer.drawString(String.format("%d RF/t", container.tile.getGenerationPower()), 12, 32, 0x404040);
        }
        else if (container.tile.isBurning())
        {
            label = I18n.format("text." + EnderRiftMod.MODID + ".generator.status.heating");
            minecraft.fontRenderer.drawString(label, 8, 22, 0x404040);
        }
        else
        {
            label = I18n.format("text." + EnderRiftMod.MODID + ".generator.status.idle");
            minecraft.fontRenderer.drawString(label, 8, 22, 0x404040);
        }

        label = I18n.format("text." + EnderRiftMod.MODID + ".generator.heat.label");
        minecraft.fontRenderer.drawString(label, 8, 46, 0x404040);
        minecraft.fontRenderer.drawString(String.format("%d C", container.tile.getHeatValue()), 12, 56, getHeatColor());

        String str = String.format("%d RF", container.tile.getContainedEnergy());
        minecraft.fontRenderer.drawString(str, xSize - 8 - minecraft.fontRenderer.getStringWidth(str), 64, 0x404040);

        drawBarTooltip(i, j, xSize - 14 - 8, 20);
    }

    private void drawBarTooltip(int mx, int my, int ox, int oy)
    {
        int rx = mx - ox - guiLeft;
        int ry = my - oy - guiTop;

        if (rx < 0 || ry < 0 || rx > barWidth || ry > barHeight)
            return;

        List<String> tooltip = Lists.newArrayList();
        tooltip.add(I18n.format("text." + EnderRiftMod.MODID + ".generator.energy.label"));
        tooltip.add(String.format("%d / %d RF", container.tile.getContainedEnergy(), GeneratorTileEntity.POWER_LIMIT));

        renderTooltip(tooltip, mx - guiLeft, my - guiTop);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);

        minecraft.getTextureManager().bindTexture(guiTextureLocation);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (container.tile.isBurning())
        {
            int k = getBurnLeftScaled(13);
            blit(guiLeft + 80, guiTop + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        drawEnergyBar(guiLeft + xSize - 14 - 8, guiTop + 20, container.tile.getContainedEnergy(), GeneratorTileEntity.POWER_LIMIT);
    }

    private void drawEnergyBar(int x, int y, int powerLevel, int powerLimit)
    {
        int bar2height = 1 + powerLevel * (barHeight - 2) / powerLimit;
        int bar1height = barHeight - bar2height;

        minecraft.getTextureManager().bindTexture(energyTextureLocation);
        blit(x, y, bar1x, 0, barWidth, bar1height, 32, 64);
        blit(x, y + bar1height, bar2x, bar1height, barWidth, bar2height, 32, 64);
    }

    private int getHeatColor()
    {
        int heatLevel = container.tile.getHeatValue();
        if (heatLevel <= GeneratorTileEntity.MIN_HEAT)
            return 0x404040;


        float p = (heatLevel - GeneratorTileEntity.MIN_HEAT) / (float) (GeneratorTileEntity.MAX_HEAT - GeneratorTileEntity.MIN_HEAT);

        int r = 0xA0;
        int g = Math.round(0x40 + (0xA0 - 0x40) * (1 - p));
        int b = 0x40;

        return (r << 16) | (g << 8) | b;
    }

    private int getBurnLeftScaled(int pixels)
    {
        int i = container.tile.getCurrentItemBurnTime();

        if (i == 0)
        {
            i = 200;
        }

        return container.tile.getBurnTimeRemaining() * pixels / i;
    }
}