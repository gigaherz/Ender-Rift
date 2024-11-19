package dev.gigaherz.enderrift.automation.browser;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;

public class BrowserMenu extends AbstractBrowserMenu
{
    public BrowserMenu(int id, Inventory playerInventory)
    {
        this(id, null, playerInventory);
    }

    public BrowserMenu(int id, @Nullable BrowserBlockEntity te, Inventory playerInventory)
    {
        super(EnderRiftMod.BROWSER_MENU.get(), id, te, playerInventory);
    }
}
