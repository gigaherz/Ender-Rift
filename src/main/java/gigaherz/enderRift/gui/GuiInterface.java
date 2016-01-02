package gigaherz.enderRift.gui;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.TileInterface;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiInterface extends GuiContainer
{
    protected InventoryPlayer player;
    protected TileInterface tile;
    protected ResourceLocation guiTextureLocation;

    static final String textFilters = "text." + EnderRiftMod.MODID + ".interface.filters";

    public GuiInterface(InventoryPlayer playerInventory, TileInterface tileEntity)
    {
        super(new ContainerInterface(tileEntity, playerInventory));
        this.player = playerInventory;
        this.tile = tileEntity;
        this.ySize = 176;
        guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/interface.png");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.tile.getName()), 8, 6, 0x404040);
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.player.getName()), 8, ySize - 96 + 2, 0x404040);

        String text = StatCollector.translateToLocal(textFilters);
        mc.fontRendererObj.drawString(text, (xSize - mc.fontRendererObj.getStringWidth(text)) / 2, 20, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        mc.renderEngine.bindTexture(guiTextureLocation);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}