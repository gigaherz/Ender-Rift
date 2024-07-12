package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class OrbDuplicationRecipe extends CustomRecipe
{
    private final NonNullList<Ingredient> ingredients = NonNullList.of(
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
        if (crafting.width() != 3 || crafting.height() != 3)
            return false;

        ItemStack stack = crafting.getItem(1,1);
        if (stack.getCount() <= 0)
            return false;

        if (stack.getItem() != EnderRiftMod.RIFT_ORB.get())
            return false;

        UUID riftId = stack.get(EnderRiftMod.RIFT_ID);
        if (riftId == null)
            return false;

        if (!ingredients.get(0).test(crafting.getItem(0,0))) return false;
        if (!ingredients.get(1).test(crafting.getItem(0,1))) return false;
        if (!ingredients.get(2).test(crafting.getItem(0,2))) return false;
        if (!ingredients.get(3).test(crafting.getItem(1,0))) return false;
        if (!ingredients.get(5).test(crafting.getItem(1,2))) return false;
        if (!ingredients.get(6).test(crafting.getItem(2,0))) return false;
        if (!ingredients.get(7).test(crafting.getItem(2,1))) return false;
        if (!ingredients.get(8).test(crafting.getItem(2,2))) return false;

        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput crafting, HolderLookup.Provider registryAccess)
    {
        if (crafting.width() != 3 || crafting.height() != 3)
            return ItemStack.EMPTY;

        return crafting.getItem(1,1).copyWithCount(2);
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
}