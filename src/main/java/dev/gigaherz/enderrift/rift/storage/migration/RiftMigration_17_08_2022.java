package dev.gigaherz.enderrift.rift.storage.migration;

import dev.gigaherz.enderrift.rift.storage.RiftHolder;
import dev.gigaherz.enderrift.rift.storage.RiftInventory;
import dev.gigaherz.enderrift.rift.storage.RiftMigration;
import dev.gigaherz.enderrift.rift.storage.RiftStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class RiftMigration_17_08_2022 extends RiftMigration
{

    private final HashMap<Integer, UUID> map = new HashMap<>();

    public UUID getMigratedId(int id)
    {
        return map.get(id);
    }

    @Override
    public void migrate(RiftStorage storage) throws Exception
    {
        File oldStorage = new File(storage.getDataDirectory(), "../data/enderRiftStorageManager.dat");
        CompoundTag tag = NbtIo.readCompressed(oldStorage);
        CompoundTag data = tag.getCompound("data");
        ListTag rifts = data.getList("Rifts", Tag.TAG_COMPOUND);
        for (int index = 0; index < rifts.size(); index++)
        {
            CompoundTag riftTag = rifts.getCompound(index);
            int riftId = riftTag.getByte("Rift");
            ListTag items = riftTag.getList("Items", Tag.TAG_COMPOUND);
            RiftHolder holder = storage.newRift();
            map.put(riftId, holder.getId());
            RiftInventory inventory = holder.getInventoryOrCreate();
            for (int i = 0; i < items.size(); i++)
            {
                ItemStack itemStack = ItemStack.of(items.getCompound(i));
                if (!inventory.isItemValid(i, itemStack))
                {
                    continue;
                }
                inventory.insertItem(i, itemStack, false);
            }
        }
    }

    @Override
    protected String getName()
    {
        return "Old integer ids to new UUID storage system";
    }

    @Override
    protected boolean isApplicable(RiftStorage storage)
    {
        return new File(storage.getDataDirectory(), "../data/enderRiftStorageManager.dat").exists();
    }
}
