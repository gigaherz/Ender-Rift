package gigaherz.enderRift.storage;

import com.google.common.collect.Lists;
import gigaherz.api.automation.AutomationHolder;
import gigaherz.enderRift.blocks.TileEnderRift;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class RiftInventory extends AutomationHolder
{
    private final RiftStorageWorldData manager;

    final List<Reference<? extends TileEnderRift>> listeners = Lists.newArrayList();
    final ReferenceQueue<TileEnderRift> deadListeners = new ReferenceQueue<TileEnderRift>();

    RiftInventory(RiftStorageWorldData manager)
    {
        this.manager = manager;
    }

    public void addWeakListener(TileEnderRift e)
    {
        listeners.add(new WeakReference<TileEnderRift>(e, deadListeners));
    }

    public void onContentsChanged()
    {
        for (Reference<? extends TileEnderRift>
             ref = deadListeners.poll();
             ref != null;
             ref = deadListeners.poll())
        {
            listeners.remove(ref);
        }

        for (Iterator<Reference<? extends TileEnderRift>> it = listeners.iterator(); it.hasNext(); )
        {
            TileEnderRift rift = it.next().get();
            if (rift == null || rift.isInvalid())
            {
                it.remove();
            }
            else
            {
                rift.markDirty();
            }
        }

        manager.markDirty();
    }
}
