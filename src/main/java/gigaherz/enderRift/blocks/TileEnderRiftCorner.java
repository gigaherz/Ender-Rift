package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEnderRiftCorner
        extends TileEntity
        implements IEnergyReceiver
{
    int xParent = 0;
    int yParent;
    int zParent;
    TileEnderRift energyParent;

    public IEnergyReceiver getParent()
    {
        if (energyParent == null)
        {
            int meta = getBlockMetadata();
            xParent = xCoord + ((meta & 1) != 0 ? -1 : 1);
            yParent = yCoord + ((meta & 2) != 0 ? -1 : 1);
            zParent = zCoord + ((meta & 4) != 0 ? -1 : 1);
            TileEntity te = worldObj.getTileEntity(xParent, yParent, zParent);
            if (te instanceof TileEnderRift)
            {
                energyParent = (TileEnderRift) te;
            }
        }
        return energyParent;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound)
    {
        super.writeToNBT(nbtTagCompound);
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
    {
        IEnergyReceiver parent = getParent();
        if (parent == null)
            return 0;
        return parent.receiveEnergy(from, maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(ForgeDirection from)
    {
        IEnergyReceiver parent = getParent();
        if (parent == null)
            return 0;
        return getParent().getEnergyStored(from);
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from)
    {
        IEnergyReceiver parent = getParent();
        if (parent == null)
            return 0;
        return getParent().getMaxEnergyStored(from);
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from)
    {
        return true;
    }

}