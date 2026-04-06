package dev.gigaherz.enderrift.generator;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.iface.InterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

@EventBusSubscriber(modid = EnderRiftMod.MODID)
public class GeneratorBlockEntity extends BlockEntity
{
    @SubscribeEvent
    private static void registerCapability(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                EnderRiftMod.GENERATOR_BLOCK_ENTITY.get(),
                (be, context) -> be.energyBuffer
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                EnderRiftMod.GENERATOR_BLOCK_ENTITY.get(),
                (be, context) -> be.fuelSlotWrapper
        );
    }

    public static final int POWER_LIMIT = 100000;
    public static final int MIN_HEAT = 100;
    public static final int MAX_HEAT = 1000;
    public static final int POWERGEN_MIN = 20;
    public static final int POWERGEN_MAX = 200;
    public static final int HEAT_INTERVAL = 20;
    public static final int POWER_TRANSFER_MAX = 800;

    private final SimpleEnergyHandler energyBuffer = new SimpleEnergyHandler(POWER_LIMIT)
    {
        @Override
        public int insert(int amount, TransactionContext transaction)
        {
            return 0;
        }

        @Override
        protected void onEnergyChanged(int previousAmount)
        {
            GeneratorBlockEntity.this.setChanged();
        }
    };

    private final FuelSlotContainer fuelSlot = new FuelSlotContainer();

    private final ResourceHandler<ItemResource> fuelSlotWrapper = VanillaContainerWrapper.of(fuelSlot);


    public int heatLevel;
    public int burnTimeRemaining;
    public int currentItemBurnTime;
    public int timeInterval;

    public GeneratorBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public void tick()
    {
        if (level.isClientSide())
            return;

        boolean anyChanged;

        anyChanged = updateGeneration();
        anyChanged |= transferPower();

        if (anyChanged)
            this.setChanged();
    }

    private boolean updateGeneration()
    {
        boolean anyChanged = false;

        int minHeatLevel = 0;
        int heatInterval = HEAT_INTERVAL;
        if (level.getBlockState(worldPosition.below()).getBlock() == Blocks.LAVA)
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

        if (heatLevel >= MIN_HEAT && energyBuffer.getAmountAsInt() < POWER_LIMIT)
        {
            int powerGen = getGenerationPower();
            energyBuffer.set(Math.min(energyBuffer.getAmountAsInt() + powerGen, POWER_LIMIT));
            anyChanged = true;
        }

        if (burnTimeRemaining <= 0 && energyBuffer.getAmountAsInt() < POWER_LIMIT)
        {
            var stack = fuelSlot.getItem(0);
            var count = stack.getCount();
            if (count > 0)
            {
                currentItemBurnTime = burnTimeRemaining = stack.getBurnTime(null, level.fuelValues());
                timeInterval = 0;
                if (stack.getCount() == 1)
                {
                    var r = stack.getItem().getCraftingRemainder(stack);
                    if (r != null)
                    {
                        fuelSlot.setItem(0, r.create());
                    }
                    else
                    {
                        fuelSlot.setItem(0, ItemStack.EMPTY);
                    }
                }
                else
                    stack.shrink(1);
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    private boolean transferPower()
    {
        boolean anyChanged = false;

        int sendPower = Math.min(POWER_TRANSFER_MAX, energyBuffer.getAmountAsInt());
        if (sendPower > 0)
        {
            EnergyHandler[] handlers = new EnergyHandler[6];
            int[] wantedSide = new int[6];
            int accepted = 0;

            for (Direction neighbor : Direction.values())
            {
                BlockEntity e = level.getBlockEntity(worldPosition.relative(neighbor));
                Direction from = neighbor.getOpposite();

                if (e == null)
                    continue;

                EnergyHandler handler = e.getLevel().getCapability(Capabilities.Energy.BLOCK, e.getBlockPos(), e.getBlockState(), e, from);
                if (handler != null)
                {
                    handlers[from.ordinal()] = handler;

                    try (var transaction = Transaction.openRoot())
                    {
                        int wanted = handler.insert(sendPower, transaction);
                        if (wanted > 0)
                        {
                            wantedSide[from.ordinal()] = wanted;
                            accepted += wanted;
                        }
                    }
                }
            }

            if (accepted > 0)
            {
                try (var transaction = Transaction.openRoot())
                {
                    for (Direction from : Direction.values())
                    {
                        EnergyHandler handler = handlers[from.ordinal()];
                        int wanted = wantedSide[from.ordinal()];
                        if (handler == null || wanted == 0)
                            continue;

                        int given = Math.min(Math.min(energyBuffer.getAmountAsInt(), wanted), wanted * accepted / sendPower);
                        int received = Math.min(given, handler.insert(given, transaction));
                        energyBuffer.set(energyBuffer.getAmountAsInt() - received);
                        if (energyBuffer.getAmountAsInt() <= 0)
                            break;
                    }
                    transaction.commit();
                }
                anyChanged = true;
            }
        }

        return anyChanged;
    }

    @Override
    public void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

        heatLevel = input.getIntOr("heatLevel", 0);
        burnTimeRemaining = input.getIntOr("burnTimeRemaining", 0);
        currentItemBurnTime = input.getIntOr("currentItemBurnTime", 0);
        timeInterval = input.getIntOr("timeInterval",0);
        energyBuffer.deserialize(input.childOrEmpty("storedEnergy"));
        fuelSlot.deserialize(input.childOrEmpty("fuelSlot"));
    }

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

        output.putInt("heatLevel", heatLevel);
        output.putInt("burnTimeRemaining", burnTimeRemaining);
        output.putInt("currentItemBurnTime", currentItemBurnTime);
        output.putInt("timeInterval", timeInterval);
        energyBuffer.serialize(output.child("storedEnergy"));
        fuelSlot.serialize(output.child("fuelSlot"));
    }

    @Override
    public void handleUpdateTag(ValueInput input)
    {
        super.loadAdditional(input);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        super.preRemoveSideEffects(pos, state);

        InterfaceBlockEntity.dropItems(level, pos, this.inventory());
    }

    public boolean isUseableByPlayer(Player player)
    {
        return level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    private final ContainerData fields = new ContainerData()
    {
        @Override
        public int get(int field)
        {
            return switch (field)
            {
                case 0 -> burnTimeRemaining;
                case 1 -> currentItemBurnTime;
                case 2 -> energyBuffer.getAmountAsInt() & 0xFFFF;
                case 3 -> energyBuffer.getAmountAsInt() >> 16;
                case 4 -> heatLevel;
                default -> 0;
            };
        }

        @Override
        public void set(int field, int value)
        {
            switch (field)
            {
                case 0 -> burnTimeRemaining = value;
                case 1 -> currentItemBurnTime = value;
                case 2 -> energyBuffer.set((energyBuffer.getAmountAsInt() & 0xFFFF0000) | (value & 0xFFFF));
                case 3 -> energyBuffer.set((energyBuffer.getAmountAsInt() & 0xFFFF) | (value << 16));
                case 4 -> heatLevel = value;
            }
        }

        @Override
        public int getCount()
        {
            return 5;
        }
    };

    public ContainerData getFields()
    {
        return fields;
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

    public static int calculateGenerationPower(int heatLevel)
    {
        if (heatLevel < MIN_HEAT)
            return 0;
        return Math.max(0, Math.round(POWERGEN_MIN + (POWERGEN_MAX - POWERGEN_MIN) * (heatLevel - MIN_HEAT) / (float) (MAX_HEAT - MIN_HEAT)));
    }

    public int getContainedEnergy()
    {
        return energyBuffer.getAmountAsInt();
    }

    public Container inventory()
    {
        return fuelSlot;
    }

    public ResourceHandler<ItemResource> transferInventory()
    {
        return fuelSlotWrapper;
    }

    public int getCurrentItemBurnTime()
    {
        return currentItemBurnTime;
    }

    public int getBurnTimeRemaining()
    {
        return burnTimeRemaining;
    }

    public static void tickStatic(Level level, BlockPos blockPos, BlockState blockState, GeneratorBlockEntity te)
    {
        te.tick();
    }

    private class FuelSlotContainer extends SimpleContainer implements ValueIOSerializable
    {
        public FuelSlotContainer()
        {
            super(1);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack itemStack)
        {
            if (itemStack.getBurnTime(null, level.fuelValues()) <= 0)
                return false;

            return super.canPlaceItem(slot, itemStack);
        }

        @Override
        public boolean canAddItem(ItemStack itemStack)
        {
            if (itemStack.getBurnTime(null, level.fuelValues()) <= 0)
                return false;

            return super.canAddItem(itemStack);
        }

        @Override
        public void setChanged()
        {
            super.setChanged();
            GeneratorBlockEntity.this.setChanged();
        }

        @Override
        public void serialize(ValueOutput output)
        {
            output.store("Stack", ItemStack.OPTIONAL_CODEC, getItem(0));
        }

        @Override
        public void deserialize(ValueInput input)
        {
            setItem(0, input.read("Stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        }
    }
}