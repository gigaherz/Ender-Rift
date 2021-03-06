package gigaherz.enderRift.automation.browser;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class BrowserContainer extends AbstractBrowserContainer
{
    @ObjectHolder("enderrift:browser")
    public static ContainerType<BrowserContainer> TYPE;

    public BrowserContainer(int id, PlayerInventory playerInventory)
    {
        this(id, null, playerInventory);
    }

    public BrowserContainer(int id, @Nullable BrowserTileEntity te, PlayerInventory playerInventory)
    {
        super(TYPE, id, te, playerInventory);
    }
}
