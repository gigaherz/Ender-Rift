package gigaherz.enderRift.rift;

import com.google.gson.JsonObject;
import gigaherz.enderRift.EnderRiftMod;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;


public class OrbDuplicationRecipe implements ICraftingRecipe, net.minecraftforge.common.crafting.IShapedRecipe<CraftingInventory>
{
    private final ResourceLocation recipeId;
    private final IRecipeSerializer<?> serializer;

    public OrbDuplicationRecipe(ResourceLocation recipeId, IRecipeSerializer<?> serializer)
    {
        this.recipeId = recipeId;
        this.serializer = serializer;
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return NonNullList.from(
                Ingredient.EMPTY,
                Ingredient.fromItems(Items.MAGMA_CREAM),
                Ingredient.fromItems(Items.ENDER_PEARL),
                Ingredient.fromItems(Items.MAGMA_CREAM),
                Ingredient.fromItems(Items.ENDER_PEARL),
                Ingredient.fromItems(EnderRiftMod.Items.RIFT_ORB),
                Ingredient.fromItems(Items.ENDER_PEARL),
                Ingredient.fromItems(Items.MAGMA_CREAM),
                Ingredient.fromItems(Items.ENDER_PEARL),
                Ingredient.fromItems(Items.MAGMA_CREAM)
        );
    }

    @Override
    public boolean matches(CraftingInventory crafting, World p_77569_2_)
    {
        if (crafting.getSizeInventory() < 9)
            return false;

        ItemStack stack = crafting.getStackInSlot(4);
        if (stack.getCount() <= 0)
            return false;

        if (stack.getItem() != EnderRiftMod.Items.RIFT_ORB)
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
        return new ItemStack(EnderRiftMod.Items.RIFT_ORB,2);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
    {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean isDynamic()
    {
        return true;
    }

    @Override
    public ResourceLocation getId()
    {
        return recipeId;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return serializer;
    }

    @Override
    public int getRecipeWidth()
    {
        return 0;
    }

    @Override
    public int getRecipeHeight()
    {
        return 0;
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<OrbDuplicationRecipe>
    {
        @Override
        public OrbDuplicationRecipe read(ResourceLocation recipeId, JsonObject json)
        {
            return new OrbDuplicationRecipe(recipeId, this);
        }

        @Override
        public OrbDuplicationRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            return new OrbDuplicationRecipe(recipeId, this);
        }

        @Override
        public void write(PacketBuffer buffer, OrbDuplicationRecipe recipe)
        {

        }
    }
}