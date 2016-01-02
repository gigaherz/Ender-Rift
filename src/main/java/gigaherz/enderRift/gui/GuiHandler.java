package gigaherz.enderRift.gui;

import gigaherz.enderRift.blocks.TileInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity instanceof TileInterface)
        {
            return new ContainerInterface((TileInterface) tileEntity, player.inventory);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

        if (tileEntity instanceof TileInterface)
        {
            return new GuiInterface(player.inventory, (TileInterface) tileEntity);
        }

        return null;
    }
}
