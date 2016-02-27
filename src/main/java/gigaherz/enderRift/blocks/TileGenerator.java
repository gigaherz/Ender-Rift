package gigaherz.enderRift.blocks;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import gigaherz.capabilities.api.energy.CapabilityEnergy;
import gigaherz.capabilities.api.energy.IEnergyHandler;
import gigaherz.capabilities.api.energy.compat.RFWrapper;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;

public class TileGenerator extends TileEntity
        implements IInventory, ITickable, IEnergyProvider
{
    public static final int SlotCount = 1;
    public static final int PowerLimit = 100000;
    public static final int MinHeat = 100;
    public static final int MaxHeat = 1000;
    public static final int PowerGenMin = 20;
    public static final int PowerGenMax = 200;
    public static final int HeatInterval = 20;
    public static final int PowerTransferMax = 800;

    final ItemStack[] inputs = new ItemStack[SlotCount];

    int heatLevel;
    int burnTimeRemaining;
    int currentItemBurnTime;
    int containedEnergy;
    int timeInterval;

    RFWrapper energyCapability = RFWrapper.wrap(this, null);

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return (T)energyCapability;
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void update()
    {
        if (worldObj.isRemote)
            return;

        boolean anyChanged = false;

        timeInterval++;
        if (burnTimeRemaining > 0)
        {
            burnTimeRemaining -= Math.max(1, heatLevel / MinHeat);
            if (burnTimeRemaining <= 0)
                timeInterval = 0;
            if (timeInterval >= HeatInterval && heatLevel < MaxHeat)
            {
                timeInterval = 0;
                heatLevel++;
                anyChanged = true;
            }
        }
        else if (heatLevel > 0)
        {
            if (timeInterval >= HeatInterval)
            {
                timeInterval = 0;
                heatLevel--;
                anyChanged = true;
            }
        }

        if (heatLevel >= MinHeat && containedEnergy < PowerLimit)
        {
            int powerGen = getGenerationPower();
            containedEnergy = Math.min(containedEnergy + powerGen, PowerLimit);
            anyChanged = true;
        }

        if (burnTimeRemaining <= 0 && containedEnergy < PowerLimit)
        {
            if (inputs[0] != null)
            {
                currentItemBurnTime = burnTimeRemaining = TileEntityFurnace.getItemBurnTime(inputs[0]);
                timeInterval = 0;
                inputs[0].stackSize--;
                if (inputs[0].stackSize <= 0)
                    inputs[0] = null;
                anyChanged = true;
            }
        }

        int sendPower = Math.min(PowerTransferMax, containedEnergy);
        if (sendPower > 0)
        {
            IEnergyHandler[] handlers = new IEnergyHandler[6];
            int[] wantedSide = new int[6];
            int accepted = 0;
            for (EnumFacing neighbor : EnumFacing.VALUES)
            {
                TileEntity e = worldObj.getTileEntity(pos.offset(neighbor));
                EnumFacing from = neighbor.getOpposite();

                if(e == null)
                    continue;

                IEnergyHandler handler = null;
                if(e.hasCapability(CapabilityEnergy.ENERGY_HANDLER_CAPABILITY, from))
                {
                    handler = e.getCapability(CapabilityEnergy.ENERGY_HANDLER_CAPABILITY, from);
                }
                else if (e instanceof IEnergyReceiver)
                {
                    handler = RFWrapper.wrap(e, from);
                }

                if(handler != null)
                {
                    handlers[from.ordinal()] = handler;
                    int wanted = handler.insertEnergy(sendPower, true);
                    wantedSide[from.ordinal()] = wanted;
                    accepted += wanted;
                }
            }
            if (accepted > 0)
            {
                for (EnumFacing from : EnumFacing.VALUES)
                {
                    IEnergyHandler handler = handlers[from.ordinal()];
                    int wanted = wantedSide[from.ordinal()];
                    if (handler == null || wanted == 0)
                        continue;

                    int given = Math.min(Math.min(containedEnergy, wanted), wanted * accepted / sendPower);
                    int received = Math.min(given, handler.insertEnergy(given, false));
                    containedEnergy -= received;
                    if (containedEnergy <= 0)
                        break;
                }
                anyChanged = true;
            }
        }

        if (anyChanged)
            this.markDirty();
    }

    @Override
    public int getSizeInventory()
    {
        return SlotCount;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        if (index < 0 || index >= inputs.length)
            return null;
        return inputs[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (index < 0 || index >= inputs.length)
            return null;

        if (inputs[index] == null)
            return null;

        if (count > inputs[index].stackSize)
            count = inputs[index].stackSize;

        ItemStack result = inputs[index].splitStack(count);

        if (inputs[index].stackSize <= 0)
        {
            inputs[index] = null;
        }

        this.markDirty();

        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if (index < 0 || index >= inputs.length)
            return null;

        if (inputs[index] == null)
            return null;

        ItemStack itemstack = inputs[index];
        inputs[index] = null;
        return itemstack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index < 0 || index >= inputs.length)
            return;

        inputs[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
        {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public String getName()
    {
        return "container." + EnderRiftMod.MODID + ".generator";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        NBTTagList _outputs = compound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < _outputs.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound = _outputs.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;

            if (j >= 0 && j < inputs.length)
            {
                inputs[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
        }

        heatLevel = compound.getInteger("heatLevel");
        burnTimeRemaining = compound.getInteger("burnTimeRemaining");
        currentItemBurnTime = compound.getInteger("currentItemBurnTime");
        containedEnergy = compound.getInteger("powerLevel");
        timeInterval = compound.getInteger("timeInterval");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        NBTTagList _outputs = new NBTTagList();
        for (int i = 0; i < inputs.length; ++i)
        {
            if (inputs[i] != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                inputs[i].writeToNBT(nbttagcompound);
                _outputs.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", _outputs);

        compound.setInteger("heatLevel", heatLevel);
        compound.setInteger("burnTimeRemaining", burnTimeRemaining);
        compound.setInteger("currentItemBurnTime", currentItemBurnTime);
        compound.setInteger("powerLevel", containedEnergy);
        compound.setInteger("timeInterval", timeInterval);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return TileEntityFurnace.getItemBurnTime(stack) > 0;
    }

    @Override
    public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return burnTimeRemaining;
            case 1:
                return currentItemBurnTime;
            case 2:
                return containedEnergy;
            case 3:
                return heatLevel;
        }
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                burnTimeRemaining = value;
                break;
            case 1:
                currentItemBurnTime = value;
                break;
            case 2:
                containedEnergy = value;
                break;
            case 3:
                heatLevel = value;
                break;
            default:
                return;
        }
        this.markDirty();
    }

    @Override
    public int getFieldCount()
    {
        return 4;
    }

    public void clear()
    {
        for (int i = 0; i < inputs.length; ++i)
        {
            inputs[i] = null;
        }
        markDirty();
    }

    @Override
    public int extractEnergy(EnumFacing facing, int maxExtract, boolean simulate)
    {
        int powerToExtract = Math.min(containedEnergy, maxExtract);
        if (!simulate)
            containedEnergy -= powerToExtract;
        return powerToExtract;
    }

    @Override
    public int getEnergyStored(EnumFacing facing)
    {
        return containedEnergy;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing facing)
    {
        return PowerLimit;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing facing)
    {
        return true;
    }

    public boolean isBurning()
    {
        return burnTimeRemaining > 0;
    }

    public int getHeatValue()
    {
        return heatLevel;
    }

    public int getGenerationPower()
    {
        if (heatLevel < MinHeat)
            return 0;
        return Math.max(0, Math.round(PowerGenMin + (PowerGenMax - PowerGenMin) * (heatLevel - MinHeat) / (float) (MaxHeat - MinHeat)));
    }

    public int getContainedEnergy()
    {
        return containedEnergy;
    }
}
