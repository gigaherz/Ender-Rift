package gigaherz.enderRift.items;

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
