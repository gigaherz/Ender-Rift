package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ObjectHolder;


import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

public class OrbDuplicationRecipe extends CustomRecipe implements IShapedRecipe<CraftingContainer>
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
    private ItemStack output = new ItemStack(EnderRiftMod.RIFT_ORB.get(), 2);

    public OrbDuplicationRecipe(ResourceLocation recipeId)
    {
        super(recipeId);
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return ingredients;
    }

    @Override
    public boolean matches(CraftingContainer crafting, Level worldIn)
    {
        if (crafting.getContainerSize() < 9)
            return false;

        ItemStack stack = crafting.getItem(4);
        if (stack.getCount() <= 0)
            return false;

        if (stack.getItem() != EnderRiftMod.RIFT_ORB.get())
            return false;

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("RiftId"))
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

    private boolean slotHasItem(CraftingContainer crafting, int slot, Item item)
    {
        ItemStack stack = crafting.getItem(slot);
        return stack.getItem() == item;
    }

    @Override
    public ItemStack assemble(CraftingContainer crafting)
    {
        ItemStack stack = crafting.getItem(4).copy();
        stack.setCount(2);
        return stack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return (width == 3) && (height == 3);
    }

    @Override
    public ItemStack getResultItem()
    {
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv)
    {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return EnderRiftMod.ORB_DUPLICATION.get();
    }

    @Override
    public int getRecipeWidth()
    {
        return 3;
    }

    @Override
    public int getRecipeHeight()
    {
        return 3;
    }
}