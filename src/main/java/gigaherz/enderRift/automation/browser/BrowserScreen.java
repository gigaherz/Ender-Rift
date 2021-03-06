package gigaherz.enderRift.automation.browser;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class BrowserScreen extends AbstractBrowserScreen<BrowserContainer>
{
    public BrowserScreen(BrowserContainer container, PlayerInventory playerInventory, ITextComponent title)
    {
        super(container, playerInventory, title);
    }
}