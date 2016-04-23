package gigaherz.enderRift.gui;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.TileInterface;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GuiInterface extends GuiContainer
{
    protected InventoryPlayer player;
    protected TileInterface tile;

    private static final ResourceLocation guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/interface.png");
    private static final String textFilters = "text." + EnderRiftMod.MODID + ".interface.filters";
    private static final String textName = "container." + EnderRiftMod.MODID + ".interface";

    public GuiInterface(InventoryPlayer playerInventory, TileInterface tileEntity)
    {
        super(new ContainerInterface(tileEntity, playerInventory));
        player = playerInventory;
        tile = tileEntity;
        ySize = 176;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        String name = StatCollector.translateToLocal(textName);
        fontRendererObj.drawString(name, (xSize - fontRendererObj.getStringWidth(name)) / 2, 6, 0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal(this.player.getName()), 8, ySize - 96 + 2, 0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal(textFilters), 8, 20, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        mc.renderEngine.bindTexture(guiTextureLocation);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}