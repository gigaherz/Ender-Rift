package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEnderRift extends Item
{
    public ItemEnderRift(Properties properties)
    {
        super(properties);
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        CompoundNBT tag = stack.getTag();
        if (tag == null || !tag.contains("RiftId"))
            return this.getTranslationKey() + ".empty";
        else
            return this.getTranslationKey() + ".bound";
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("RiftId"))
        {
            tooltip.add(new TranslationTextComponent("text.enderrift.tooltip.riftid", tag.getInt("RiftId")));
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getItem();
        World world = context.getWorld();

        if (world.isRemote)
            return ActionResultType.SUCCESS;

        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        if (!player.canPlayerEdit(pos, facing, stack))
            return ActionResultType.PASS;

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != EnderRiftMod.rift)
            return ActionResultType.PASS;

        if (state.get(BlockEnderRift.ASSEMBLED))
        {
            CompoundNBT tag = stack.getTag();
            if (tag == null || !tag.contains("RiftId"))
            {
                if (!RiftStructure.duplicateOrb(world, pos, player))
                    return ActionResultType.PASS;
            }
        }
        else
        {
            if (!RiftStructure.assemble(world, pos, stack))
                return ActionResultType.PASS;
        }

        if (!player.abilities.isCreativeMode)
        {
            stack.shrink(1);
        }

        return ActionResultType.SUCCESS;
    }
}