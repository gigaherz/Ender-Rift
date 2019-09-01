package gigaherz.enderRift.plugins;

/*
import com.google.common.base.Function;
import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.driver.TileDriver;
import gigaherz.enderRift.generator.TileGenerator;
import gigaherz.enderRift.rift.TileEnderRift;
import gigaherz.enderRift.rift.TileEnderRiftCorner;
import gigaherz.graph2.Graph;
import gigaherz.graph2.GraphObject;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TheOneProbeProviders implements Function<ITheOneProbe, Void>
{
    @Override
    public Void apply(@Nullable ITheOneProbe top)
    {
        assert top != null;

        top.registerProvider(new IProbeInfoProvider()
        {
            @Override
            public String getID()
            {
                return EnderRiftMod.MODID + "_Probes";
            }

            @Override
            public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data)
            {
                TileEntity te = world.getTileEntity(data.getPos());

                if (te instanceof TileEnderRift || te instanceof TileEnderRiftCorner)
                    handleRiftTooltip(mode, probeInfo, te);

                if (te instanceof TileDriver)
                    handleDriver(mode, probeInfo, te);

                if (te instanceof TileGenerator)
                    handleGenerator(mode, probeInfo, te);

                if (te instanceof GraphObject)
                    handleGraphObject(mode, probeInfo, te);
            }
        });

        return null;
    }

    private static final int RF_COLOR_B = 0xFF2F0000;
    private static final int RF_COLOR_F1 = 0xFF8F0000;
    private static final int RF_COLOR_F2 = 0xFF6F0000;
    private static final int HEAT_LOW = 0xFF6F0000;
    private static final IProgressStyle RF_STYLE = new ProgressStyle().suffix(" RF").backgroundColor(RF_COLOR_B).filledColor(RF_COLOR_F1).alternateFilledColor(RF_COLOR_F2);

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

    private static void handleGenerator(ProbeMode mode, IProbeInfo info, TileEntity te)
    {
        TileGenerator generator = (TileGenerator) te;

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
        info.progress(heat, TileGenerator.MAX_HEAT, getTemperatureColor(heat, TileGenerator.MIN_HEAT, TileGenerator.MAX_HEAT));

        info.progress(generator.getContainedEnergy(), TileGenerator.POWER_LIMIT, RF_STYLE);
    }

    private static void handleDriver(ProbeMode mode, IProbeInfo info, TileEntity te)
    {
        TileDriver driver = (TileDriver) te;
        info.progress(driver.getInternalBuffer().getEnergyStored(), TileDriver.POWER_LIMIT, RF_STYLE);
    }

    private static void handleGraphObject(ProbeMode mode, IProbeInfo info, TileEntity te)
    {
        Graph network = ((GraphObject) te).getGraph();
        info.text(String.format("Network size: %d", network.getObjects().size()));
    }

    private static void handleRiftTooltip(ProbeMode mode, IProbeInfo info, TileEntity te)
    {
        TileEnderRift rift;

        if (te instanceof TileEnderRiftCorner)
        {
            rift = ((TileEnderRiftCorner) te).getParent();
        }
        else
        {
            rift = (TileEnderRift) te;
        }

        assert rift != null;

        boolean isFormed = rift.getBlockMetadata() != 0;

        if (isFormed)
        {
            info.text(I18n.format("text.enderrift.rift.isFormed", true));
            info.text(I18n.format("text.enderrift.rift.is_powered", rift.isPowered()));
            info.text(I18n.format("text.enderrift.rift.rift_id", rift.getRiftId()));
            info.text(I18n.format("text.enderrift.rift.used_slots", rift.countInventoryStacks()));
            info.progress(rift.getEnergyBuffer().getEnergyStored(), TileEnderRift.BUFFER_POWER, RF_STYLE);
        }
        else
        {
            info.text(I18n.format("text.enderrift.rift.waila.isFormed", false));
        }
    }
}
*/