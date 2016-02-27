package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyReceiver;
import gigaherz.capabilities.api.energy.CapabilityEnergy;
import gigaherz.capabilities.api.energy.IEnergyHandler;
import gigaherz.capabilities.api.energy.compat.RFWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class TileEnderRiftCorner
        extends TileEntity
        implements IEnergyReceiver
{
    int xParent = 0;
    int yParent;
    int zParent;
    TileEnderRift energyParent;

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return getParent() != null;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return (T)getParent();
        return super.getCapability(capability, facing);
    }

    public IEnergyHandler getParent()
    {
        if (energyParent == null)
        {
            IBlockState state = worldObj.getBlockState(pos);
            boolean base = state.getValue(BlockStructure.BASE);
            BlockStructure.Corner corner = state.getValue(BlockStructure.CORNER);
            xParent = pos.getX();
            yParent = pos.getY() + (base ? 1 : -1);
            zParent = pos.getZ();
            switch (corner)
            {
                case NE:
                    xParent -= 1;
                    zParent += 1;
                    break;
                case NW:
                    xParent += 1;
                    zParent += 1;
                    break;
                case SE:
                    xParent -= 1;
                    zParent -= 1;
                    break;
                case SW:
                    xParent += 1;
                    zParent -= 1;
                    break;
            }
            TileEntity te = worldObj.getTileEntity(new BlockPos(xParent, yParent, zParent));
            if (te instanceof TileEnderRift)
            {
                energyParent = (TileEnderRift) te;
            }
        }
        return energyParent.getEnergyBuffer();
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
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate)
    {
        IEnergyHandler parent = getParent();
        if (parent == null)
            return 0;
        return parent.insertEnergy(maxReceive, simulate);
    }

    @Override
    public int getEnergyStored(EnumFacing from)
    {
        IEnergyHandler parent = getParent();
        if (parent == null)
            return 0;
        return getParent().getEnergy();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from)
    {
        IEnergyHandler parent = getParent();
        if (parent == null)
            return 0;
        return getParent().getCapacity();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from)
    {
        return getParent() != null;
    }
}