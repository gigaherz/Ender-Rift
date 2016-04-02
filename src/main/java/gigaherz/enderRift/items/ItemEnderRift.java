package gigaherz.enderRift.items;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.blocks.BlockEnderRift;
import gigaherz.enderRift.rift.RiftStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderRift extends ItemRegistered
{
    public ItemEnderRift(String name)
    {
        super(name);
        this.maxStackSize = 16;
        this.setCreativeTab(EnderRiftMod.tabEnderRift);
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
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> information, boolean p_77624_4_)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("RiftId"))
        {
            information.add("Rift ID: " + tag.getInteger("RiftId"));
        }
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote)
            return EnumActionResult.SUCCESS;

        if (!playerIn.canPlayerEdit(pos, facing, stack))
            return EnumActionResult.PASS;

        IBlockState state = worldIn.getBlockState(pos);

        if (state.getBlock() != EnderRiftMod.rift)
            return EnumActionResult.PASS;

        if (state.getValue(BlockEnderRift.ASSEMBLED))
        {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null || !tag.hasKey("RiftId"))
            {
                if (!RiftStructure.duplicateOrb(worldIn, pos, playerIn))
                    return EnumActionResult.PASS;
            }
        }
        else
        {
            if (!RiftStructure.assemble(worldIn, pos, stack))
                return EnumActionResult.PASS;
        }

        if (!playerIn.capabilities.isCreativeMode)
        {
            --stack.stackSize;
        }

        return EnumActionResult.SUCCESS;
    }
}