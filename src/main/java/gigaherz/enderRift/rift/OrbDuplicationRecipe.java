package gigaherz.enderRift.rift;

import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ObjectHolder;


public class OrbDuplicationRecipe extends SpecialRecipe implements IShapedRecipe<CraftingInventory>
{
    @ObjectHolder("enderrift:orb_duplication")
    public static SpecialRecipeSerializer<OrbDuplicationRecipe> SERIALIZER;

    private NonNullList<Ingredient> ingredients = NonNullList.from(
            Ingredient.EMPTY,
            Ingredient.fromItems(Items.MAGMA_CREAM),
            Ingredient.fromItems(Items.ENDER_PEARL),
            Ingredient.fromItems(Items.MAGMA_CREAM),
            Ingredient.fromItems(Items.ENDER_PEARL),
            Ingredient.fromItems(EnderRiftMod.EnderRiftItems.RIFT_ORB),
            Ingredient.fromItems(Items.ENDER_PEARL),
            Ingredient.fromItems(Items.MAGMA_CREAM),
            Ingredient.fromItems(Items.ENDER_PEARL),
            Ingredient.fromItems(Items.MAGMA_CREAM)
    );
    private ItemStack output = new ItemStack(EnderRiftMod.EnderRiftItems.RIFT_ORB, 2);

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
    public boolean matches(CraftingInventory crafting, World worldIn)
    {
        if (crafting.getSizeInventory() < 9)
            return false;

        ItemStack stack = crafting.getStackInSlot(4);
        if (stack.getCount() <= 0)
            return false;

        if (stack.getItem() != EnderRiftMod.EnderRiftItems.RIFT_ORB)
            return false;

        CompoundNBT tag = stack.getTag();
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

    private boolean slotHasItem(CraftingInventory crafting, int slot, Item item)
    {
        ItemStack stack = crafting.getStackInSlot(slot);
        return stack.getItem() == item;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory crafting)
    {
        ItemStack stack = crafting.getStackInSlot(4).copy();
        stack.setCount(2);
        return stack;
    }

    @Override
    public boolean canFit(int width, int height)
    {
        return (width == 3) && (height == 3);
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
    {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return SERIALIZER;
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