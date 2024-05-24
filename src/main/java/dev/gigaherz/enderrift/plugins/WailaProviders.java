package dev.gigaherz.enderrift.plugins;
/*
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AggregatorBlockEntity;
import dev.gigaherz.enderrift.automation.driver.DriverBlock;
import dev.gigaherz.enderrift.automation.driver.DriverBlockEntity;
import dev.gigaherz.enderrift.generator.GeneratorBlock;
import dev.gigaherz.enderrift.generator.GeneratorBlockEntity;
import dev.gigaherz.enderrift.rift.RiftBlock;
import dev.gigaherz.enderrift.rift.RiftBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

import java.util.List;
import java.util.Optional;

@WailaPlugin
public class WailaProviders implements IWailaPlugin
{
    private static final ResourceLocation CONFIG_GENERATOR = EnderRiftMod.location("generator");
    private static final ResourceLocation CONFIG_RIFT = EnderRiftMod.location("rift");
    private static final ResourceLocation CONFIG_DRIVER = EnderRiftMod.location("driver");
    private static final ResourceLocation CONFIG_RF = EnderRiftMod.location("rf");


    @Override
    public void register(IWailaCommonRegistration registrar)
    {
        registrar.addConfig(CONFIG_GENERATOR, true);
        registrar.addConfig(CONFIG_RIFT, true);
        registrar.addConfig(CONFIG_DRIVER, true);
        registrar.addConfig(CONFIG_RF, true);

        {
            RiftTooltipProvider instance = new RiftTooltipProvider();
            registrar.registerStackProvider(instance, StructureBlockEntity.class);
            registrar.registerBlockDataProvider(instance, StructureBlockEntity.class);
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, StructureBlock.class);
            registrar.registerBlockDataProvider(instance, RiftBlockEntity.class);
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, RiftBlock.class);
        }

        {
            NetworkTooltipProvider instance = new NetworkTooltipProvider();
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, AggregatorBlockEntity.class);
        }

        {
            DriverTooltipProvider instance = new DriverTooltipProvider();
            registrar.registerBlockDataProvider(instance, DriverBlockEntity.class);
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, DriverBlock.class);
        }

        {
            GeneratorTooltipProvider instance = new GeneratorTooltipProvider();
            registrar.registerBlockDataProvider(instance, GeneratorBlockEntity.class);
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, GeneratorBlock.class);
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

                if (tag.getInt("powerGen") > 0)
                {
                    tooltip.add(Component.translatable("text.enderrift.generator.status.generating", tag.getInt("powerGen")));
                }
                else if (tag.getBoolean("isBurning"))
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

    public static class DriverTooltipProvider implements IComponentProvider, IServerDataProvider<TileEntity>
    {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config)
        {
            if (config.get(CONFIG_DRIVER))
            {
                CompoundNBT tag = accessor.getServerData();

                if (config.get(CONFIG_RF))
                    tooltip.add(Component.translatable("text.enderrift.generator.energy", tag.getInt("energy"), DriverTileEntity.POWER_LIMIT));
            }
        }

        @Override
        public void appendServerData(CompoundNBT tag, ServerPlayerEntity serverPlayerEntity, World world, TileEntity tileEntity)
        {
            DriverTileEntity rift = (DriverTileEntity) tileEntity;

            tag.putInt("energy", rift.getInternalBuffer().map(IEnergyStorage::getEnergyStored).orElse(0));
        }
    }

    public static class NetworkTooltipProvider implements IComponentProvider
    {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config)
        {
            Graph network = ((GraphObject) accessor.getTileEntity()).getGraph();
            if (network != null)
            {
                tooltip.add(Component.translatable("text.enderrift.network.size", network.getObjects().size()));
            }
        }
    }

    public static class RiftTooltipProvider
            implements IComponentProvider, IServerDataProvider<TileEntity>
    {
        @Override
        public ItemStack getStack(IDataAccessor accessor, IPluginConfig config)
        {
            return new ItemStack(accessor.getBlock());
        }

        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config)
        {
            if (config.get(CONFIG_RIFT) && (accessor.getBlock() != EnderRiftMod.STRUCTURE.get() || accessor.getBlockState().get(StructureBlock.TYPE1) == StructureBlock.Type1.CORNER))
            {
                CompoundNBT tag = accessor.getServerData();

                if (tag != null && tag.contains("isFormed"))
                {
                    tooltip.add(Component.translatable("text.enderrift.rift.is_formed", tag.getBoolean("isFormed")));
                    tooltip.add(Component.translatable("text.enderrift.rift.is_powered", tag.getBoolean("isPowered")));
                    if (tag.getBoolean("isFormed"))
                    {
                        tooltip.add(Component.translatable("text.enderrift.rift.rift_id", tag.getInt("riftId")));
                        if (config.get(CONFIG_RF))
                            tooltip.add(Component.translatable("text.enderrift.rift.rf", tag.getInt("energy"), RiftTileEntity.BUFFER_POWER));
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
        public void appendServerData(CompoundNBT tag, ServerPlayerEntity serverPlayerEntity, World world, TileEntity tileEntity)
        {
            Optional<RiftTileEntity> riftMaybe;

            if (tileEntity instanceof StructureTileEntity)
            {
                riftMaybe = ((StructureTileEntity) tileEntity).getParent();
            }
            else
            {
                riftMaybe = Optional.of((RiftTileEntity) tileEntity);
            }

            riftMaybe.ifPresent(rift -> {
                tag.putInt("usedSlots", rift.countInventoryStacks());
                tag.putBoolean("isFormed", rift.getBlockState().get(RiftBlock.ASSEMBLED));
                tag.putBoolean("isPowered", rift.isPowered());
                tag.putInt("riftId", rift.getRiftId());
                tag.putInt("energy", rift.getEnergyBuffer().map(IEnergyStorage::getEnergyStored).orElse(0));
            });
        }
    }
}
*/