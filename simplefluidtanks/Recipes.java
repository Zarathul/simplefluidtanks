package simplefluidtanks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Provides helper methods to register the mods recipes.
 */
public final class Recipes
{
	public static final void registerRecipes()
	{
		// use thermal expansions hardened glass and bronze(tinkers alloy) ingots for the recipes if TE is installed, otherwise use normal glass and iron
		ItemStack teHardenedGlass = GameRegistry.findItemStack(SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_MOD_ID, SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_HARDENED_GLASS, 1);
		ItemStack teBronzeIngots = GameRegistry.findItemStack(SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_MOD_ID, SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_BRONZE_INGOT, 1);
		ItemStack glassRecipeComponent = new ItemStack(Block.glass);
		ItemStack ingotRecipeComponent = (teBronzeIngots != null) ? teBronzeIngots : new ItemStack(Item.ingotIron);
		int tankRecipeOutputAmount = 2;
		
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
			'S', Item.slimeBall
		);
	}
}
