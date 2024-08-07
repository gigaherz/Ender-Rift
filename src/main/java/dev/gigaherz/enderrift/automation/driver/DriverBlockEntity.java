package dev.gigaherz.enderrift.automation.driver;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Optional;

@EventBusSubscriber(modid = EnderRiftMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DriverBlockEntity extends AggregatorBlockEntity
{
    @SubscribeEvent
    private static void registerCapability(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                EnderRiftMod.DRIVER_BLOCK_ENTITY.get(),
                (be, context) -> be.energyBuffer
        );
    }

    public static final int POWER_LIMIT = 100000;

    final EnergyStorage energyBuffer = new EnergyStorage(POWER_LIMIT);

    public DriverBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.DRIVER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void lazyDirty()
    {
        // Nothing to do here
    }

    @Override
    protected boolean canConnectSide(Direction side)
    {
        return false;
    }

    @Override
    public Optional<IEnergyStorage> getInternalBuffer()
    {
        return Optional.of(energyBuffer);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup)
    {
        super.loadAdditional(compound, lookup);

        energyBuffer.deserializeNBT(lookup, compound.get("storedEnergy"));
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup)
    {
        super.saveAdditional(compound, lookup);

        compound.put("storedEnergy", energyBuffer.serializeNBT(lookup));
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, DriverBlockEntity te)
    {
        te.tick();
    }
}