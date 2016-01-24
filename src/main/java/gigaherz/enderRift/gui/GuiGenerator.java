package gigaherz.enderRift.gui;

import com.google.common.collect.Lists;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.TileGenerator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

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
        this.player = playerInventory;
        this.tile = tileEntity;
        this.ySize = 165;
        guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/generator.png");
        energyTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/Energy.png");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        String name = StatCollector.translateToLocal(this.tile.getName());
        mc.fontRendererObj.drawString(name, (xSize - mc.fontRendererObj.getStringWidth(name)) / 2, 6, 0x404040);
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.player.getName()), 8, ySize - 96 + 2, 0x404040);

        String label;
        if (tile.getPowerGeneration() > 0)
        {
            label = StatCollector.translateToLocal("text." + EnderRiftMod.MODID + ".generator.status.generating.label");
            mc.fontRendererObj.drawString(label, 8, 22, 0x404040);
            mc.fontRendererObj.drawString(String.format("%d RF/t", tile.getPowerGeneration()), 12, 32, 0x404040);
        }
        else if (tile.isBurning())
        {
            label = StatCollector.translateToLocal("text." + EnderRiftMod.MODID + ".generator.status.heating");
            mc.fontRendererObj.drawString(label, 8, 22, 0x404040);
        }
        else
        {
            label = StatCollector.translateToLocal("text." + EnderRiftMod.MODID + ".generator.status.idle");
            mc.fontRendererObj.drawString(label, 8, 22, 0x404040);
        }

        label = StatCollector.translateToLocal("text." + EnderRiftMod.MODID + ".generator.heat.label");
        mc.fontRendererObj.drawString(label, 8, 46, 0x404040);
        mc.fontRendererObj.drawString(String.format("%d C", tile.getHeatValue()), 12, 56, getHeatColor());

        String str = String.format("%d RF", tile.getPowerLevel());
        mc.fontRendererObj.drawString(str, xSize - 8 - mc.fontRendererObj.getStringWidth(str), 64, 0x404040);

        drawBarTooltip(i, j, xSize - 14 - 8, 20);
    }

    private void drawBarTooltip(int mx, int my, int ox, int oy)
    {
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        int rx = mx - ox - x;
        int ry = my - oy - y;

        if (rx < 0 || ry < 0 || rx > barWidth || ry > barHeight)
            return;

        List<String> tooltip = Lists.newArrayList();
        tooltip.add(StatCollector.translateToLocal("text." + EnderRiftMod.MODID + ".generator.energy.label"));
        tooltip.add(String.format("%d / %d RF", tile.getPowerLevel(), TileGenerator.PowerLimit));

        drawHoveringText(tooltip, mx - x, my - y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        mc.renderEngine.bindTexture(guiTextureLocation);
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        if (tile.isBurning())
        {
            int k = this.getBurnLeftScaled(13);
            this.drawTexturedModalRect(x + 80, y + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        drawEnergyBar(x + xSize - 14 - 8, y + 20, tile.getPowerLevel(), TileGenerator.PowerLimit);
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
        if (heatLevel <= TileGenerator.MinHeat)
            return 0x404040;


        float p = (heatLevel - TileGenerator.MinHeat) / (float) (TileGenerator.MaxHeat - TileGenerator.MinHeat);

        int r = 0xA0;
        int g = Math.round(0x40 + (0xA0 - 0x40) * (1 - p));
        int b = 0x40;

        return (r << 16) | (g << 8) | b;
    }

    private int getBurnLeftScaled(int pixels)
    {
        int i = this.tile.getField(1);

        if (i == 0)
        {
            i = 200;
        }

        return this.tile.getField(0) * pixels / i;
    }
}