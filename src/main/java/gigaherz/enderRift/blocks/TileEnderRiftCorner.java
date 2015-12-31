package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import net.minecraft.block.state.IBlockState;
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
            IBlockState state = worldObj.getBlockState(pos);
            boolean base = state.getValue(BlockStructure.BASE);
            BlockStructure.Corner corner = state.getValue(BlockStructure.CORNER) ;
            xParent = pos.getX();
            yParent = pos.getY() + (base?1:-1);
            zParent = pos.getZ();
            switch(corner)
            {
                case NE: xParent-=1; zParent+=1; break;
                case NW: xParent+=1; zParent+=1; break;
                case SE: xParent-=1; zParent-=1; break;
                case SW: xParent+=1; zParent-=1; break;
            }
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