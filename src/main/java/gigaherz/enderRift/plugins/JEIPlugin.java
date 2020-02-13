package gigaherz.enderRift.plugins;

import gigaherz.enderRift.EnderRiftMod;
import gigaherz.enderRift.automation.browser.CraftingBrowserContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    private static final ResourceLocation UID = EnderRiftMod.location("jei_plugin");

    @Override
    public ResourceLocation getPluginUid()
    {
        return UID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
    {
        registration.addRecipeTransferHandler(CraftingBrowserContainer.class, VanillaRecipeCategoryUid.CRAFTING,
                CraftingBrowserContainer.CraftingSlotStart, 9,
                CraftingBrowserContainer.InventorySlotStart, 36);
    }
}
