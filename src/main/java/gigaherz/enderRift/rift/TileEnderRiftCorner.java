package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.plugins.tesla.TeslaControllerBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileEnderRiftCorner
        extends TileEntity
{
    TileEnderRift energyParent;

    private Capability teslaConsumerCap;
    private Object teslaConsumerInstance;

    private Capability teslaHolderCap;
    private Object teslaHolderInstance;

    public TileEnderRiftCorner()
    {
        teslaConsumerCap = TeslaControllerBase.CONSUMER.getCapability();
        teslaHolderCap = TeslaControllerBase.HOLDER.getCapability();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
            return getParent() != null;
        if (teslaConsumerCap != null && capability == teslaConsumerCap)
            return getParent() != null;
        if (teslaHolderCap != null && capability == teslaHolderCap)
            return getParent() != null;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
            return (T) getParent();
        if (teslaConsumerCap != null && capability == teslaConsumerCap)
        {
            if (teslaConsumerInstance == null && getParent() != null)
                teslaConsumerInstance = TeslaControllerBase.CONSUMER.createInstance(getParent());
            return (T) teslaConsumerInstance;
        }
        if (teslaHolderCap != null && capability == teslaHolderCap)
        {
            if (teslaHolderInstance == null && getParent() != null)
                teslaHolderInstance = TeslaControllerBase.HOLDER.createInstance(getParent());
            return (T) teslaHolderInstance;
        }
        return super.getCapability(capability, facing);
    }

    public IEnergyStorage getParent()
    {
        if (energyParent == null)
        {
            IBlockState state = worldObj.getBlockState(pos);
            if (state.getBlock() != EnderRiftMod.structure)
                return null;

            TileEntity te = worldObj.getTileEntity(getRiftFromCorner(state, pos));
            if (te instanceof TileEnderRift)
            {
                energyParent = (TileEnderRift) te;
            }
            else
            {
                return null;
            }
        }
        return energyParent.getEnergyBuffer();
    }

    private static BlockPos getRiftFromCorner(IBlockState state, BlockPos pos)
    {
        boolean base = state.getValue(BlockStructure.BASE);
        BlockStructure.Corner corner = state.getValue(BlockStructure.CORNER);
        int xParent = pos.getX();
        int yParent = pos.getY() + (base ? 1 : -1);
        int zParent = pos.getZ();
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
        return new BlockPos(xParent, yParent, zParent);
    }
}