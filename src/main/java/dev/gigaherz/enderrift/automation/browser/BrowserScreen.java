package dev.gigaherz.enderrift.automation.browser;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;

public class BrowserScreen extends AbstractBrowserScreen<BrowserContainer>
{
    public BrowserScreen(BrowserContainer container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
    }
}