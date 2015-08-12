package gigaherz.enderRift.items;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemEnderRift extends Item
{
    public ItemEnderRift()
    {
        this.maxStackSize = 16;
        this.setCreativeTab(EnderRiftMod.tabEnderRift);
        this.setTextureName("enderrift:item_rift");
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world,
                             int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ)
    {
        if (world.isRemote)
            return true;

        if (!player.canPlayerEdit(x, y, z, side, itemStack))
            return false;

        if(!EnderRiftMod.blockEnderRift.tryCompleteStructure(world, x, y, z, itemStack))
            return false;

        if (!player.capabilities.isCreativeMode)
        {
            --itemStack.stackSize;
        }

        return true;
    }
}