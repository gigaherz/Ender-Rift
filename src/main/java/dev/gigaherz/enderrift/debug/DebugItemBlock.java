package dev.gigaherz.enderrift.debug;

import com.google.common.collect.Lists;
import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.rift.RiftBlockEntity;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

import org.jetbrains.annotations.Nullable;

public class DebugItemBlock extends BaseEntityBlock {

    public DebugItemBlock(Properties properties) {
        super(properties);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DebugItemBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return BaseEntityBlock.createTickerHelper(pBlockEntityType, EnderRiftMod.RIFT_BLOCK_ENTITY.get(), RiftBlockEntity::tickStatic);
    }

    @Deprecated
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        return Lists.newArrayList(new ItemStack(this));
    }

}