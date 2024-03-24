package dev.gigaherz.enderrift.automation.driver;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = EnderRiftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
    public void load(CompoundTag compound)
    {
        super.load(compound);

        energyBuffer.deserializeNBT(compound.get("storedEnergy"));
    }

    @Override
    protected void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.put("storedEnergy", energyBuffer.serializeNBT());
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, DriverBlockEntity te)
    {
        te.tick();
    }
}