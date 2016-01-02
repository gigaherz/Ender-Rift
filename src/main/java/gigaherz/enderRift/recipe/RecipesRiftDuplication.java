package gigaherz.enderRift.recipe;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class RecipesRiftDuplication implements IRecipe
{
    /**
     * Used to check if a recipe matches current crafting inventory
     */
    public boolean matches(InventoryCrafting crafting, World p_77569_2_)
    {
        if (crafting.getSizeInventory() < 9)
            return false;

        ItemStack stack = crafting.getStackInSlot(4);
        if (stack == null)
            return false;

        if (stack.getItem() != EnderRiftMod.itemEnderRift)
            return false;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("RiftId"))
            return false;

        if (!slotHasItem(crafting, 0, Items.magma_cream))
            return false;

        if (!slotHasItem(crafting, 1, Items.ender_pearl))
            return false;

        if (!slotHasItem(crafting, 2, Items.magma_cream))
            return false;

        if (!slotHasItem(crafting, 3, Items.ender_pearl))
            return false;

        if (!slotHasItem(crafting, 5, Items.ender_pearl))
            return false;

        if (!slotHasItem(crafting, 6, Items.magma_cream))
            return false;

        if (!slotHasItem(crafting, 7, Items.ender_pearl))
            return false;

        if (!slotHasItem(crafting, 8, Items.magma_cream))
            return false;

        return true;
    }

    private boolean slotHasItem(InventoryCrafting crafting, int slot, Item item)
    {
        ItemStack stack = crafting.getStackInSlot(slot);
        return stack != null && stack.getItem() == item;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    public ItemStack getCraftingResult(InventoryCrafting crafting)
    {
        ItemStack stack = crafting.getStackInSlot(4).copy();
        stack.stackSize = 2;
        return stack;
    }

    /**
     * Returns the size of the recipe area
     */
    public int getRecipeSize()
    {
        return 9;
    }

    public ItemStack getRecipeOutput()
    {
        return null;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        return new ItemStack[0];
    }
}