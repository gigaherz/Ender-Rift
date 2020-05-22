package gigaherz.enderRift.automation.browser;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ObjectHolder;

public class BrowserContainer extends AbstractBrowserContainer
{
    @ObjectHolder("enderrift:browser")
    public static ContainerType<BrowserContainer> TYPE;

    public BrowserContainer(int id, PlayerInventory playerInventory, PacketBuffer extraData)
    {
        this(id, extraData.readBlockPos(), playerInventory);
    }

    public BrowserContainer(int id, BlockPos pos, PlayerInventory playerInventory)
    {
        super(id, pos, playerInventory, TYPE);
    }
}

