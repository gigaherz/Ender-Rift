package dev.gigaherz.enderrift.rift.storage;

import com.googlecode.concurentlocks.ReentrantReadWriteUpdateLock;
import com.mojang.logging.LogUtils;
import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = EnderRiftMod.MODID)
public class RiftStorage
{
    private RiftStorage()
    {
        throw new RuntimeException("Class cannot be instantiated.");
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LevelResource DATA_DIR = new LevelResource("enderrift");

    private static final HashMap<UUID, RiftHolder> rifts = new HashMap<>();

    private static final ReentrantReadWriteUpdateLock lock = new ReentrantReadWriteUpdateLock();
    private static final Lock readLock = lock.readLock();
    private static final Lock updateLock = lock.updateLock();
    private static final Lock writeLock = lock.writeLock();

    private static Path dataDirectory;

    @NotNull
    private static Path getRiftPath(UUID id)
    {
        return dataDirectory.resolve(id + ".dat");
    }

    @NotNull
    private static Path getTempPath(UUID id)
    {
        return dataDirectory.resolve(id + ".dat.tmp");
    }

    @NotNull
    private static Path getBakPath(UUID id)
    {
        return dataDirectory.resolve(id + ".dat.bak");
    }

    @SubscribeEvent
    public static void serverStart(ServerAboutToStartEvent event)
    {
        dataDirectory = event.getServer().getWorldPath(DATA_DIR);
        try
        {
            if (!Files.exists(dataDirectory))
            {
                Files.createDirectories(dataDirectory);
            }
        }
        catch(IOException ex)
        {
            LOGGER.error("Could not create rifts directory '" + dataDirectory  + "'", ex);
        }
    }

    @SubscribeEvent
    public static void serverSave(LevelEvent.Save event)
    {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevel sl && sl.dimension().equals(Level.OVERWORLD))
        {
            readLock.lock();
            try
            {
                saveDirtyNoLock(event.getLevel().registryAccess());
            }
            finally
            {
                readLock.unlock();
            }
        }
    }

    @SubscribeEvent
    public static void serverStop(ServerStoppingEvent event)
    {
        writeLock.lock();
        try
        {
            saveDirtyNoLock(event.getServer().registryAccess());
            rifts.clear();
            dataDirectory = null;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    private static RiftHolder createRift(UUID id)
    {
        writeLock.lock();
        try
        {
            return rifts.computeIfAbsent(id, RiftHolder::new);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public static RiftHolder newRift()
    {
        updateLock.lock();
        try
        {
            UUID id;
            do
            {
                id = UUID.randomUUID();
            } while (rifts.containsKey(id));

            return createRift(id);
        }
        finally
        {
            updateLock.unlock();
        }
    }

    public static RiftHolder getOrCreateRift(@Nullable UUID id)
    {
        if (id == null)
            return newRift();

        updateLock.lock();
        try
        {
            return Objects.requireNonNullElseGet(rifts.get(id), () -> createRift(id));
        }
        finally
        {
            updateLock.unlock();
        }
    }

    public static Optional<RiftHolder> findRift(UUID id)
    {
        readLock.lock();
        try
        {
            return Optional.ofNullable(rifts.get(id));
        }
        finally
        {
            readLock.unlock();
        }
    }

    static RiftInventory load(RiftHolder holder, HolderLookup.Provider lookup)
    {
        var inv = new RiftInventory(holder);
        UUID id = holder.getId();

        var tmpFile = getTempPath(id);
        var bakFile = getBakPath(id);
        var file = getRiftPath(id);

        if (Files.exists(file))
        {
            if (Files.exists(tmpFile))
            {
                try
                {
                    tryMoveAtomic(tmpFile, bakFile);
                    LOGGER.info("Found both main and temporary storage for rift {}. If the rift is missing contents, they may be in '{}'!", id, bakFile);
                }
                catch (IOException e)
                {
                    LOGGER.error("Error moving temp file {}, this is bad!", tmpFile, e);
                }
            }

            loadRift(lookup, inv, id, file);
        }
        else if (Files.exists(tmpFile))
        {
            LOGGER.info("Found temporary storage for rift {} in {}. Attempting to load...", id, tmpFile);
            if (loadRift(lookup, inv, id, tmpFile))
            {
                try
                {
                    tryMoveAtomic(tmpFile, file);
                }
                catch (IOException e)
                {
                    LOGGER.error("Error moving temp file {}, this is bad!", tmpFile, e);
                }
                return inv;
            }
        }

        return inv;
    }

    private static boolean loadRift(HolderLookup.Provider lookup, RiftInventory inv, UUID id, Path file)
    {
        LOGGER.info("Loading rift {} from {}...", id, file);
        try
        {
            var tag = NbtIo.readCompressed(file, NbtAccounter.create(0x6400000L));
            inv.load(tag, lookup);
        }
        catch (IOException e)
        {
            LOGGER.error("Could not load rift {} from {}", id, file, e);
            return false;
        }
        return true;
    }

    private static void saveDirtyNoLock(HolderLookup.Provider lookup)
    {
        for (RiftHolder holder : rifts.values())
        {
            if (holder.isDirty())
            {
                save(holder, lookup);
                holder.clearDirty();
            }
        }
    }

    private static void save(RiftHolder holder, HolderLookup.Provider lookup)
    {
        var id = holder.getId();
        RiftInventory inventory = holder.getOrLoad(lookup);
        LOGGER.info("Saving rift {}...", id);
        try
        {
            var tmpFile = getTempPath(id);
            var file = getRiftPath(id);

            NbtIo.writeCompressed(inventory.save(lookup), tmpFile);

            tryMoveAtomic(tmpFile, file);
        }
        catch (IOException ex)
        {
            LOGGER.error("Could not save rift {}", id, ex);
        }
    }

    private static void tryMoveAtomic(Path tmpFile, Path file) throws IOException
    {
        try
        {
            // ATOMIC_MOVE may return an IOException in two cases:
            // 1. If a file exists and the operating system doesn't support replacing an existing file in the atomic move operation, or
            // 2. If atomic operations are not supported at all.
            Files.move(tmpFile, file, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException ex)
        {
            Files.move(tmpFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void walkRifts(HolderLookup.Provider lookup, BiConsumer<UUID, RiftInventory> riftConsumer)
    {
        readLock.lock();
        try
        {
            rifts.forEach((id, holder) -> riftConsumer.accept(id, holder.getOrLoad(lookup)));
        }
        finally
        {
            readLock.unlock();
        }
    }
}