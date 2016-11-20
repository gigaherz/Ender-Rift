package gigaherz.enderRift.generator;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.common.EnergyBuffer;
import gigaherz.enderRift.plugins.tesla.TeslaControllerBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class TileGenerator extends TileEntity
        implements ITickable
{
    public static final int SLOT_COUNT = 1;
    public static final int POWER_LIMIT = 100000;
    public static final int MIN_HEAT = 100;
    public static final int MAX_HEAT = 1000;
    public static final int POWERGEN_MIN = 20;
    public static final int POWERGEN_MAX = 200;
    public static final int HEAT_INTERVAL = 20;
    public static final int POWER_TRANSFER_MAX = 800;

    private EnergyBuffer energyCapability = new EnergyBuffer(POWER_LIMIT);

    private final ItemStackHandler fuelSlot = new ItemStackHandler(SLOT_COUNT)
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

    public int heatLevel;
    public int burnTimeRemaining;
    public int currentItemBurnTime;
    public int timeInterval;

    private Capability teslaProducerCap;
    private Capability teslaHolderCap;

    private Object teslaProducerInstance;
    private Object teslaHolderInstance;

    public TileGenerator()
    {
        teslaProducerCap = TeslaControllerBase.PRODUCER.getCapability();
        teslaProducerInstance = TeslaControllerBase.PRODUCER.createInstance(energyCapability);
        teslaHolderCap = TeslaControllerBase.HOLDER.getCapability();
        teslaHolderInstance = TeslaControllerBase.HOLDER.createInstance(energyCapability);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
            return true;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        if (teslaProducerCap != null && capability == teslaProducerCap)
            return true;
        if (teslaHolderCap != null && capability == teslaHolderCap)
            return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
            return (T)energyCapability;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T)fuelSlot;
        if (teslaProducerCap != null && capability == teslaProducerCap)
            return (T) teslaProducerInstance;
        if (teslaHolderCap != null && capability == teslaHolderCap)
            return (T) teslaHolderInstance;
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void update()
    {
        if (world.isRemote)
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

        int minHeatLevel = 0;
        int heatInterval = HEAT_INTERVAL;
        if (world.getBlockState(pos.down()).getBlock() == Blocks.LAVA)
        {
            minHeatLevel = MIN_HEAT - 1;
            if (heatLevel < minHeatLevel)
                heatInterval = Math.max(1, heatInterval / 2);
        }

        if (timeInterval < HEAT_INTERVAL)
            timeInterval++;

        if (burnTimeRemaining > 0)
        {
            burnTimeRemaining -= Math.max(1, heatLevel / MIN_HEAT);
            if (burnTimeRemaining <= 0)
                timeInterval = 0;
            if (timeInterval >= heatInterval && heatLevel < MAX_HEAT)
            {
                timeInterval = 0;
                heatLevel++;
                anyChanged = true;
            }
        }
        else if (heatLevel > minHeatLevel)
        {
            if (timeInterval >= HEAT_INTERVAL)
            {
                timeInterval = 0;
                heatLevel--;
                anyChanged = true;
            }
        }
        else if (minHeatLevel > 0 && heatLevel < minHeatLevel)
        {
            if (timeInterval >= HEAT_INTERVAL)
            {
                timeInterval = 0;
                heatLevel++;
                anyChanged = true;
            }
        }

        if (heatLevel >= MIN_HEAT && energyCapability.getEnergyStored() < POWER_LIMIT)
        {
            int powerGen = getGenerationPower();
            energyCapability.setEnergy(Math.min(energyCapability.getEnergyStored() + powerGen, POWER_LIMIT));
            anyChanged = true;
        }

        if (burnTimeRemaining <= 0 && energyCapability.getEnergyStored() < POWER_LIMIT)
        {
            ItemStack stack = fuelSlot.getStackInSlot(0);
            if (stack.getCount() > 0)
            {
                currentItemBurnTime = burnTimeRemaining = TileEntityFurnace.getItemBurnTime(fuelSlot.getStackInSlot(0));
                timeInterval = 0;
                stack.shrink(1);
                if (stack.getCount() <= 0)
                    fuelSlot.setStackInSlot(0, stack.getItem().getContainerItem(stack));
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    private boolean transferPower()
    {
        boolean anyChanged = false;

        int sendPower = Math.min(POWER_TRANSFER_MAX, energyCapability.getEnergyStored());
        if (sendPower > 0)
        {
            IEnergyStorage[] handlers = new IEnergyStorage[6];
            int[] wantedSide = new int[6];
            int accepted = 0;

            for (EnumFacing neighbor : EnumFacing.VALUES)
            {
                TileEntity e = world.getTileEntity(pos.offset(neighbor));
                EnumFacing from = neighbor.getOpposite();

                if (e == null)
                    continue;

                IEnergyStorage handler = null;
                if (e.hasCapability(CapabilityEnergy.ENERGY, from))
                {
                    handler = e.getCapability(CapabilityEnergy.ENERGY, from);
                    if (!handler.canReceive())
                        handler = null;
                }

                if (handler == null)
                {
                    handler = TeslaControllerBase.CONSUMER.wrapReverse(e, from);
                }

                if (handler != null)
                {
                    handlers[from.ordinal()] = handler;
                    int wanted = handler.receiveEnergy(sendPower, true);
                    wantedSide[from.ordinal()] = wanted;
                    accepted += wanted;
                }
            }

            if (accepted > 0)
            {
                for (EnumFacing from : EnumFacing.VALUES)
                {
                    IEnergyStorage handler = handlers[from.ordinal()];
                    int wanted = wantedSide[from.ordinal()];
                    if (handler == null || wanted == 0)
                        continue;

                    int given = Math.min(Math.min(energyCapability.getEnergyStored(), wanted), wanted * accepted / sendPower);
                    int received = Math.min(given, handler.receiveEnergy(given, false));
                    energyCapability.setEnergy(energyCapability.getEnergyStored() - received);
                    if (energyCapability.getEnergyStored() <= 0)
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

        heatLevel = compound.getInteger("heatLevel");
        burnTimeRemaining = compound.getInteger("burnTimeRemaining");
        currentItemBurnTime = compound.getInteger("currentItemBurnTime");
        timeInterval = compound.getInteger("timeInterval");
        CapabilityEnergy.ENERGY.readNBT(energyCapability, null, compound.getTag("storedEnergy"));
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(fuelSlot, null, compound.getTagList("fuelSlot", Constants.NBT.TAG_COMPOUND));

        if (compound.hasKey("Items", Constants.NBT.TAG_LIST))
        {
            NBTTagList _outputs = compound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < _outputs.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = _outputs.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;

                if (j >= 0 && j < fuelSlot.getSlots())
                {
                    fuelSlot.setStackInSlot(j, new ItemStack(nbttagcompound));
                }
            }
        }
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        compound.setInteger("heatLevel", heatLevel);
        compound.setInteger("burnTimeRemaining", burnTimeRemaining);
        compound.setInteger("currentItemBurnTime", currentItemBurnTime);
        compound.setInteger("timeInterval", timeInterval);
        compound.setTag("storedEnergy", CapabilityEnergy.ENERGY.writeNBT(energyCapability, null));
        compound.setTag("fuelSlot", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(fuelSlot, null));

        return compound;
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    public int[] getFields()
    {
        return new int[]{burnTimeRemaining, currentItemBurnTime, energyCapability.getEnergyStored(), heatLevel};
    }

    public void setFields(int[] values)
    {
        burnTimeRemaining = values[0];
        currentItemBurnTime = values[1];
        energyCapability.setEnergy(values[2]);
        heatLevel = values[3];
        //this.markDirty();
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
        if (heatLevel < MIN_HEAT)
            return 0;
        return Math.max(0, Math.round(POWERGEN_MIN + (POWERGEN_MAX - POWERGEN_MIN) * (heatLevel - MIN_HEAT) / (float) (MAX_HEAT - MIN_HEAT)));
    }

    public int getContainedEnergy()
    {
        return energyCapability.getEnergyStored();
    }

    public IItemHandler inventory()
    {
        return fuelSlot;
    }

    public int getCurrentItemBurnTime()
    {
        return currentItemBurnTime;
    }

    public int getBurnTimeRemaining()
    {
        return burnTimeRemaining;
    }
}
