package dev.gigaherz.enderrift.rift;

import dev.gigaherz.enderrift.EnderRiftMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplays;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class OrbDuplicationRecipe extends CustomRecipe
{
    private final List<@NotNull Ingredient> ingredients = List.of(
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
    public List<RecipeDisplay> display()
    {
        return List.of(new ShapedCraftingRecipeDisplay(3,3,
                List.of(new SlotDisplay.ItemSlotDisplay(Items.MAGMA_CREAM),
                new SlotDisplay.ItemSlotDisplay(Items.ENDER_PEARL),
                new SlotDisplay.ItemSlotDisplay(Items.MAGMA_CREAM),
                new SlotDisplay.ItemSlotDisplay(Items.ENDER_PEARL),
                new SlotDisplay.ItemSlotDisplay(EnderRiftMod.RIFT_ORB.get()),
                new SlotDisplay.ItemSlotDisplay(Items.ENDER_PEARL),
                new SlotDisplay.ItemSlotDisplay(Items.MAGMA_CREAM),
                new SlotDisplay.ItemSlotDisplay(Items.ENDER_PEARL),
                new SlotDisplay.ItemSlotDisplay(Items.MAGMA_CREAM)),
                new SlotDisplay.ItemStackSlotDisplay(output),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
                ));
    }

    @Override
    public boolean matches(CraftingInput crafting, Level worldIn)
    {
        if (crafting.width() != 3 || crafting.height() != 3)
            return false;

        ItemStack middle = crafting.getItem(1,1);
        if (middle.getCount() <= 0)
            return false;

        if (middle.getItem() != EnderRiftMod.RIFT_ORB.get())
            return false;

        UUID riftId = middle.get(EnderRiftMod.RIFT_ID);
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
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
    {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<OrbDuplicationRecipe> getSerializer()
    {
        return EnderRiftMod.ORB_DUPLICATION.get();
    }
}