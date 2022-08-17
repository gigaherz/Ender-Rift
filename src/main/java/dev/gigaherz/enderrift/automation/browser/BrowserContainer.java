package dev.gigaherz.enderrift.automation.browser;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;

public class BrowserContainer extends AbstractBrowserContainer
{
    public BrowserContainer(int id, Inventory playerInventory)
    {
        this(id, null, playerInventory);
    }

    public BrowserContainer(int id, @Nullable BrowserBlockEntity te, Inventory playerInventory)
    {
        super(EnderRiftMod.BROWSER_MENU.get(), id, te, playerInventory);
    }
}
