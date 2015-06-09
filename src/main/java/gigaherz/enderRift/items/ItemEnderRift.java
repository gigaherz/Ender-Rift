package gigaherz.enderRift.items;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.item.Item;

public class ItemEnderRift extends Item {

    public ItemEnderRift() {
        this.maxStackSize = 16;
        this.setCreativeTab(EnderRiftMod.tabEnderRift);
        this.setTextureName("enderrift:item_rift");
    }
}