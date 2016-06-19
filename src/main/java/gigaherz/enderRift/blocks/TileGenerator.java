package gigaherz.enderRift.blocks;

import gigaherz.capabilities.api.energy.CapabilityEnergy;
import gigaherz.capabilities.api.energy.EnergyBuffer;
import gigaherz.capabilities.api.energy.IEnergyHandler;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileGenerator extends TileEntity
        implements ITickable
{
    public static final int SlotCount = 1;
    public static final int PowerLimit = 100000;
    public static final int MinHeat = 100;
    public static final int MaxHeat = 1000;
    public static final int PowerGenMin = 20;
    public static final int PowerGenMax = 200;
    public static final int HeatInterval = 20;
    public static final int PowerTransferMax = 800;

    final ItemStackHandler inputs = new ItemStackHandler(SlotCount)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            markDirty();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (TileEntityFurnace.getItemBurnTime(stack) <= 0)
                return stack;

            return super.insertItem(slot, stack, simulate);
        }
    };

    int heatLevel;
    int burnTimeRemaining;
    int currentItemBurnTime;
    int containedEnergy;
    int timeInterval;

    EnergyBuffer energyCapability = new EnergyBuffer(PowerLimit);

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return true;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return CapabilityEnergy.ENERGY_HANDLER_CAPABILITY.cast(energyCapability);
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inputs);
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

        boolean anyChanged;

        anyChanged = updateGeneration();
        anyChanged |= transferPower();

        if (anyChanged)
            this.markDirty();
    }

    private boolean updateGeneration()
    {
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
            ItemStack stack = inputs.getStackInSlot(0);
            if (stack != null)
            {
                currentItemBurnTime = burnTimeRemaining = TileEntityFurnace.getItemBurnTime(inputs.getStackInSlot(0));
                timeInterval = 0;
                stack.stackSize--;
                if (stack.stackSize <= 0)
                    inputs.setStackInSlot(0, stack.getItem().getContainerItem(stack));
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    private boolean transferPower()
    {
        boolean anyChanged = false;

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

                if (e == null)
                    continue;

                IEnergyHandler handler = null;
                if (e.hasCapability(CapabilityEnergy.ENERGY_HANDLER_CAPABILITY, from))
                {
                    handler = e.getCapability(CapabilityEnergy.ENERGY_HANDLER_CAPABILITY, from);
                }

                if (handler != null)
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

        return anyChanged;
    }

    public String getName()
    {
        return "container." + EnderRiftMod.MODID + ".generator";
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

            if (j >= 0 && j < inputs.getSlots())
            {
                inputs.setStackInSlot(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
            }
        }

        heatLevel = compound.getInteger("heatLevel");
        burnTimeRemaining = compound.getInteger("burnTimeRemaining");
        currentItemBurnTime = compound.getInteger("currentItemBurnTime");
        containedEnergy = compound.getInteger("powerLevel");
        timeInterval = compound.getInteger("timeInterval");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        NBTTagList _outputs = new NBTTagList();
        for (int i = 0; i < inputs.getSlots(); ++i)
        {
            ItemStack stack = inputs.getStackInSlot(i);
            if (stack != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                stack.writeToNBT(nbttagcompound);
                _outputs.appendTag(nbttagcompound);
            }
        }

        compound.setTag("Items", _outputs);

        compound.setInteger("heatLevel", heatLevel);
        compound.setInteger("burnTimeRemaining", burnTimeRemaining);
        compound.setInteger("currentItemBurnTime", currentItemBurnTime);
        compound.setInteger("powerLevel", containedEnergy);
        compound.setInteger("timeInterval", timeInterval);

        return compound;
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

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

    public int getFieldCount()
    {
        return 4;
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

    public IItemHandler inventory()
    {
        return inputs;
    }
}
