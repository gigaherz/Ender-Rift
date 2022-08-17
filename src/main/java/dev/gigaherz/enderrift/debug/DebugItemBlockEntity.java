package dev.gigaherz.enderrift.debug;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DebugItemBlockEntity extends BlockEntity {

    private final DebugItemHandler handler = new DebugItemHandler(this);
    private final LazyOptional<DebugItemHandler> poweredInventoryProvider = LazyOptional.of(() -> handler);

    public DebugItemBlockEntity(BlockPos pos, BlockState state) {
        super(EnderRiftMod.DEBUG_ITEM_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public IItemHandler getInventory() {
        return handler;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        handler.setSeed(compound.getLong("Seed"));
        handler.setIterations(compound.getLong("Itr"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Seed", handler.getSeed());
        tag.putLong("Itr", handler.getIterations());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putLong("Seed", handler.getSeed());
        tag.putLong("Itr", handler.getIterations());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handler.setSeed(tag.getLong("Seed"));
        handler.setIterations(tag.getLong("Itr"));
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return poweredInventoryProvider.cast();
        return super.getCapability(cap, side);
    }

}