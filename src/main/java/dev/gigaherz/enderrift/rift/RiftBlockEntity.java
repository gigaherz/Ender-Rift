package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.common.EnergyBuffer;
import dev.gigaherz.enderrift.rift.storage.RiftHolder;
import dev.gigaherz.enderrift.rift.storage.RiftInventory;
import dev.gigaherz.enderrift.rift.storage.RiftStorage;
import dev.gigaherz.enderrift.rift.storage.migration.RiftMigration_17_08_2022;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class RiftBlockEntity extends BlockEntity implements IRiftChangeListener
{
    private static final int STARTUP_POWER = 10000;
    public static final int BUFFER_POWER = 1000000;
    private final Random rand = new Random();

    private EnergyBuffer energyBuffer = new EnergyBuffer(BUFFER_POWER);

    private boolean powered;
    private RiftHolder holder;
    private boolean listenerState;

    // Client-side, for rendering
    private float lastPoweringState;
    private float poweringState;
    private boolean poweringStartParticlesSpawned = false;
    // End of Client-side fields

    public PoweredInventory poweredInventory = new PoweredInventory();
    public LazyOptional<IItemHandler> poweredInventoryProvider = LazyOptional.of(() -> poweredInventory);

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
            holder.getInventoryOrCreate().addWeakListener(this);
            listenerState = true;
        }
        return holder.getInventory();
    }

    public int countInventoryStacks()
    {
        IItemHandler handler = getInventory();
        return handler == null ? 0 : (handler.getSlots() - 1);
    }

    public ItemStack getRiftItem()
    {
        ItemStack stack = new ItemStack(EnderRiftMod.RIFT_ORB.get());

        CompoundTag tag = new CompoundTag();

        tag.putUUID("RiftId", holder.getId());

        stack.setTag(tag);

        return stack;
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);

        energyBuffer.deserializeNBT(compound.get("Energy"));
        powered = compound.getBoolean("Powered");
        if (compound.contains("RiftId"))
        {
            RiftStorage storage = RiftStorage.get();
            UUID id = compound.hasUUID("RiftId") ? compound.getUUID("RiftId") : storage.getMigration(RiftMigration_17_08_2022.class).getMigratedId(compound.getInt("RiftId"));
            holder = storage.getRift(id);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.put("Energy", energyBuffer.serializeNBT());
        compound.putBoolean("Powered", powered);
        compound.remove("RiftId");
        if (holder != null)
        {
            compound.putUUID("RiftId", holder.getId());
        }
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("Powered", powered);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        powered = tag.getBoolean("Powered");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        handleUpdateTag(pkt.getTag());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return poweredInventoryProvider.cast();
        return super.getCapability(cap, side);
    }

    public ItemStack chooseRandomStack()
    {
        IItemHandler handler = getInventory();
        if (handler == null)
            return ItemStack.EMPTY;

        int max = handler.getSlots();

        if (max <= 0)
            return ItemStack.EMPTY;

        int slot = rand.nextInt(max);

        return poweredInventory.getStackInSlot(slot);
    }

    public UUID getRiftId()
    {
        if (holder == null)
        {
            return null;
        }
        return holder.getId();
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
        setChanged();
    }

    @Override
    public Optional<Level> getRiftLevel()
    {
        return Optional.ofNullable(getLevel());
    }

    @Override
    public Optional<BlockPos> getLocation()
    {
        return Optional.ofNullable(getBlockPos());
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, RiftBlockEntity te)
    {
        te.tick();
    }

    public class PoweredInventory implements IItemHandler
    {
        public long getCount(int slot)
        {
            RiftInventory inventory = getInventory();
            if (inventory == null)
            {
                return 0;
            }
            return inventory.getCount(slot);
        }

        @Override
        public int getSlots()
        {
            IItemHandler handler = getInventory();
            if (handler == null)
                return 0;

            return handler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            if (!powered)
                return ItemStack.EMPTY;
            IItemHandler handler = getInventory();
            if (handler == null)
                return ItemStack.EMPTY;
            return handler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (!powered)
                return stack;
            IItemHandler handler = getInventory();
            if (handler == null)
                return stack;
            return handler.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (!powered)
                return ItemStack.EMPTY;
            IItemHandler handler = getInventory();
            if (handler == null)
                return ItemStack.EMPTY;
            return handler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack)
        {
            return true;
        }
    }
}