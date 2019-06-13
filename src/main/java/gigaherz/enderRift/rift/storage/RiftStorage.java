package gigaherz.enderRift.rift.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class RiftStorage extends WorldSavedData
{
    private static final String StorageKey = "enderRiftStorageManager";

    private Map<Integer, RiftInventory> rifts = new HashMap<Integer, RiftInventory>();
    private int lastRiftId;

    public RiftStorage()
    {
        super(StorageKey);
    }

    private static final RiftStorage clientStorageCopy = new RiftStorage();
    public static RiftStorage get(World world)
    {
        if (!(world instanceof ServerWorld))
        {
            return clientStorageCopy;
        }

        ServerWorld overworld = world.getServer().getWorld(DimensionType.OVERWORLD);

        DimensionSavedDataManager storage = overworld.getSavedData();
        return storage.func_215752_a(RiftStorage::new, StorageKey);
    }

    public RiftInventory getRift(int id)
    {
        RiftInventory rift = rifts.get(id);

        if (rift == null)
        {
            rift = new RiftInventory(this);
            rifts.put(id, rift);
        }

        return rift;
    }

    public int getNextRiftId()
    {
        markDirty();

        return ++lastRiftId;
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        ListNBT riftList = nbt.getList("Rifts", Constants.NBT.TAG_COMPOUND);

        rifts.clear();

        for (int i = 0; i < riftList.size(); ++i)
        {
            CompoundNBT riftTag = riftList.getCompound(i);
            int j = riftTag.getByte("Rift");

            RiftInventory inventory = new RiftInventory(this);
            inventory.readFromNBT(riftTag);

            rifts.put(j, inventory);
        }

        lastRiftId = nbt.getInt("LastRiftId");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbtTagCompound)
    {
        ListNBT nbtTagList = new ListNBT();

        for (Map.Entry<Integer, RiftInventory> entry : rifts.entrySet())
        {
            RiftInventory inventory = entry.getValue();

            CompoundNBT nbtTagCompound1 = new CompoundNBT();
            nbtTagCompound1.putInt("Rift", entry.getKey());
            inventory.writeToNBT(nbtTagCompound1);
            nbtTagList.add(nbtTagCompound1);
        }

        nbtTagCompound.put("Rifts", nbtTagList);
        nbtTagCompound.putInt("LastRiftId", lastRiftId);

        return nbtTagCompound;
    }
}