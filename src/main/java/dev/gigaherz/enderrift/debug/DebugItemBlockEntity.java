package dev.gigaherz.enderrift.debug;

import dev.gigaherz.enderrift.EnderRiftMod;
import dev.gigaherz.enderrift.automation.driver.DriverBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DebugItemBlockEntity extends BlockEntity
{

    private final DebugItemHandler randomItemHandler = new DebugItemHandler(this);
    private final LazyOptional<DebugItemHandler> randomItemHandlerProvider = LazyOptional.of(() -> randomItemHandler);

    public DebugItemBlockEntity(BlockPos pos, BlockState state)
    {
        super(EnderRiftMod.DEBUG_ITEM_BLOCK_ENTITY.get(), pos, state);
    }

    @Nullable
    public IItemHandler getInventory()
    {
        return randomItemHandler;
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);
        randomItemHandler.setSeed(compound.getLong("Seed"));
        randomItemHandler.setIterations(compound.getLong("Itr"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.putLong("Seed", randomItemHandler.getSeed());
        tag.putLong("Itr", randomItemHandler.getIterations());
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag tag = super.getUpdateTag();
        tag.putLong("Seed", randomItemHandler.getSeed());
        tag.putLong("Itr", randomItemHandler.getIterations());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag)
    {
        randomItemHandler.setSeed(tag.getLong("Seed"));
        randomItemHandler.setIterations(tag.getLong("Itr"));
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        handleUpdateTag(pkt.getTag());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return randomItemHandlerProvider.cast();
        return super.getCapability(cap, side);
    }

}