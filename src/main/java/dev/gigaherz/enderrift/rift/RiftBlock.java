package dev.gigaherz.enderrift.rift;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.AutomationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RiftBlock extends BaseEntityBlock
{
    public static final MapCodec<RiftBlock> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(Properties.CODEC.fieldOf("properties").forGetter(RiftBlock::properties)).apply(inst, RiftBlock::new));

    public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

    private static final VoxelShape SHAPE = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape SHAPE_ACTIVE = Block.box(2, 2, 2, 14, 14, 14);

    public RiftBlock(Properties properties)
    {
        super(properties);
        registerDefaultState(this.getStateDefinition().any()
                .setValue(ASSEMBLED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(ASSEMBLED);
    }

    @Deprecated
    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
    {
        if (state.getBlock() != this)
            return super.getLightEmission(state, world, pos);
        return state.getValue(ASSEMBLED) ? 15 : 0;
    }

    @Deprecated
    @Override
    public int getLightBlock(BlockState state)
    {
        if (state.getBlock() != this)
            return super.getLightBlock(state);
        return state.getValue(ASSEMBLED) ? 1 : 15;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new RiftBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType)
    {
        return BaseEntityBlock.createTickerHelper(pBlockEntityType, EnderRiftMod.RIFT_BLOCK_ENTITY.get(), RiftBlockEntity::tickStatic);
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
    {
        return state.getValue(ASSEMBLED) ? SHAPE_ACTIVE : SHAPE;
    }

    @Deprecated
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        return Lists.newArrayList(new ItemStack(this));
    }

    @Deprecated
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (newState.getBlock() == this || !state.getValue(ASSEMBLED))
        {
            super.onRemove(state, worldIn, pos, newState, isMoving);
            return;
        }

        if (worldIn.getBlockEntity(pos) instanceof RiftBlockEntity rift)
        {
            popResource(worldIn, pos, rift.getRiftItem());
            rift.setRemoved();
        }

        RiftStructure.dismantle(worldIn, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                          BlockHitResult pHitResult)
    {
        if (player.isShiftKeyDown())
            return InteractionResult.PASS;

        if (stack.getItem() == EnderRiftMod.UNBOUND_RIFT_ORB.get())
            return InteractionResult.PASS;

        if (stack.getItem() == EnderRiftMod.RIFT_ORB.get())
            return InteractionResult.PASS;

        if (state.getBlock() != this || !state.getValue(ASSEMBLED))
            return InteractionResult.FAIL;

        BlockEntity te = level.getBlockEntity(pos);
        if (!(te instanceof RiftBlockEntity rift) || !rift.isPowered())
            return InteractionResult.FAIL;

        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        var inv = rift.getInventory();
        if (inv == null)
            return InteractionResult.FAIL;

        ItemStack remaining = ItemHandlerHelper.insertItem(inv, stack, false);

        player.setItemInHand(hand, remaining);

        return InteractionResult.SUCCESS;
    }

    @Deprecated
    @Override
    public void attack(BlockState state, Level worldIn, BlockPos pos, Player playerIn)
    {
        if (state.getBlock() != this || !state.getValue(ASSEMBLED))
            super.attack(state, worldIn, pos, playerIn);

        if (worldIn.isClientSide)
            return;

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (!(te instanceof RiftBlockEntity rift) || !rift.isPowered())
            return;

        ItemStack stack = playerIn.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.getCount() <= 0)
        {
            if (stack.getItem() == EnderRiftMod.RIFT_ORB.get() || stack.getItem() == EnderRiftMod.UNBOUND_RIFT_ORB.get())
                return;
        }
        else
        {
            stack = rift.chooseRandomStack();
            if (stack.getCount() <= 0)
                return;
        }

        var inv = rift.getInventory();
        if (inv == null)
            return;

        int numberToExtract = playerIn.isShiftKeyDown() ? 1 : stack.getMaxStackSize();

        ItemStack extracted = AutomationHelper.extractItems(inv, stack, numberToExtract, false);
        if (extracted.getCount() > 0)
        {
            popResource(worldIn, pos, extracted);
        }
    }

    @Deprecated
    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn)
    {
        if (worldIn.isClientSide)
            return;

        if (!(entityIn instanceof ItemEntity itemEntity))
            return;

        if (state.getBlock() != this || !state.getValue(ASSEMBLED))
            return;

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (!(te instanceof RiftBlockEntity rift) || !rift.isPowered())
            return;

        var inv = rift.getInventory();
        if (inv == null)
            return;

        ItemStack remaining = ItemHandlerHelper.insertItem(inv, itemEntity.getItem(), false);

        if (remaining.getCount() <= 0)
        {
            entityIn.remove(Entity.RemovalReason.KILLED);
        }
        else
        {
            itemEntity.setItem(remaining);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        UUID riftId = stack.get(EnderRiftMod.RIFT_ID);
        if (riftId != null)
        {
            tooltip.add(Component.translatable("text.enderrift.tooltip.riftid", riftId.toString()));
        }
    }

}