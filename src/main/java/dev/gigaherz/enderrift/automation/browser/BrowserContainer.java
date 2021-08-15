package dev.gigaherz.enderrift.automation.browser;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class BrowserContainer extends AbstractBrowserContainer
{
    @ObjectHolder("enderrift:browser")
    public static MenuType<BrowserContainer> TYPE;

    public BrowserContainer(int id, Inventory playerInventory)
    {
        this(id, null, playerInventory);
    }

    public BrowserContainer(int id, @Nullable BrowserBlockEntity te, Inventory playerInventory)
    {
        super(TYPE, id, te, playerInventory);
    }
}
