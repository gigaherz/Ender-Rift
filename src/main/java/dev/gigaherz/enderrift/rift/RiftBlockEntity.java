package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.common.EnergyBuffer;
import dev.gigaherz.enderrift.rift.storage.RiftHolder;
import dev.gigaherz.enderrift.rift.storage.RiftInventory;
import dev.gigaherz.enderrift.rift.storage.RiftStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = EnderRiftMod.MODID)
public class RiftBlockEntity extends BlockEntity implements IRiftChangeListener
{
    @SubscribeEvent
    private static void registerCapability(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                EnderRiftMod.RIFT_BLOCK_ENTITY.get(),
                (be, context) -> be.powered ? be.getInventory() : null
        );
    }

    private static final int STARTUP_POWER = 10000;
    public static final int BUFFER_POWER = 1000000;
    private final RandomSource rand = RandomSource.create();

    private final EnergyBuffer energyBuffer = new EnergyBuffer(BUFFER_POWER);

    private boolean changedFlag;

    private boolean powered;
    private RiftHolder holder;
    private boolean listenerState;

    // Client-side, for rendering
    private float lastPoweringState;
    private float poweringState;
    private boolean poweringStartParticlesSpawned = false;
    // End of Client-side fields

    public RiftBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.RIFT_BLOCK_ENTITY.get(), pos, state);
    }

    public Optional<IEnergyStorage> getEnergyBuffer()
    {
        return Optional.of(energyBuffer);
    }

    public void tick()
    {
        if (level.isClientSide)
        {
            lastPoweringState = poweringState;
            if (powered)
            {
                if (!poweringStartParticlesSpawned)
                {
                    poweringStartParticlesSpawned = true;

                    for (int i = 0; i < 32; ++i)
                    {
                        this.level.addParticle(ParticleTypes.CRIT, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, this.rand.nextGaussian(), this.rand.nextGaussian(), this.rand.nextGaussian());
                        this.level.addParticle(ParticleTypes.PORTAL, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, this.rand.nextGaussian(), this.rand.nextGaussian(), this.rand.nextGaussian());
                    }
                }
                if (poweringState < 1)
                    poweringState += 0.1f;
                else
                    poweringState = 1;
            }
            else
            {
                poweringStartParticlesSpawned = false;
                if (poweringState > 0)
                    poweringState -= 0.02f;
                else
                    poweringState = 0;
            }
            return;
        }

        int energyUsage = getEnergyUsage();
        int energyStored = energyBuffer.getEnergyStored();
        if (energyStored > energyBuffer.getMaxEnergyStored())
        {
            energyStored = energyBuffer.getMaxEnergyStored();
            energyBuffer.setEnergy(energyStored);
        }

        if (energyStored > energyUsage && !level.hasNeighborSignal(worldPosition))
        {
            if (powered)
            {
                energyBuffer.setEnergy(energyStored - energyUsage);
            }
            else if (energyStored >= STARTUP_POWER)
            {
                powered = true;
                energyBuffer.setEnergy(energyStored - STARTUP_POWER);
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 3);
            }
        }
        else
        {
            powered = false;
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }

        if (changedFlag)
        {
            setChanged();
            changedFlag = false;
        }
    }

    public void assemble(RiftHolder holder)
    {
        this.holder = holder;
        listenerState = false;
        setChanged();
    }

    public void unassemble()
    {
        holder = null;
        listenerState = false;
        setChanged();
    }

    @Nullable
    public RiftInventory getInventory()
    {
        if (holder == null)
            return null;

        if (!listenerState && level != null && !level.isClientSide)
        {
            RiftInventory inv = holder.getOrLoad(level.registryAccess());
            inv.addWeakListener(this);
            listenerState = true;
            return inv;
        }

        return holder.getOrLoad(level.registryAccess());
    }

    public ItemStack getRiftItem()
    {
        var stack = new ItemStack(EnderRiftMod.RIFT_ORB.get());

        stack.set(EnderRiftMod.RIFT_ID, holder.getId());

        return stack;
    }


    @Override
    protected void applyImplicitComponents(DataComponentGetter componentGetter)
    {
        holder = RiftStorage.getOrCreateRift(componentGetter.get(EnderRiftMod.RIFT_ID));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder pComponents)
    {
        if (holder != null)
            pComponents.set(EnderRiftMod.RIFT_ID, holder.getId());
    }

    private static UUID uuidFromIntArray(int[] p_235886_) {
        return new UUID((long)p_235886_[0] << 32 | (long)p_235886_[1] & 4294967295L, (long)p_235886_[2] << 32 | (long)p_235886_[3] & 4294967295L);
    }

    private static int[] uuidToIntArray(UUID p_235882_) {
        long i = p_235882_.getMostSignificantBits();
        long j = p_235882_.getLeastSignificantBits();
        return new int[]{(int)(i >> 32), (int) i, (int)(j >> 32), (int) j};
    }

    @Override
    public void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

        energyBuffer.deserialize(input.childOrEmpty("Energy"));
        powered = input.getBooleanOr("Powered", false);
        var id0 = input.getIntArray("RiftId");
        if (id0.isPresent())
        {
            UUID id = uuidFromIntArray(id0.get());
            holder = RiftStorage.getOrCreateRift(id);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

        energyBuffer.serialize(output.child("Energy"));
        output.putBoolean("Powered", powered);
        if (holder != null)
        {
            output.putIntArray("RiftId", uuidToIntArray(holder.getId()));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup)
    {
        CompoundTag tag = super.getUpdateTag(lookup);
        tag.putBoolean("Powered", powered);
        return tag;
    }

    @Override
    public void handleUpdateTag(ValueInput input)
    {
        powered = input.getBooleanOr("Powered", false);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input)
    {
        handleUpdateTag(input);
    }

    public ItemStack chooseRandomStack()
    {
        if (!powered)
            return ItemStack.EMPTY;

        IItemHandler handler = getInventory();
        if (handler == null)
            return ItemStack.EMPTY;

        int max = handler.getSlots();

        if (max <= 0)
            return ItemStack.EMPTY;

        int slot = rand.nextInt(max);

        return handler.getStackInSlot(slot);
    }

    public int getEnergyUsage()
    {
        IItemHandler handler = getInventory();
        if (handler == null)
            return 0;
        return Mth.ceil(Math.pow(handler.getSlots(), 0.8));
    }

    public boolean isPowered()
    {
        return powered;
    }

    public float getPoweringState()
    {
        return poweringState;
    }

    public float getLastPoweringState()
    {
        return lastPoweringState;
    }

    @Override
    public boolean isInvalid()
    {
        return isRemoved();
    }

    @Override
    public void onRiftChanged()
    {
        changedFlag = true;
    }

    @Override
    public Optional<Level> getRiftLevel()
    {
        return Optional.ofNullable(getLevel());
    }

    @Override
    public BlockPos getLocation()
    {
        return getBlockPos();
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, RiftBlockEntity te)
    {
        te.tick();
    }

    @Nullable
    public UUID getRiftId()
    {
        return this.holder != null ? this.holder.getId() : null;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        super.preRemoveSideEffects(pos, state);

        Block.popResource(level, pos, this.getRiftItem());
    }
}
