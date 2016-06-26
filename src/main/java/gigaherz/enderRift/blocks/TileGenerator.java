package gigaherz.enderRift.blocks;

import gigaherz.capabilities.api.energy.CapabilityEnergy;
import gigaherz.capabilities.api.energy.EnergyBuffer;
import gigaherz.capabilities.api.energy.IEnergyHandler;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.compatibility.tesla.TeslaControllerBase;
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

    final ItemStackHandler fuelSlot = new ItemStackHandler(SlotCount)
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
    int timeInterval;

    EnergyBuffer energyCapability = new EnergyBuffer(PowerLimit);

    private Capability teslaProducerCap;
    private Object teslaProducerInstance;

    private Capability teslaHolderCap;
    private Object teslaHolderInstance;

    public TileGenerator()
    {
        teslaProducerCap = TeslaControllerBase.PRODUCER.getCapability();
        teslaProducerInstance = TeslaControllerBase.PRODUCER.createInstance(energyCapability);
        teslaHolderCap = TeslaControllerBase.HOLDER.getCapability();
        teslaHolderInstance = TeslaControllerBase.HOLDER.createInstance(energyCapability);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
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
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY_HANDLER_CAPABILITY)
            return CapabilityEnergy.ENERGY_HANDLER_CAPABILITY.cast(energyCapability);
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(fuelSlot);
        if (teslaProducerCap != null && capability == teslaProducerCap)
            return (T)teslaProducerInstance;
        if (teslaHolderCap != null && capability == teslaHolderCap)
            return (T)teslaHolderInstance;
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

        int minHeatLevel = 0;
        int heatInterval = HeatInterval;
        if (worldObj.getBlockState(pos.down()).getBlock() == Blocks.LAVA)
        {
            minHeatLevel = MinHeat - 1;
            if (heatLevel < minHeatLevel)
                heatInterval = Math.max(1, heatInterval / 2);
        }

        if (timeInterval < HeatInterval)
            timeInterval++;

        if (burnTimeRemaining > 0)
        {
            burnTimeRemaining -= Math.max(1, heatLevel / MinHeat);
            if (burnTimeRemaining <= 0)
                timeInterval = 0;
            if (timeInterval >= heatInterval && heatLevel < MaxHeat)
            {
                timeInterval = 0;
                heatLevel++;
                anyChanged = true;
            }
        }
        else if (heatLevel > minHeatLevel)
        {
            if (timeInterval >= HeatInterval)
            {
                timeInterval = 0;
                heatLevel--;
                anyChanged = true;
            }
        }
        else if (minHeatLevel > 0 && heatLevel < minHeatLevel)
        {
            if (timeInterval >= HeatInterval)
            {
                timeInterval = 0;
                heatLevel++;
                anyChanged = true;
            }
        }

        if (heatLevel >= MinHeat && energyCapability.getEnergy() < PowerLimit)
        {
            int powerGen = getGenerationPower();
            energyCapability.setEnergy(Math.min(energyCapability.getEnergy() + powerGen, PowerLimit));
            anyChanged = true;
        }

        if (burnTimeRemaining <= 0 && energyCapability.getEnergy() < PowerLimit)
        {
            ItemStack stack = fuelSlot.getStackInSlot(0);
            if (stack != null)
            {
                currentItemBurnTime = burnTimeRemaining = TileEntityFurnace.getItemBurnTime(fuelSlot.getStackInSlot(0));
                timeInterval = 0;
                stack.stackSize--;
                if (stack.stackSize <= 0)
                    fuelSlot.setStackInSlot(0, stack.getItem().getContainerItem(stack));
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    private boolean transferPower()
    {
        boolean anyChanged = false;

        int sendPower = Math.min(PowerTransferMax, energyCapability.getEnergy());
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

                IEnergyHandler handler;
                if (e.hasCapability(CapabilityEnergy.ENERGY_HANDLER_CAPABILITY, from))
                {
                    handler = e.getCapability(CapabilityEnergy.ENERGY_HANDLER_CAPABILITY, from);
                }
                else
                {
                    handler = TeslaControllerBase.CONSUMER.wrapReverse(e, from);
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

                    int given = Math.min(Math.min(energyCapability.getEnergy(), wanted), wanted * accepted / sendPower);
                    int received = Math.min(given, handler.insertEnergy(given, false));
                    energyCapability.setEnergy(energyCapability.getEnergy() - received);
                    if (energyCapability.getEnergy() <= 0)
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
        CapabilityEnergy.ENERGY_HANDLER_CAPABILITY.readNBT(energyCapability, null, compound.getCompoundTag("storedEnergy"));
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(fuelSlot, null, compound.getTagList("fuelSlot", Constants.NBT.TAG_COMPOUND));

        if (compound.hasKey("powerLevel", Constants.NBT.TAG_INT))
            energyCapability.insertEnergy(compound.getInteger("powerLevel"), false);

        if (compound.hasKey("Items", Constants.NBT.TAG_LIST))
        {
            NBTTagList _outputs = compound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < _outputs.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = _outputs.getCompoundTagAt(i);
                int j = nbttagcompound.getByte("Slot") & 255;

                if (j >= 0 && j < fuelSlot.getSlots())
                {
                    fuelSlot.setStackInSlot(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound = super.writeToNBT(compound);

        compound.setInteger("heatLevel", heatLevel);
        compound.setInteger("burnTimeRemaining", burnTimeRemaining);
        compound.setInteger("currentItemBurnTime", currentItemBurnTime);
        compound.setInteger("timeInterval", timeInterval);
        compound.setTag("storedEnergy", CapabilityEnergy.ENERGY_HANDLER_CAPABILITY.writeNBT(energyCapability, null));
        compound.setTag("fuelSlot", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(fuelSlot, null));

        return compound;
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    public int[] getFields()
    {
        return new int[]{ burnTimeRemaining, currentItemBurnTime, energyCapability.getEnergy(), heatLevel };
    }

    public void setFields(int[] values)
    {
        burnTimeRemaining = values[0];
        currentItemBurnTime = values[1];
        energyCapability.setEnergy(values[2]);
        heatLevel = values[3];
        this.markDirty();
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
        return energyCapability.getEnergy();
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
