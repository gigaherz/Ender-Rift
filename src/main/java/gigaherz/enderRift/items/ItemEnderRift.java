package gigaherz.enderRift.items;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderRift extends Item
{
    public ItemEnderRift()
    {
        this.maxStackSize = 16;
        this.setCreativeTab(EnderRiftMod.tabEnderRift);
        this.setTextureName("enderrift:item_rift");
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("RiftId"))
            return this.getUnlocalizedName() + ".empty";
        else
            return this.getUnlocalizedName() + ".bound";
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List information, boolean p_77624_4_)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("RiftId"))
        {
            information.add("Rift ID: " + tag.getInteger("RiftId"));
        }
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

        if (world.getBlock(x, y, z) != EnderRiftMod.blockEnderRift)
            return false;

        if (world.getBlockMetadata(x, y, z) != 0)
        {
            NBTTagCompound tag = itemStack.getTagCompound();
            if (tag == null || !tag.hasKey("RiftId"))
            {
                if (!EnderRiftMod.blockEnderRift.tryDuplicateRift(world, x, y, z, player))
                    return false;
            }
        }
        else
        {
            if (!EnderRiftMod.blockEnderRift.tryCompleteStructure(world, x, y, z, itemStack))
                return false;
        }

        if (!player.capabilities.isCreativeMode)
        {
            --itemStack.stackSize;
        }

        return true;
    }
}