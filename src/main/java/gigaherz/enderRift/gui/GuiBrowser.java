package gigaherz.enderRift.gui;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.TileBrowser;
import gigaherz.enderRift.blocks.TileInterface;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiBrowser extends GuiContainer
{
    protected InventoryPlayer player;
    protected TileBrowser tile;
    protected ResourceLocation guiTextureLocation;

    static final String textBrowser= "text." + EnderRiftMod.MODID + ".browser";

    public GuiBrowser(InventoryPlayer playerInventory, TileBrowser tileEntity)
    {
        super(new ContainerBrowser(tileEntity, playerInventory));
        this.player = playerInventory;
        this.tile = tileEntity;
        this.xSize = 185;
        this.ySize = 168;
        guiTextureLocation = new ResourceLocation(EnderRiftMod.MODID, "textures/gui/browser.png");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j)
    {
        String name = StatCollector.translateToLocal(textBrowser);
        mc.fontRendererObj.drawString(name, (xSize - mc.fontRendererObj.getStringWidth(name)) / 2, 6, 0x404040);
        mc.fontRendererObj.drawString(StatCollector.translateToLocal(this.player.getName()), 8, ySize - 96 + 2, 0x404040);
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