package gigaherz.enderRift;

import net.minecraftforge.common.config.Configuration;

public class ConfigValues
{

    public static float PowerPerInsertionConstant;
    public static float PowerPerInsertionLinear;
    public static float PowerPerInsertionGeometric;

    public static float PowerPerExtractionConstant;
    public static float PowerPerExtractionLinear;
    public static float PowerPerExtractionGeometric;

    public static void readConfig(Configuration config)
    {
        config.load();

        PowerPerInsertionConstant = (float) config.get("PowerUsage", "PowerPerInsertionConstant", 1.23).getDouble();
        PowerPerInsertionLinear = (float) config.get("PowerUsage", "PowerPerInsertionLinear", 0.93).getDouble();
        PowerPerInsertionGeometric = (float) config.get("PowerUsage", "PowerPerInsertionGeometric", 0).getDouble();
        PowerPerExtractionConstant = (float) config.get("PowerUsage", "PowerPerExtractionConstant", 0.97).getDouble();
        PowerPerExtractionLinear = (float) config.get("PowerUsage", "PowerPerExtractionLinear", 0.013).getDouble();
        PowerPerExtractionGeometric = (float) config.get("PowerUsage", "PowerPerExtractionGeometric", 0).getDouble();

        config.save();
    }
}
