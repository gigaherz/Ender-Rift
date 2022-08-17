package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.rift.storage.RiftStorage;
import dev.gigaherz.enderrift.rift.storage.migration.RiftMigration_17_08_2022;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class RiftItem extends Item
{
    public RiftItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public String getDescriptionId(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("RiftId"))
            return this.getDescriptionId() + ".empty";
        else
            return this.getDescriptionId() + ".bound";
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag tag)
    {
        if (RiftStorage.isAvailable() && tag.contains("RiftId", Tag.TAG_ANY_NUMERIC))
        {
            UUID id = RiftStorage.get().getMigration(RiftMigration_17_08_2022.class).getMigratedId(tag.getInt("RiftId"));
            tag.remove("RiftId");
            if (id != null)
            {
                tag.putUUID("RiftId", id);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("RiftId"))
        {
            tooltip.add(Component.translatable("text.enderrift.tooltip.riftid", tag.getUUID("RiftId")));
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
            CompoundTag tag = stack.getTag();
            if (tag == null || !tag.contains("RiftId"))
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