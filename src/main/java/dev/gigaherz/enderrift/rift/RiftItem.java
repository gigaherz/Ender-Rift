package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class RiftItem extends Item
{
    public RiftItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag)
    {
        UUID riftId = stack.get(EnderRiftMod.RIFT_ID);
        if (riftId != null)
        {
            tooltipAdder.accept(Component.translatable("text.enderrift.tooltip.riftid", riftId.toString()));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Level world = context.getLevel();

        if (world.isClientSide)
            return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        if (!player.mayUseItemAt(pos, facing, stack))
            return InteractionResult.PASS;

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != EnderRiftMod.RIFT.get())
            return InteractionResult.PASS;

        if (state.getValue(RiftBlock.ASSEMBLED))
        {
            UUID riftId = stack.get(EnderRiftMod.RIFT_ID);
            if (riftId == null)
            {
                if (!RiftStructure.duplicateOrb(world, pos, player))
                    return InteractionResult.PASS;
            }
        }
        else
        {
            if (!RiftStructure.assemble(world, pos, stack))
                return InteractionResult.PASS;
        }

        if (!player.getAbilities().instabuild)
        {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}