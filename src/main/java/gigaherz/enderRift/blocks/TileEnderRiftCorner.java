package gigaherz.enderRift.blocks;

import gigaherz.capabilities.api.energy.CapabilityEnergy;
import gigaherz.capabilities.api.energy.IEnergyHandler;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

public class TileEnderRiftCorner
        extends TileEntity
{
    TileEnderRift energyParent;

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return getParent() != null;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return (T) getParent();
        return super.getCapability(capability, facing);
    }

    public IEnergyHandler getParent()
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