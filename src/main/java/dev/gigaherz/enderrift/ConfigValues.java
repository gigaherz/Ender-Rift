package dev.gigaherz.enderrift;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = EnderRiftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigValues
{
    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static
    {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    public static class ServerConfig
    {
        public ForgeConfigSpec.DoubleValue powerPerInsertionConstant;
        public ForgeConfigSpec.DoubleValue powerPerInsertionLinear;
        public ForgeConfigSpec.DoubleValue powerPerInsertionGeometric;
        public ForgeConfigSpec.DoubleValue powerPerInsertionCap;
        public ForgeConfigSpec.DoubleValue powerPerExtractionConstant;
        public ForgeConfigSpec.DoubleValue powerPerExtractionLinear;
        public ForgeConfigSpec.DoubleValue powerPerExtractionGeometric;
        public ForgeConfigSpec.DoubleValue powerPerExtractionCap;

        ServerConfig(ForgeConfigSpec.Builder builder)
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
    public static void modConfig(ModConfigEvent event)
    {
        PowerPerInsertionConstant = (float)(double)SERVER.powerPerInsertionConstant.get();
        PowerPerInsertionLinear = (float)(double)SERVER.powerPerInsertionLinear.get();
        PowerPerInsertionGeometric = (float)(double)SERVER.powerPerInsertionGeometric.get();
        PowerPerInsertionCap = (float)(double)SERVER.powerPerInsertionCap.get();
        PowerPerExtractionConstant = (float)(double)SERVER.powerPerExtractionConstant.get();
        PowerPerExtractionLinear = (float)(double)SERVER.powerPerExtractionLinear.get();
        PowerPerExtractionGeometric = (float)(double)SERVER.powerPerExtractionGeometric.get();
        PowerPerExtractionCap = (float)(double)SERVER.powerPerExtractionCap.get();
    }
}