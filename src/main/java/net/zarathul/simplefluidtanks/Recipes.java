package net.zarathul.simplefluidtanks;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Provides helper methods to register the mods recipes.
 */
public final class Recipes
{
	/**
	 * Registers the mods recipes.
	 */
	public static final void registerRecipes()
	{
		// use thermal expansions hardened glass and bronze(tinkers alloy) ingots for the recipes if TE is installed, otherwise use normal glass and iron
		ItemStack teHardenedGlass = GameRegistry.findItemStack(Config.thermalExpansionModId, Config.thermalExpansionHardenedGlass, 1);
		ItemStack teBronzeIngots = GameRegistry.findItemStack(Config.thermalExpansionModId, Config.thermalExpansionBronzeIngot, 1);
		ItemStack glassRecipeComponent = new ItemStack(Blocks.glass);
		ItemStack ingotRecipeComponent = (teBronzeIngots != null) ? teBronzeIngots : new ItemStack(Items.iron_ingot);
		int tankRecipeOutputAmount = 2;
		
		// the thermal expansion recipe for tanks yields 4 instead of 2 tanks
		if (teHardenedGlass != null)
		{
			glassRecipeComponent = teHardenedGlass;
			tankRecipeOutputAmount = 4;
		}
		
		// tank recipe
		GameRegistry.addShapedRecipe(new ItemStack(SimpleFluidTanks.tankBlock, tankRecipeOutputAmount),
			"IGI",
			"G G",
			"IGI",
			'I', ingotRecipeComponent,
			'G', glassRecipeComponent
		);
		
		// valve recipe
		GameRegistry.addShapedRecipe(new ItemStack(SimpleFluidTanks.valveBlock, 1),
			"ISI",
			"STS",
			"ISI",
			'I', ingotRecipeComponent,
			'T', SimpleFluidTanks.tankBlock,
			'S', Items.slime_ball
		);
	}
}
