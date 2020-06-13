package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.common.EnergyBuffer;
import gigaherz.enderRift.rift.storage.RiftInventory;
import gigaherz.enderRift.rift.storage.RiftStorage;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class RiftTileEntity extends TileEntity implements ITickableTileEntity, IRiftChangeListener
{
    @ObjectHolder("enderrift:rift")
    public static TileEntityType<RiftTileEntity> TYPE;

    private static final int STARTUP_POWER = 10000;
    public static final int BUFFER_POWER = 1000000;
    private final Random rand = new Random();

    private EnergyBuffer energyBuffer = new EnergyBuffer(BUFFER_POWER);

    private boolean powered;
    private int riftId;
    private RiftInventory inventory;

    // Client-side, for rendering
    private float lastPoweringState;
    private float poweringState;
    private boolean poweringStartParticlesSpawned = false;
    // End of Client-side fields

    public PoweredInventory poweredInventory = new PoweredInventory();
    public LazyOptional<IItemHandler> poweredInventoryProvider = LazyOptional.of(() -> poweredInventory);

    public RiftTileEntity()
    {
        super(TYPE);
    }

    public Optional<IEnergyStorage> getEnergyBuffer()
    {
        return Optional.of(energyBuffer);
    }

    @Override
    public void tick()
    {
        if (world.isRemote)
        {
            lastPoweringState = poweringState;
            if (powered)
            {
                if (!poweringStartParticlesSpawned)
                {
                    poweringStartParticlesSpawned = true;

                    for (int i = 0; i < 32; ++i)
                    {
                        this.world.addParticle(ParticleTypes.CRIT, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.rand.nextGaussian(), this.rand.nextGaussian(), this.rand.nextGaussian());
                        this.world.addParticle(ParticleTypes.PORTAL, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.rand.nextGaussian(), this.rand.nextGaussian(), this.rand.nextGaussian());
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

        if (energyStored > energyUsage && !world.isBlockPowered(pos))
        {
            if (powered)
            {
                energyBuffer.setEnergy(energyStored - energyUsage);
            }
            else if (energyStored >= STARTUP_POWER)
            {
                powered = true;
                energyBuffer.setEnergy(energyStored - STARTUP_POWER);
                BlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }
        else
        {
            powered = false;
            BlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    public void assemble(int id)
    {
        inventory = null;
        riftId = id;
        markDirty();
    }

    public void unassemble()
    {
        inventory = null;
        riftId = -1;
        markDirty();
    }

    @Nullable
    public IItemHandler getInventory()
    {
        if (riftId < 0)
            return null;

        if (inventory == null && world != null && !world.isRemote)
        {
            inventory = RiftStorage.get(world).getRift(riftId);
            inventory.addWeakListener(this);
        }
        return inventory;
    }

    public int countInventoryStacks()
    {
        IItemHandler handler = getInventory();
        return handler == null ? 0 : handler.getSlots();
    }

    public ItemStack getRiftItem()
    {
        ItemStack stack = new ItemStack(EnderRiftMod.EnderRiftItems.RIFT_ORB);

        CompoundNBT tag = new CompoundNBT();

        tag.putInt("RiftId", riftId);

        stack.setTag(tag);

        return stack;
    }

    public void read(CompoundNBT compound)
    {
        super.read(compound);
        CapabilityEnergy.ENERGY.readNBT(energyBuffer, null, compound.get("Energy"));
        powered = compound.getBoolean("Powered");
        riftId = compound.getInt("RiftId");
    }

    public CompoundNBT write(CompoundNBT compound)
    {
        compound = super.write(compound);
        compound.put("Energy", CapabilityEnergy.ENERGY.writeNBT(energyBuffer, null));
        compound.putBoolean("Powered", powered);
        compound.putInt("RiftId", riftId);
        return compound;
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT tag = super.getUpdateTag();
        tag.putBoolean("Powered", powered);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag)
    {
        powered = tag.getBoolean("Powered");
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        handleUpdateTag(pkt.getNbtCompound());
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

    public int getRiftId()
    {
        return riftId;
    }

    public int getEnergyUsage()
    {
        IItemHandler handler = getInventory();
        if (handler == null)
            return 0;
        return MathHelper.ceil(Math.pow(handler.getSlots(), 0.8));
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
        markDirty();
    }

    public class PoweredInventory implements IItemHandler
    {
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