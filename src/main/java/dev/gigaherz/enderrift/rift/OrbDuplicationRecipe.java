package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

import java.util.UUID;

public class OrbDuplicationRecipe extends CustomRecipe implements IShapedRecipe<CraftingInput>
{
    private NonNullList<Ingredient> ingredients = NonNullList.of(
            Ingredient.EMPTY,
            Ingredient.of(Items.MAGMA_CREAM),
            Ingredient.of(Items.ENDER_PEARL),
            Ingredient.of(Items.MAGMA_CREAM),
            Ingredient.of(Items.ENDER_PEARL),
            Ingredient.of(EnderRiftMod.RIFT_ORB.get()),
            Ingredient.of(Items.ENDER_PEARL),
            Ingredient.of(Items.MAGMA_CREAM),
            Ingredient.of(Items.ENDER_PEARL),
            Ingredient.of(Items.MAGMA_CREAM)
    );
    private final ItemStack output = new ItemStack(EnderRiftMod.RIFT_ORB.get(), 2);

    public OrbDuplicationRecipe(CraftingBookCategory category)
    {
        super(category);
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return ingredients;
    }

    @Override
    public boolean matches(CraftingInput crafting, Level worldIn)
    {
        if (crafting.size() < 9)
            return false;

        ItemStack stack = crafting.getItem(4);
        if (stack.getCount() <= 0)
            return false;

        if (stack.getItem() != EnderRiftMod.RIFT_ORB.get())
            return false;

        UUID riftId = stack.get(EnderRiftMod.RIFT_ID);
        if (riftId == null)
            return false;

        if (!slotHasItem(crafting, 0, Items.MAGMA_CREAM))
            return false;

        if (!slotHasItem(crafting, 1, Items.ENDER_PEARL))
            return false;

        if (!slotHasItem(crafting, 2, Items.MAGMA_CREAM))
            return false;

        if (!slotHasItem(crafting, 3, Items.ENDER_PEARL))
            return false;

        if (!slotHasItem(crafting, 5, Items.ENDER_PEARL))
            return false;

        if (!slotHasItem(crafting, 6, Items.MAGMA_CREAM))
            return false;

        if (!slotHasItem(crafting, 7, Items.ENDER_PEARL))
            return false;

        if (!slotHasItem(crafting, 8, Items.MAGMA_CREAM))
            return false;

        return true;
    }

    private boolean slotHasItem(CraftingInput crafting, int slot, Item item)
    {
        ItemStack stack = crafting.getItem(slot);
        return stack.getItem() == item;
    }

    @Override
    public ItemStack assemble(CraftingInput crafting, HolderLookup.Provider registryAccess)
    {
        return crafting.getItem(4).copyWithCount(2);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return (width == 3) && (height == 3);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess)
    {
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
    {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return EnderRiftMod.ORB_DUPLICATION.get();
    }

    @Override
    public int getWidth()
    {
        return 3;
    }

    @Override
    public int getHeight()
    {
        return 3;
    }
}