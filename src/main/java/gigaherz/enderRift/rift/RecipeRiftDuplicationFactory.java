package gigaherz.enderRift.rift;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class RecipeRiftDuplicationFactory implements IRecipeFactory
{
    @Override
    public IRecipe parse(JsonContext context, JsonObject json)
    {
        return new RecipeRiftDuplication();
    }
}
