package simplefluidtanks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

public final class Recipes
{
	public static final void registerRecipes()
	{
		// use thermal expansions hardened glass for the recipes if TE is installed otherwise use normal glass
		Block teHardenedGlass = GameRegistry.findBlock(SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_MOD_ID, SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_HARDENED_GLASS);
		Block glassRecipeComponent = (teHardenedGlass != null) ? teHardenedGlass : Block.glass;
		
		// tank recipe
		GameRegistry.addShapedRecipe(new ItemStack(SimpleFluidTanks.tankBlock, 2),
									 "IGI",
									 "GGG",
									 "IGI",
									 'I', Item.ingotIron,
									 'G', glassRecipeComponent
									);
		
		// valve recipe
		GameRegistry.addShapedRecipe(new ItemStack(SimpleFluidTanks.valveBlock),
				 					 "ISI",
				 					 "STS",
				 					 "ISI",
				 					 'I', Item.ingotIron,
				 					 'T', SimpleFluidTanks.tankBlock,
				 					 'S', Item.slimeBall
									);
	}
}
