package gigaherz.enderRift.generator;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GuiGenerator extends GuiContainer
{
    private static final int bar1x = 1;
    private static final int bar2x = 17;
    private static final int barWidth = 14;
    private static final int barHeight = 42;

    protected ResourceLocation guiTextureLocation;
    protected ResourceLocation energyTextureLocation;
    protected InventoryPlayer player;
    protected TileGenerator tile;

    public GuiGenerator(InventoryPlayer playerInventory, TileGenerator tileEntity)
    {
        super(new ContainerGenerator(tileEntity, playerInventory));
        player = playerInventory;
        tile = tileEntity;
        ySize = 165;
        guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/generator.png");
        energyTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/Energy.png");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        String name = I18n.format(tile.getName());
        mc.fontRenderer.drawString(name, (xSize - mc.fontRenderer.getStringWidth(name)) / 2, 6, 0x404040);
        mc.fontRenderer.drawString(I18n.format(player.getName()), 8, ySize - 96 + 2, 0x404040);

        String label;
        if (tile.getGenerationPower() > 0)
        {
            label = I18n.format("text." + EnderRiftMod.MODID + ".generator.status.generating.label");
            mc.fontRenderer.drawString(label, 8, 22, 0x404040);
            mc.fontRenderer.drawString(String.format("%d RF/t", tile.getGenerationPower()), 12, 32, 0x404040);
        }
        else if (tile.isBurning())
        {
            label = I18n.format("text." + EnderRiftMod.MODID + ".generator.status.heating");
            mc.fontRenderer.drawString(label, 8, 22, 0x404040);
        }
        else
        {
            label = I18n.format("text." + EnderRiftMod.MODID + ".generator.status.idle");
            mc.fontRenderer.drawString(label, 8, 22, 0x404040);
        }

        label = I18n.format("text." + EnderRiftMod.MODID + ".generator.heat.label");
        mc.fontRenderer.drawString(label, 8, 46, 0x404040);
        mc.fontRenderer.drawString(String.format("%d C", tile.getHeatValue()), 12, 56, getHeatColor());

        String str = String.format("%d RF", tile.getContainedEnergy());
        mc.fontRenderer.drawString(str, xSize - 8 - mc.fontRenderer.getStringWidth(str), 64, 0x404040);

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
        tooltip.add(String.format("%d / %d RF", tile.getContainedEnergy(), TileGenerator.POWER_LIMIT));

        drawHoveringText(tooltip, mx - guiLeft, my - guiTop);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        mc.renderEngine.bindTexture(guiTextureLocation);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (tile.isBurning())
        {
            int k = getBurnLeftScaled(13);
            drawTexturedModalRect(guiLeft + 80, guiTop + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        drawEnergyBar(guiLeft + xSize - 14 - 8, guiTop + 20, tile.getContainedEnergy(), TileGenerator.POWER_LIMIT);
    }

    private void drawEnergyBar(int x, int y, int powerLevel, int powerLimit)
    {
        int bar2height = 1 + powerLevel * (barHeight - 2) / powerLimit;
        int bar1height = barHeight - bar2height;

        mc.renderEngine.bindTexture(energyTextureLocation);
        drawModalRectWithCustomSizedTexture(x, y, bar1x, 0, barWidth, bar1height, 32, 64);
        drawModalRectWithCustomSizedTexture(x, y + bar1height, bar2x, bar1height, barWidth, bar2height, 32, 64);
    }

    private int getHeatColor()
    {
        int heatLevel = tile.getHeatValue();
        if (heatLevel <= TileGenerator.MIN_HEAT)
            return 0x404040;


        float p = (heatLevel - TileGenerator.MIN_HEAT) / (float) (TileGenerator.MAX_HEAT - TileGenerator.MIN_HEAT);

        int r = 0xA0;
        int g = Math.round(0x40 + (0xA0 - 0x40) * (1 - p));
        int b = 0x40;

        return (r << 16) | (g << 8) | b;
    }

    private int getBurnLeftScaled(int pixels)
    {
        int i = tile.getCurrentItemBurnTime();

        if (i == 0)
        {
            i = 200;
        }

        return tile.getBurnTimeRemaining() * pixels / i;
    }
}