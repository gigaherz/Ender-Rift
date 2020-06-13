package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.AggregatorTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.Optional;

public class StructureTileEntity extends AggregatorTileEntity
{
    @ObjectHolder("enderrift:structure")
    public static TileEntityType<?> TYPE;

    private RiftTileEntity energyParent;

    private final LazyOptional<IEnergyStorage> bufferProvider = LazyOptional.of(() -> getEnergyBuffer().orElse(null));

    public StructureTileEntity()
    {
        super(TYPE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
            return bufferProvider.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public Optional<IEnergyStorage> getEnergyBuffer()
    {
        return getInternalBuffer();
    }

    @Override
    public Optional<IEnergyStorage> getInternalBuffer()
    {
        return getParent().flatMap(RiftTileEntity::getEnergyBuffer);
    }

    @Override
    protected void lazyDirty()
    {
        // Nothing to do
    }

    @Override
    protected boolean canConnectSide(Direction side)
    {
        return false;
    }

    public Optional<RiftTileEntity> getParent()
    {
        if (energyParent == null)
        {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() != EnderRiftMod.EnderRiftBlocks.STRUCTURE)
                return Optional.empty();

            TileEntity te = world.getTileEntity(getRiftFromCorner(state, pos));
            if (te instanceof RiftTileEntity)
            {
                energyParent = (RiftTileEntity) te;
            }
            else
            {
                return Optional.empty();
            }
        }
        return Optional.of(energyParent);
    }

    private static BlockPos getRiftFromCorner(BlockState state, BlockPos pos)
    {
        boolean base = state.get(StructureBlock.BASE);
        StructureBlock.Corner corner = state.get(StructureBlock.CORNER);
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