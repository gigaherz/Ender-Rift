package gigaherz.enderRift;

public class ConfigValues
{

    public static float PowerPerInsertionConstant;
    public static float PowerPerInsertionLinear;
    public static float PowerPerInsertionGeometric;
    public static float PowerPerInsertionCap;

    public static float PowerPerExtractionConstant;
    public static float PowerPerExtractionLinear;
    public static float PowerPerExtractionGeometric;
    public static float PowerPerExtractionCap;

    public static boolean PreferContainersWithExistingStacks;

    public static boolean EnableRudimentaryGenerator;
/*
    public static void readConfig(Configuration config)
    {
        config.load();

        ConfigValues.config = config;

        Property cfg = config.get("General", "PreferContainersWithExistingStacks", true);
        cfg.setComment("If the game lags when using the Rift Browser or Interface, disable this to make item insertion take a bit less time.");
        PreferContainersWithExistingStacks = cfg.getBoolean();

        PowerPerInsertionConstant = (float) config.get("PowerUsage", "PowerPerInsertionConstant", 1.23).getDouble();
        PowerPerInsertionLinear = (float) config.get("PowerUsage", "PowerPerInsertionLinear", 0.93).getDouble();
        PowerPerInsertionGeometric = (float) config.get("PowerUsage", "PowerPerInsertionGeometric", 0).getDouble();
        PowerPerInsertionCap = (float) config.get("PowerUsage", "PowerPerInsertionCap", 10000).getDouble();
        PowerPerExtractionConstant = (float) config.get("PowerUsage", "PowerPerExtractionConstant", 0.97).getDouble();
        PowerPerExtractionLinear = (float) config.get("PowerUsage", "PowerPerExtractionLinear", 0.013).getDouble();
        PowerPerExtractionGeometric = (float) config.get("PowerUsage", "PowerPerExtractionGeometric", 0).getDouble();
        PowerPerExtractionCap = (float) config.get("PowerUsage", "PowerPerExtractionCap", 10000).getDouble();

        EnableRudimentaryGenerator = config.get("Generator", "Enable", true).getBoolean();

        config.save();
    }
 */
}