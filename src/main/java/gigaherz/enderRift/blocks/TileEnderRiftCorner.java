package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class TileEnderRiftCorner
        extends TileEntity
        implements IEnergyReceiver {
    int xParent = 0;
    int yParent;
    int zParent;
    TileEnderRift energyParent;

    public IEnergyReceiver getParent() {
        if (energyParent == null) {
            int meta = getBlockMetadata();
            xParent = pos.getX() + ((meta & 2) != 0 ? -1 : 1);
            yParent = pos.getY() + ((meta & 1) != 0 ? -1 : 1);
            zParent = pos.getZ() + ((meta & 4) != 0 ? -1 : 1);
            TileEntity te = worldObj.getTileEntity(new BlockPos(xParent, yParent, zParent));
            if (te instanceof TileEnderRift) {
                energyParent = (TileEnderRift) te;
            }
        }
        return energyParent;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        super.readFromNBT(nbtTagCompound);
    }

    public void writeToNBT(NBTTagCompound nbtTagCompound) {
        super.writeToNBT(nbtTagCompound);
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        IEnergyReceiver parent = getParent();
        if (parent == null)
            return 0;
        return parent.receiveEnergy(from, maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        IEnergyReceiver parent = getParent();
        if (parent == null)
            return 0;
        return getParent().getEnergyStored(from);
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        IEnergyReceiver parent = getParent();
        if (parent == null)
            return 0;
        return getParent().getMaxEnergyStored(from);
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

}