package boss_tools_giselle_addon.common.item.crafting;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.mrscauthd.boss_tools.crafting.ItemStackToItemStackRecipe;

public class RollingRecipe extends ItemStackToItemStackRecipe
{
	public RollingRecipe(ResourceLocation id, JsonObject json)
	{
		super(id, json);
	}

	public RollingRecipe(ResourceLocation id, FriendlyByteBuf buffer)
	{
		super(id, buffer);
	}

	public RollingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack output, int cookTime)
	{
		super(id, ingredient, output, cookTime);
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return AddonRecipes.RECIPE_SERIALIZER_ROLLING.get();
	}

	@Override
	public RecipeType<?> getType()
	{
		return AddonRecipes.ROLLING;
	}

}
