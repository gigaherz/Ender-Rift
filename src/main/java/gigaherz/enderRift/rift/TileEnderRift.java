package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.common.EnergyBuffer;
import gigaherz.enderRift.rift.storage.RiftInventory;
import gigaherz.enderRift.rift.storage.RiftStorageWorldData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEnderRift
        extends TileEntity implements ITickable
{
    private static final int STARTUP_POWER =  10000;
    private static final int BUFFER_POWER = 1000000;
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

    public EnergyBuffer getEnergyBuffer()
    {
        return energyBuffer;
    }

    @Override
    public void update()
    {
        if (worldObj.isRemote)
        {
            lastPoweringState = poweringState;
            if (powered)
            {
                if (!poweringStartParticlesSpawned)
                {
                    poweringStartParticlesSpawned = true;

                    for (int i = 0; i < 32; ++i)
                    {
                        this.worldObj.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.rand.nextGaussian(), this.rand.nextGaussian(), this.rand.nextGaussian());
                        this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.rand.nextGaussian(), this.rand.nextGaussian(), this.rand.nextGaussian());
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
        if (energyStored > energyUsage && !worldObj.isBlockPowered(pos))
        {
            if (powered)
            {
                energyBuffer.setEnergy(energyStored - energyUsage);
            }
            else if (energyStored >= STARTUP_POWER)
            {
                powered = true;
                energyBuffer.setEnergy(energyStored - STARTUP_POWER);
                IBlockState state = worldObj.getBlockState(pos);
                worldObj.notifyBlockUpdate(pos, state, state, 3);
            }
        }
        else
        {
            energyBuffer.setEnergy(0);
            powered = false;
            IBlockState state = worldObj.getBlockState(pos);
            worldObj.notifyBlockUpdate(pos, state, state, 3);
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

        if (inventory == null)
        {
            inventory = RiftStorageWorldData.get(worldObj).getRift(riftId);
            inventory.addWeakListener(this);
        }
        return inventory;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public boolean shouldRenderInPass(int pass)
    {
        return pass == 1;
    }

    public int countInventoryStacks()
    {
        return getInventory().getSlots();
    }

    public ItemStack getRiftItem()
    {
        ItemStack stack = new ItemStack(EnderRiftMod.riftOrb);

        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("RiftId", riftId);

        stack.setTagCompound(tag);

        return stack;
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        energyBuffer.setEnergy(compound.getInteger("Energy"));
        powered = compound.getBoolean("Powered");
        riftId = compound.getInteger("RiftId");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);
        compound.setInteger("Energy", energyBuffer.getEnergyStored());
        compound.setBoolean("Powered", powered);
        compound.setInteger("RiftId", riftId);
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound tag = super.getUpdateTag();
        tag.setBoolean("Powered", powered);
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        powered = tag.getBoolean("Powered");
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T)poweredInventory;
        return super.getCapability(capability, facing);
    }

    @Nullable
    public ItemStack chooseRandomStack()
    {
        if (getInventory() == null)
            return null;

        int max = getInventory().getSlots();

        if (max <= 0)
            return null;

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
        return MathHelper.ceiling_double_int(Math.pow(handler.getSlots(), 0.8));
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

        @Nullable
        @Override
        public ItemStack getStackInSlot(int slot)
        {
            if (!powered)
                return null;
            IItemHandler handler = getInventory();
            if (handler == null)
                return null;
            return handler.getStackInSlot(slot);
        }

        @Nullable
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

        @Nullable
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (!powered)
                return null;
            IItemHandler handler = getInventory();
            if (handler == null)
                return null;
            return handler.extractItem(slot, amount, simulate);
        }
    }
}