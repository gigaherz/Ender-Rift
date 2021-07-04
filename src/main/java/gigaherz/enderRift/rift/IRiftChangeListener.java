package gigaherz.enderRift.rift;

import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface IRiftChangeListener
{
    boolean isInvalid();

    void onRiftChanged();

    Optional<BlockPos> getLocation();
}