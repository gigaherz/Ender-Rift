package dev.gigaherz.enderrift.automation.driver;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;

import java.util.Optional;

@EventBusSubscriber(modid = EnderRiftMod.MODID)
public class DriverBlockEntity extends AggregatorBlockEntity
{
    @SubscribeEvent
    private static void registerCapability(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                EnderRiftMod.DRIVER_BLOCK_ENTITY.get(),
                (be, context) -> be.energyBuffer
        );
    }

    public static final int POWER_LIMIT = 100000;

    final SimpleEnergyHandler energyBuffer = new SimpleEnergyHandler(POWER_LIMIT);

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
    public Optional<EnergyHandler> getInternalBuffer()
    {
        return Optional.of(energyBuffer);
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

        energyBuffer.deserialize(input.childOrEmpty("storedEnergy"));
    }

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

        energyBuffer.serialize(output.child("storedEnergy"));
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, DriverBlockEntity te)
    {
        te.tick();
    }
}