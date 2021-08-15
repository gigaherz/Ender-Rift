package dev.gigaherz.enderrift.plugins;
/*
import EnderRiftMod;
import AggregatorTileEntity;
import DriverBlock;
import DriverTileEntity;
import GeneratorBlock;
import GeneratorTileEntity;
import RiftBlock;
import RiftTileEntity;
import StructureBlock;
import StructureTileEntity;
import dev.gigaherz.enderrift.graph3.Graph;
import dev.gigaherz.enderrift.graph3.GraphObject;
import mcp.mobius.waila.api.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;

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
    public void register(IRegistrar registrar)
    {
        registrar.addConfig(CONFIG_GENERATOR, true);
        registrar.addConfig(CONFIG_RIFT, true);
        registrar.addConfig(CONFIG_DRIVER, true);
        registrar.addConfig(CONFIG_RF, true);

        {
            RiftTooltipProvider instance = new RiftTooltipProvider();
            registrar.registerStackProvider(instance, StructureTileEntity.class);
            registrar.registerBlockDataProvider(instance, StructureTileEntity.class);
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, StructureBlock.class);
            registrar.registerBlockDataProvider(instance, RiftTileEntity.class);
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, RiftBlock.class);
        }

        {
            NetworkTooltipProvider instance = new NetworkTooltipProvider();
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, AggregatorTileEntity.class);
        }

        {
            DriverTooltipProvider instance = new DriverTooltipProvider();
            registrar.registerBlockDataProvider(instance, DriverTileEntity.class);
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, DriverBlock.class);
        }

        {
            GeneratorTooltipProvider instance = new GeneratorTooltipProvider();
            registrar.registerBlockDataProvider(instance, GeneratorTileEntity.class);
            registrar.registerComponentProvider(instance, TooltipPosition.BODY, GeneratorBlock.class);
        }
    }

    public static class GeneratorTooltipProvider implements IComponentProvider, IServerDataProvider<TileEntity>
    {
        @Override
        public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config)
        {
            if (config.get(CONFIG_GENERATOR))
            {
                CompoundNBT tag = accessor.getServerData();

                if (tag.getInt("powerGen") > 0)
                {
                    tooltip.add(new TranslationTextComponent("text.enderrift.generator.status.generating", tag.getInt("powerGen")));
                }
                else if (tag.getBoolean("isBurning"))
                {
                    tooltip.add(new TranslationTextComponent("text.enderrift.generator.status.heating"));
                }
                else
                {
                    tooltip.add(new TranslationTextComponent("text.enderrift.generator.status.idle"));
                }

                tooltip.add(new TranslationTextComponent("text.enderrift.generator.heat", tag.getInt("heat")));

                if (config.get(CONFIG_RF))
                    tooltip.add(new TranslationTextComponent("text.enderrift.generator.energy", tag.getInt("energy"), GeneratorTileEntity.POWER_LIMIT));
            }
        }

        @Override
        public void appendServerData(CompoundNBT tag, ServerPlayerEntity serverPlayerEntity, World world, TileEntity tileEntity)
        {
            GeneratorTileEntity rift = (GeneratorTileEntity) tileEntity;

            tag.putBoolean("isBurning", rift.isBurning());
            tag.putInt("powerGen", rift.getGenerationPower());
            tag.putInt("energy", rift.getContainedEnergy());
            tag.putInt("heat", rift.getHeatValue());
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
                    tooltip.add(new TranslationTextComponent("text.enderrift.generator.energy", tag.getInt("energy"), DriverTileEntity.POWER_LIMIT));
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
                tooltip.add(new TranslationTextComponent("text.enderrift.network.size", network.getObjects().size()));
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
            if (config.get(CONFIG_RIFT) && (accessor.getBlock() != EnderRiftMod.EnderRiftBlocks.STRUCTURE || accessor.getBlockState().get(StructureBlock.TYPE1) == StructureBlock.Type1.CORNER))
            {
                CompoundNBT tag = accessor.getServerData();

                if (tag != null && tag.contains("isFormed"))
                {
                    tooltip.add(new TranslationTextComponent("text.enderrift.rift.is_formed", tag.getBoolean("isFormed")));
                    tooltip.add(new TranslationTextComponent("text.enderrift.rift.is_powered", tag.getBoolean("isPowered")));
                    if (tag.getBoolean("isFormed"))
                    {
                        tooltip.add(new TranslationTextComponent("text.enderrift.rift.rift_id", tag.getInt("riftId")));
                        if (config.get(CONFIG_RF))
                            tooltip.add(new TranslationTextComponent("text.enderrift.rift.rf", tag.getInt("energy"), RiftTileEntity.BUFFER_POWER));
                    }
                    tooltip.add(new TranslationTextComponent("text.enderrift.rift.used_slots", tag.getInt("usedSlots")));
                }
                else
                {
                    tooltip.add(new TranslationTextComponent("text.enderrift.rift.is_formed", false));
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