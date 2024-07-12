package dev.gigaherz.enderrift;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

@EventBusSubscriber(modid = EnderRiftMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigValues
{
    public static final ServerConfig SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static
    {
        final Pair<ServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class ServerConfig
    {
        public ModConfigSpec.DoubleValue powerPerInsertionConstant;
        public ModConfigSpec.DoubleValue powerPerInsertionLinear;
        public ModConfigSpec.DoubleValue powerPerInsertionGeometric;
        public ModConfigSpec.DoubleValue powerPerInsertionCap;
        public ModConfigSpec.DoubleValue powerPerExtractionConstant;
        public ModConfigSpec.DoubleValue powerPerExtractionLinear;
        public ModConfigSpec.DoubleValue powerPerExtractionGeometric;
        public ModConfigSpec.DoubleValue powerPerExtractionCap;

        ServerConfig(ModConfigSpec.Builder builder)
        {
            builder.push("PowerUsage");

            powerPerInsertionConstant = builder.defineInRange("powerPerInsertionConstant", 1.23, 0, Double.MAX_VALUE);
            powerPerInsertionLinear = builder.defineInRange("powerPerInsertionLinear", 0.93, 0, Double.MAX_VALUE);
            powerPerInsertionGeometric = builder.defineInRange("powerPerInsertionGeometric", 0, 0, Double.MAX_VALUE);
            powerPerInsertionCap = builder.defineInRange("powerPerInsertionCap", 10000, 0, Double.MAX_VALUE);
            powerPerExtractionConstant = builder.defineInRange("powerPerExtractionConstant", 0.97, 0, Double.MAX_VALUE);
            powerPerExtractionLinear = builder.defineInRange("powerPerExtractionLinear", 0.013, 0, Double.MAX_VALUE);
            powerPerExtractionGeometric = builder.defineInRange("powerPerExtractionGeometric", 0, 0, Double.MAX_VALUE);
            powerPerExtractionCap = builder.defineInRange("powerPerExtractionCap", 10000, 0, Double.MAX_VALUE);

            builder.pop();
        }
    }

    public static float PowerPerInsertionConstant;
    public static float PowerPerInsertionLinear;
    public static float PowerPerInsertionGeometric;
    public static float PowerPerInsertionCap;

    public static float PowerPerExtractionConstant;
    public static float PowerPerExtractionLinear;
    public static float PowerPerExtractionGeometric;
    public static float PowerPerExtractionCap;

    @SubscribeEvent
    public static void modConfig(ModConfigEvent.Loading event)
    {
        reloadConfigs();
    }

    @SubscribeEvent
    public static void modConfig(ModConfigEvent.Reloading event)
    {
        reloadConfigs();
    }

    private static void reloadConfigs()
    {
        PowerPerInsertionConstant = (float) (double) SERVER.powerPerInsertionConstant.get();
        PowerPerInsertionLinear = (float) (double) SERVER.powerPerInsertionLinear.get();
        PowerPerInsertionGeometric = (float) (double) SERVER.powerPerInsertionGeometric.get();
        PowerPerInsertionCap = (float) (double) SERVER.powerPerInsertionCap.get();
        PowerPerExtractionConstant = (float) (double) SERVER.powerPerExtractionConstant.get();
        PowerPerExtractionLinear = (float) (double) SERVER.powerPerExtractionLinear.get();
        PowerPerExtractionGeometric = (float) (double) SERVER.powerPerExtractionGeometric.get();
        PowerPerExtractionCap = (float) (double) SERVER.powerPerExtractionCap.get();
    }
}