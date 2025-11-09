package dev.gigaherz.enderrift.plugins;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import dev.gigaherz.enderrift.automation.driver.DriverBlockEntity;
import dev.gigaherz.enderrift.generator.GeneratorBlockEntity;
import dev.gigaherz.enderrift.rift.RiftBlock;
import dev.gigaherz.enderrift.rift.RiftBlockEntity;
import dev.gigaherz.enderrift.rift.StructureCornerBlockEntity;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.impl.ui.ItemStackElement;
import net.neoforged.neoforge.energy.IEnergyStorage;

@WailaPlugin
public class WailaProviders implements IWailaPlugin
{
    private static final ResourceLocation CONFIG_GENERATOR = EnderRiftMod.location("generator");
    private static final ResourceLocation CONFIG_RIFT = EnderRiftMod.location("rift");
    private static final ResourceLocation CONFIG_DRIVER = EnderRiftMod.location("driver");
    private static final ResourceLocation CONFIG_RF = EnderRiftMod.location("rf");
    private static final ResourceLocation CONFIG_NETWORK = EnderRiftMod.location("network");


    @Override
    public void register(IWailaCommonRegistration registrar)
    {
        {
            RiftTooltipProvider instance = new RiftTooltipProvider();
            registrar.registerBlockDataProvider(instance, StructureCornerBlockEntity.class);
            registrar.registerBlockDataProvider(instance, RiftBlockEntity.class);
        }

        {
            NetworkTooltipProvider instance = new NetworkTooltipProvider();
            registrar.registerBlockDataProvider(instance, AggregatorBlockEntity.class);
        }

        {
            DriverTooltipProvider instance = new DriverTooltipProvider();
            registrar.registerBlockDataProvider(instance, DriverBlockEntity.class);
        }

        {
            GeneratorTooltipProvider instance = new GeneratorTooltipProvider();
            registrar.registerBlockDataProvider(instance, GeneratorBlockEntity.class);
        }
    }

    public static class GeneratorTooltipProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
        {
            if (config.get(CONFIG_GENERATOR))
            {
                CompoundTag tag = accessor.getServerData();

                if (tag.getIntOr("powerGen",0) > 0)
                {
                    tooltip.add(Component.translatable("text.enderrift.generator.status.generating", tag.getInt("powerGen")));
                }
                else if (tag.getBooleanOr("isBurning", false))
                {
                    tooltip.add(Component.translatable("text.enderrift.generator.status.heating"));
                }
                else
                {
                    tooltip.add(Component.translatable("text.enderrift.generator.status.idle"));
                }

                tooltip.add(Component.translatable("text.enderrift.generator.heat", tag.getInt("heat")));

                if (config.get(CONFIG_RF))
                    tooltip.add(Component.translatable("text.enderrift.generator.energy", tag.getInt("energy"), GeneratorBlockEntity.POWER_LIMIT));
            }
        }

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor)
        {
            GeneratorBlockEntity rift = (GeneratorBlockEntity) accessor.getBlockEntity();

            tag.putBoolean("isBurning", rift.isBurning());
            tag.putInt("powerGen", rift.getGenerationPower());
            tag.putInt("energy", rift.getContainedEnergy());
            tag.putInt("heat", rift.getHeatValue());
        }

        @Override
        public ResourceLocation getUid()
        {
            return CONFIG_GENERATOR;
        }
    }

    public static class DriverTooltipProvider implements IComponentProvider<BlockAccessor>, IServerDataProvider<BlockAccessor>
    {

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor blockAccessor)
        {
            if (blockAccessor.getBlockEntity() instanceof DriverBlockEntity rift)
            {
                tag.putInt("energy", rift.getInternalBuffer().map(IEnergyStorage::getEnergyStored).orElse(0));
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
        {
            if (config.get(CONFIG_DRIVER))
            {
                CompoundTag tag = accessor.getServerData();

                if (config.get(CONFIG_RF))
                    tooltip.add(Component.translatable("text.enderrift.generator.energy", tag.getInt("energy"), DriverBlockEntity.POWER_LIMIT));
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return CONFIG_DRIVER;
        }
    }

    public static class NetworkTooltipProvider implements IComponentProvider<BlockAccessor>, IServerDataProvider<BlockAccessor>
    {
        @Override
        public void appendServerData(CompoundTag compoundTag, BlockAccessor accessor)
        {

        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
        {
            if (accessor.getBlockEntity() instanceof GraphObject<?> graph
                && graph.getGraph() instanceof Graph<?> network)
            {
                tooltip.add(Component.translatable("text.enderrift.network.size", network.getObjects().size()));
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return CONFIG_NETWORK;
        }
    }

    public static class RiftTooltipProvider
            implements IComponentProvider<BlockAccessor>, IServerDataProvider<BlockAccessor>
    {
        @Override
        public @Nullable Element getIcon(BlockAccessor accessor, IPluginConfig config, Element currentIcon)
        {
            return ItemStackElement.of(new ItemStack(accessor.getBlock()));
        }

        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor)
        {
            @Nullable
            RiftBlockEntity rift;

            rift = switch (accessor.getBlockEntity())
            {
                case StructureCornerBlockEntity structure -> structure.getParent().orElse(null);
                case RiftBlockEntity riftBlockEntity -> riftBlockEntity;
                case null, default -> null;
            };

            if (rift != null)
            {
                var inv = rift.getInventory();
                if (inv != null) tag.putInt("usedSlots", inv.getSlots());
                tag.putBoolean("isFormed", rift.getBlockState().getValue(RiftBlock.ASSEMBLED));
                tag.putBoolean("isPowered", rift.isPowered());
                if (rift.getRiftId() != null) tag.putString("riftId", rift.getRiftId().toString());
                tag.putInt("energy", rift.getEnergyBuffer().map(IEnergyStorage::getEnergyStored).orElse(0));
            }
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config)
        {
            if (config.get(CONFIG_RIFT) && accessor.getBlock() == EnderRiftMod.STRUCTURE_CORNER.get())
            {
                CompoundTag tag = accessor.getServerData();

                if (tag != null && tag.contains("isFormed"))
                {
                    tooltip.add(Component.translatable("text.enderrift.rift.is_formed", tag.getBoolean("isFormed")));
                    tooltip.add(Component.translatable("text.enderrift.rift.is_powered", tag.getBoolean("isPowered")));
                    if (tag.getBooleanOr("isFormed", false))
                    {
                        tooltip.add(Component.translatable("text.enderrift.rift.rift_id", tag.getInt("riftId")));
                        if (config.get(CONFIG_RF))
                            tooltip.add(Component.translatable("text.enderrift.rift.rf", tag.getInt("energy"), RiftBlockEntity.BUFFER_POWER));
                    }
                    tooltip.add(Component.translatable("text.enderrift.rift.used_slots", tag.getInt("usedSlots")));
                }
                else
                {
                    tooltip.add(Component.translatable("text.enderrift.rift.is_formed", false));
                }
            }
        }

        @Override
        public ResourceLocation getUid()
        {
            return CONFIG_RIFT;
        }
    }
}
