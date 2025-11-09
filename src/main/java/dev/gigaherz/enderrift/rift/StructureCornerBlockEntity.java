package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Optional;

@EventBusSubscriber(modid = EnderRiftMod.MODID)
public class StructureCornerBlockEntity extends AggregatorBlockEntity
{
    @SubscribeEvent
    private static void registerCapability(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                EnderRiftMod.STRUCTURE_CORNER_BLOCK_ENTITY.get(),
                (be, context) -> be.getEnergyBuffer().orElse(null)
        );
    }

    private RiftBlockEntity energyParent;

    public StructureCornerBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.STRUCTURE_CORNER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Optional<IEnergyStorage> getEnergyBuffer()
    {
        return getInternalBuffer();
    }

    @Override
    public Optional<IEnergyStorage> getInternalBuffer()
    {
        return getParent().flatMap(RiftBlockEntity::getEnergyBuffer);
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

    public Optional<RiftBlockEntity> getParent()
    {
        if (energyParent == null)
        {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() != EnderRiftMod.STRUCTURE_CORNER.get())
                return Optional.empty();

            BlockEntity te = level.getBlockEntity(getRiftFromCorner(state, worldPosition));
            if (!(te instanceof RiftBlockEntity rift))
            {
                return Optional.empty();
            }

            energyParent = rift;
        }
        return Optional.of(energyParent);
    }

    private static BlockPos getRiftFromCorner(BlockState state, BlockPos pos)
    {
        boolean base = state.getValue(StructureCornerBlock.BASE);
        StructureCornerBlock.Corner corner = state.getValue(StructureCornerBlock.CORNER);
        int xParent = pos.getX();
        int yParent = pos.getY() + (base ? 1 : -1);
        int zParent = pos.getZ();
        switch (corner)
        {
            case NE ->
            {
                xParent -= 1;
                zParent += 1;
            }
            case NW ->
            {
                xParent += 1;
                zParent += 1;
            }
            case SE ->
            {
                xParent -= 1;
                zParent -= 1;
            }
            case SW ->
            {
                xParent += 1;
                zParent -= 1;
            }
        }
        return new BlockPos(xParent, yParent, zParent);
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, StructureCornerBlockEntity te)
    {
        te.tick();
    }
}