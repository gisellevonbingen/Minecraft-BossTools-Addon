package boss_tools_giselle_addon.compat.jei;

import boss_tools_giselle_addon.BossToolsAddon;
import boss_tools_giselle_addon.common.block.AddonBlocks;
import boss_tools_giselle_addon.config.AddonConfigs;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
	public JeiPlugin()
	{

	}

	@Override
	public ResourceLocation getPluginUid()
	{
		return BossToolsAddon.rl("jei");
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration)
	{

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
	{

	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration)
	{

	}

	@Override
	public void registerRecipes(IRecipeRegistration registration)
	{
		this.addIngredientInfo(registration, AddonBlocks.FUEL_LOADER.get(), AddonConfigs.Common.machines.fuelLoader_range.get());
		this.addIngredientInfo(registration, AddonBlocks.GRAVITY_NORMALIZER.get());
	}

	public void addIngredientInfo(IRecipeRegistration registration, IItemProvider itemProvider, Object... objects)
	{
		registration.addIngredientInfo(new ItemStack(itemProvider), VanillaTypes.ITEM, new TranslationTextComponent("jei.info." + itemProvider.asItem().getRegistryName().getPath(), objects));
	}

}
