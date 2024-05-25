package dev.gigaherz.enderrift.plugins;
/*
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.driver.DriverBlockEntity;
import dev.gigaherz.enderrift.generator.GeneratorBlockEntity;
import dev.gigaherz.enderrift.rift.RiftBlock;
import dev.gigaherz.enderrift.rift.RiftBlockEntity;
import dev.gigaherz.enderrift.rift.StructureCornerBlockEntity;
import dev.gigaherz.graph3.Graph;
import dev.gigaherz.graph3.GraphObject;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.ProbeHitData;
import mcjty.theoneprobe.apiimpl.providers.DefaultProbeInfoProvider;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;
import mcjty.theoneprobe.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TheOneProbeProviders implements Function<ITheOneProbe, Void>
{
    public static Function<ITheOneProbe, Void> create()
    {
        return new TheOneProbeProviders();
    }

    @Override
    public Void apply(@Nullable ITheOneProbe top)
    {
        if (top == null)
            return null;

        top.registerBlockDisplayOverride((probeMode, probeInfo, playerEntity, world, blockState, data) -> {
            if (blockState.getBlock() == EnderRiftMod.STRUCTURE_CORNER.get())
            {
                IProbeConfig config = Config.getRealConfig();
                data = new ProbeHitData(data.getPos(), data.getHitVec(), data.getSideHit(), new ItemStack(EnderRiftMod.STRUCTURE_CORNER.get()));
                DefaultProbeInfoProvider.showStandardBlockInfo(config, probeMode, probeInfo, blockState, blockState.getBlock(), world, data.getPos(), playerEntity, data);
                return true;
            }
            return false;
        });

        top.registerProvider(new IProbeInfoProvider()
        {
            @Override
            public ResourceLocation getID()
            {
                return EnderRiftMod.location("probes");
            }

            @Override
            public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data)
            {
                BlockEntity te = world.getBlockEntity(data.getPos());

                if (te instanceof RiftBlockEntity rift)
                    handleRiftTooltip(probeInfo, rift);

                if (te instanceof StructureCornerBlockEntity structure)
                    handleStructureTooltip(probeInfo, structure);

                if (te instanceof DriverBlockEntity driver)
                    handleDriver(probeInfo, driver);

                if (te instanceof GeneratorBlockEntity gen)
                    handleGenerator(probeInfo, gen);

                if (te instanceof GraphObject<?> go)
                    handleGraphObject(probeInfo, go);
            }
        });

        return null;
    }

    //private static final int RF_COLOR_B = 0xFF2F0000;
    //private static final int RF_COLOR_F1 = 0xFF8F0000;
    //private static final int RF_COLOR_F2 = 0xFF6F0000;
    private static final int HEAT_LOW = 0xFF6F0000;
    //private static final IProgressStyle RF_STYLE = new ProgressStyle().suffix(" RF").backgroundColor(RF_COLOR_B).filledColor(RF_COLOR_F1).alternateFilledColor(RF_COLOR_F2);

    private static int ilerp(int a, int b, int p, int m)
    {
        return a + ((b - a) * p) / m;
    }

    public static IProgressStyle getTemperatureColor(int current, int min, int max)
    {
        int c = HEAT_LOW;

        if (current >= min)
        {
            int r, g;

            r = ilerp(0, 255, current - min, max - min);
            g = ilerp(128, 192, current - min, max - min);

            c = 0xFF000000 | (r << 16) | (g << 8);
        }

        return new ProgressStyle().suffix("Â°C").filledColor(c).alternateFilledColor(c);
    }

    private static void handleGenerator(IProbeInfo info, GeneratorBlockEntity generator)
    {
        int powerGen = generator.getGenerationPower();
        if (powerGen > 0)
        {
            info.text(Component.translatable("text.enderrift.generator.status.generating", powerGen));
        }
        else if (generator.isBurning())
        {
            info.text(Component.translatable("text.enderrift.generator.status.heating"));
        }
        else
        {
            info.text(Component.translatable("text.enderrift.generator.status.idle"));
        }

        int heat = generator.getHeatValue();
        info.progress(heat, GeneratorBlockEntity.MAX_HEAT, getTemperatureColor(heat, GeneratorBlockEntity.MIN_HEAT, GeneratorBlockEntity.MAX_HEAT));

        //info.progress(generator.getContainedEnergy(), GeneratorTileEntity.POWER_LIMIT, RF_STYLE);
    }

    private static void handleDriver(IProbeInfo info, DriverBlockEntity driver)
    {
        //info.progress(driver.getInternalBuffer().map(IEnergyStorage::getEnergyStored).orElse(0), DriverTileEntity.POWER_LIMIT, RF_STYLE);
    }

    private static void handleGraphObject(IProbeInfo info, GraphObject go)
    {
        Graph network = go.getGraph();
        if (network != null)
            info.text(String.format("Network size: %d", network.getObjects().size()));
    }

    private static void handleStructureTooltip(IProbeInfo info, StructureCornerBlockEntity structure)
    {
        //info.item(new ItemStack(EnderRiftMod.STRUCTURE.get()));
        structure.getParent().ifPresent(rift -> handleRiftTooltip(info, rift));
    }

    private static void handleRiftTooltip(IProbeInfo info, RiftBlockEntity rift)
    {
        boolean isFormed = rift.getBlockState().getValue(RiftBlock.ASSEMBLED);
        if (isFormed)
        {
            info.text(Component.translatable("text.enderrift.rift.is_formed", true));
            info.text(Component.translatable("text.enderrift.rift.is_powered", rift.isPowered()));
            info.text(Component.translatable("text.enderrift.rift.rift_id", rift.getRiftId()));
            //info.text(Component.translatable("text.enderrift.rift.used_slots", rift.countInventoryStacks()));
            //info.progress(rift.getEnergyBuffer().map(IEnergyStorage::getEnergyStored).orElse(0), RiftTileEntity.BUFFER_POWER, RF_STYLE);
        }
        else
        {
            info.text(Component.translatable("text.enderrift.rift.waila.isFormed", false));
        }
    }
}
*/