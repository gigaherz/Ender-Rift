package gigaherz.enderRift.rift;

import gigaherz.enderRift.ConfigValues;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.rift.storage.RiftInventory;
import gigaherz.enderRift.rift.storage.RiftStorageWorldData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Random;

public class TileEnderRift
        extends TileEntity
{
    public final Random rand = new Random();

    class EnergyBuffer extends EnergyStorage
    {
        public EnergyBuffer(int capacity)
        {
            super(capacity);
        }

        public void setEnergy(int energy)
        {
            this.energy = energy;
        }
    }

    private EnergyBuffer energyBuffer = new EnergyBuffer(10000000);

    private int riftId;
    private RiftInventory inventory;

    private AutomationEnergyWrapper automation = new AutomationEnergyWrapper();

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

    public RiftInventory getInventory()
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

    public IEnergyStorage getEnergyBuffer()
    {
        return energyBuffer;
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

    public double getEnergyInsert()
    {
        if (getInventory() == null)
            return 0;
        int sizeInventory = getInventory().getSlots();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return ConfigValues.PowerPerInsertionConstant
                + (sizeInventory * ConfigValues.PowerPerInsertionLinear)
                + (sizeInventory2 * ConfigValues.PowerPerInsertionGeometric);
    }

    public double getEnergyExtract()
    {
        if (getInventory() == null)
            return 0;
        int sizeInventory = getInventory().getSlots();
        int sizeInventory2 = sizeInventory * sizeInventory;
        return ConfigValues.PowerPerExtractionConstant
                + (sizeInventory * ConfigValues.PowerPerExtractionLinear)
                + (sizeInventory2 * ConfigValues.PowerPerExtractionGeometric);
    }

    public int countInventoryStacks()
    {
        return getInventory().getSlots();
    }

    private int getEffectivePowerUsageToInsert(int stackSize)
    {
        return worldObj.isRemote ? 0 : (int) Math.ceil(getEnergyInsert() * stackSize);
    }

    private int getEffectivePowerUsageToExtract(int limit)
    {
        return worldObj.isRemote ? 0 : (int) Math.ceil(getEnergyExtract() * limit);
    }

    public ItemStack getRiftItem()
    {
        ItemStack stack = new ItemStack(EnderRiftMod.riftOrb);

        NBTTagCompound tag = new NBTTagCompound();

        tag.setInteger("RiftId", riftId);

        stack.setTagCompound(tag);

        return stack;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound)
    {
        super.readFromNBT(nbtTagCompound);
        energyBuffer.setEnergy(nbtTagCompound.getInteger("Energy"));
        riftId = nbtTagCompound.getInteger("RiftId");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound)
    {
        nbtTagCompound = super.writeToNBT(nbtTagCompound);
        nbtTagCompound.setInteger("Energy", energyBuffer.getEnergyStored());
        nbtTagCompound.setInteger("RiftId", riftId);
        return nbtTagCompound;
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
            return (T)automation;
        return super.getCapability(capability, facing);
    }

    class AutomationEnergyWrapper implements IItemHandler
    {

        @Override
        public int getSlots()
        {
            return getInventory().getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return getInventory().getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (getInventory() == null)
                return stack;

            int stackSize = stack.stackSize;
            int cost = getEffectivePowerUsageToInsert(stackSize);
            while (cost > energyBuffer.getEnergyStored() && stackSize > 0)
            {
                stackSize--;
            }

            if (stackSize <= 0)
                return stack;

            ItemStack temp = stack.copy();
            temp.stackSize = stackSize;

            ItemStack remaining = getInventory().insertItem(slot, temp, simulate);

            if (!simulate)
            {
                if (remaining != null)
                    stackSize -= remaining.stackSize;

                int actualCost = getEffectivePowerUsageToInsert(stackSize);
                energyBuffer.extractEnergy(actualCost, false);

                markDirty();
            }

            return remaining;
        }

        @Override
        public ItemStack extractItem(int slot, int wanted, boolean simulate)
        {
            if (getInventory() == null)
                return null;

            int cost = getEffectivePowerUsageToExtract(wanted);
            while (cost > energyBuffer.getEnergyStored() && wanted > 0)
            {
                wanted--;
            }

            if (wanted <= 0)
                return null;

            ItemStack extracted = getInventory().extractItem(slot, wanted, simulate);
            if (extracted == null)
                return null;

            if (!simulate)
            {
                int actualCost = getEffectivePowerUsageToExtract(extracted.stackSize);
                energyBuffer.extractEnergy(actualCost, false);

                markDirty();
            }

            return extracted;
        }
    }

    public ItemStack chooseRandomStack()
    {
        if (getInventory() == null)
            return null;

        int max = getInventory().getSlots();

        if (max <= 0)
            return null;

        int slot = rand.nextInt(max);

        return automation.getStackInSlot(slot);
    }

    public int getRiftId()
    {
        return riftId;
    }
}