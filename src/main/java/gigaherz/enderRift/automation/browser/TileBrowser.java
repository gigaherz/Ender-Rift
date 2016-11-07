package gigaherz.enderRift.automation.browser;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.TileAggregator;
import gigaherz.enderRift.automation.iface.BlockInterface;
import gigaherz.enderRift.common.AutomationEnergyWrapper;
import gigaherz.enderRift.common.IPoweredAutomation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileBrowser extends TileAggregator implements IPoweredAutomation
{
    private int changeCount = 1;

    EnumFacing facing = null;

    AutomationEnergyWrapper wrapper = new AutomationEnergyWrapper(this);

    public EnumFacing getFacing()
    {
        if (facing == null && worldObj != null)
        {
            IBlockState state = worldObj.getBlockState(pos);
            if (state.getBlock() == EnderRiftMod.browser)
            {
                facing = state.getValue(BlockInterface.FACING).getOpposite();
            }
        }
        return facing;
    }

    public IItemHandler getAutomation()
    {
        return wrapper;
    }

    @Nullable
    @Override
    public IItemHandler getInventory()
    {
        return super.getAutomation(EnderRiftMod.browser);
    }

    @Override
    public IEnergyStorage getEnergyBuffer()
    {
        return getCombinedPowerBuffer();
    }

    @Override
    public void markDirty()
    {
        markDirty(true);
    }

    @Override
    protected void markDirty(boolean sendBroadcast)
    {
        changeCount++;
        facing = null;
        super.markDirty();
    }

    public int getChangeCount()
    {
        return changeCount;
    }

    @Override
    protected boolean canConnectSide(EnumFacing side)
    {
        return side == getFacing().getOpposite();
    }
}
