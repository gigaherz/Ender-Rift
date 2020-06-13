package gigaherz.enderRift.plugins;


import com.google.common.base.Function;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.driver.DriverTileEntity;
import gigaherz.enderRift.generator.GeneratorTileEntity;
import gigaherz.enderRift.rift.RiftBlock;
import gigaherz.enderRift.rift.RiftTileEntity;
import gigaherz.enderRift.rift.StructureBlock;
import gigaherz.enderRift.rift.StructureTileEntity;
import gigaherz.graph2.Graph;
import gigaherz.graph2.GraphObject;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.ProbeHitData;
import mcjty.theoneprobe.apiimpl.providers.DefaultProbeInfoProvider;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TheOneProbeProviders implements Function<ITheOneProbe, Void>
{

    @Override
    public Void apply(@Nullable ITheOneProbe top)
    {
        if (top == null)
             return null;

        top.registerBlockDisplayOverride((probeMode, probeInfo, playerEntity, world, blockState, data) -> {
            if (blockState.getBlock() == EnderRiftMod.EnderRiftBlocks.STRUCTURE
                    && blockState.get(StructureBlock.TYPE1) == StructureBlock.Type1.CORNER)
            {
                IProbeConfig config = Config.getRealConfig();
                data = new ProbeHitData(data.getPos(), data.getHitVec(), data.getSideHit(), new ItemStack(EnderRiftMod.EnderRiftBlocks.STRUCTURE));
                DefaultProbeInfoProvider.showStandardBlockInfo(config, probeMode, probeInfo, blockState, blockState.getBlock(), world, data.getPos(), playerEntity, data);
                return true;
            }
            return false;
        });

        top.registerProvider(new IProbeInfoProvider()
        {
            @Override
            public String getID()
            {
                return EnderRiftMod.MODID + "_probes";
            }

            @Override
            public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data)
            {
                TileEntity te = world.getTileEntity(data.getPos());

                if (te instanceof RiftTileEntity)
                    handleRiftTooltip(probeInfo,  (RiftTileEntity) te);

                if (te instanceof StructureTileEntity)
                    handleStructureTooltip(probeInfo, (StructureTileEntity)te);

                if (te instanceof DriverTileEntity)
                    handleDriver(probeInfo, (DriverTileEntity)te);

                if (te instanceof GeneratorTileEntity)
                    handleGenerator(probeInfo, (GeneratorTileEntity)te);

                if (te instanceof GraphObject)
                    handleGraphObject(probeInfo, (GraphObject)te);
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

        return new ProgressStyle().suffix("°C").filledColor(c).alternateFilledColor(c);
    }

    private static void handleGenerator(IProbeInfo info, GeneratorTileEntity generator)
    {
        int powerGen = generator.getGenerationPower();
        if (powerGen > 0)
        {
            info.text(I18n.format("text.enderrift.generator.status.generating", powerGen));
        }
        else if (generator.isBurning())
        {
            info.text(I18n.format("text.enderrift.generator.status.heating"));
        }
        else
        {
            info.text(I18n.format("text.enderrift.generator.status.idle"));
        }

        int heat = generator.getHeatValue();
        info.progress(heat, GeneratorTileEntity.MAX_HEAT, getTemperatureColor(heat, GeneratorTileEntity.MIN_HEAT, GeneratorTileEntity.MAX_HEAT));

        //info.progress(generator.getContainedEnergy(), GeneratorTileEntity.POWER_LIMIT, RF_STYLE);
    }

    private static void handleDriver(IProbeInfo info, DriverTileEntity driver)
    {
        //info.progress(driver.getInternalBuffer().map(IEnergyStorage::getEnergyStored).orElse(0), DriverTileEntity.POWER_LIMIT, RF_STYLE);
    }

    private static void handleGraphObject(IProbeInfo info, GraphObject go)
    {
        Graph network = go.getGraph();
        if (network != null)
            info.text(String.format("Network size: %d", network.getObjects().size()));
    }

    private static void handleStructureTooltip(IProbeInfo info, StructureTileEntity structure)
    {
        //info.item(new ItemStack(EnderRiftMod.EnderRiftBlocks.STRUCTURE));
        structure.getParent().ifPresent(rift -> handleRiftTooltip(info, rift));
    }

    private static void handleRiftTooltip(IProbeInfo info, RiftTileEntity rift)
    {
        boolean isFormed = rift.getBlockState().get(RiftBlock.ASSEMBLED);
        if (isFormed)
        {
            info.text(I18n.format("text.enderrift.rift.is_formed", true));
            info.text(I18n.format("text.enderrift.rift.is_powered", rift.isPowered()));
            info.text(I18n.format("text.enderrift.rift.rift_id", rift.getRiftId()));
            info.text(I18n.format("text.enderrift.rift.used_slots", rift.countInventoryStacks()));
            //info.progress(rift.getEnergyBuffer().map(IEnergyStorage::getEnergyStored).orElse(0), RiftTileEntity.BUFFER_POWER, RF_STYLE);
        }
        else
        {
            info.text(I18n.format("text.enderrift.rift.waila.isFormed", false));
        }
    }
}
