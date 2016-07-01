package gigaherz.enderRift.common;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.item.Item;

public class ItemRegistered extends Item
{
    public ItemRegistered(String name)
    {
        setRegistryName(name);
        setUnlocalizedName(EnderRiftMod.MODID + "." + name);
    }
}
