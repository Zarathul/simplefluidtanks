package net.zarathul.simplefluidtanks;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.LogWrapper;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Provides helper methods to register the mods recipes.
 */
public final class Recipes
{
	// Default TankBlock recipe
	public static final Recipe defaultTankBlockRecipe = new Recipe
	(
		2,
		new RecipePattern(
			"IGI",
			String.format("G%sG", RecipePattern.EMPTY_SLOT),
			"IGI"
		),
		new RecipeComponent[]
		{
			new RecipeComponent("I", "minecraft", "iron_ingot"),
			new RecipeComponent("G", "minecraft", "glass")
		}
	);

	// Default ValveBlock recipe
	public static final Recipe defaultValveBlockRecipe = new Recipe
	(
		1,
		new RecipePattern(
			"ISI",
			"STS",
			"ISI"
		),
		new RecipeComponent[]
		{
			new RecipeComponent("I", "minecraft", "iron_ingot"),
			new RecipeComponent("S", "minecraft", "slime_ball"),
			new RecipeComponent("T", SimpleFluidTanks.MOD_ID, Registry.TANKBLOCK_NAME)
		}
	);

	// Default Wrench recipe
	public static final Recipe defaultWrenchRecipe = new Recipe
	(
		1,
		new RecipePattern(
			String.format("%sI%s", RecipePattern.EMPTY_SLOT, RecipePattern.EMPTY_SLOT),
			String.format("%sII", RecipePattern.EMPTY_SLOT),
			String.format("I%s%s", RecipePattern.EMPTY_SLOT, RecipePattern.EMPTY_SLOT)
		),
		new RecipeComponent[]
		{
			new RecipeComponent("I", "minecraft", "iron_ingot")
		}
	);

	/**
	 * Registers the mods recipes.
	 */
	public static final void register()
	{
		ItemStack tankBlockRecipeResult = new ItemStack(SimpleFluidTanks.tankBlock);
		ItemStack valveBlockRecipeResult = new ItemStack(SimpleFluidTanks.valveBlock);

		register(tankBlockRecipeResult, Config.tankBlockRecipe, defaultTankBlockRecipe);
		register(valveBlockRecipeResult, Config.valveBlockRecipe, defaultValveBlockRecipe);

		if (Config.wrenchEnabled)
		{
			ItemStack wrenchRecipeResult = new ItemStack(SimpleFluidTanks.wrenchItem);
			register(wrenchRecipeResult, Config.wrenchRecipe, defaultWrenchRecipe);
		}
	}

	/**
	 * Tries to register the recipe for the specified ItemStack. If registration fails
	 * the default recipe is used instead.
	 * 
	 * @param result
	 * The ItemStack crafted using the specified recipe.
	 * @param recipe
	 * The recipe.
	 * @param defaultRecipe
	 * The fallback recipe.
	 */
	private static final void register(ItemStack result, Recipe recipe, Recipe defaultRecipe)
	{
		Object[] registrationArgs;
		Recipe[] recipes = new Recipe[] { recipe, defaultRecipe };

		for (Recipe currentRecipe : recipes)
		{
			try
			{
				registrationArgs = getRegistrationArgs(currentRecipe);

				if (registrationArgs != null && currentRecipe.yield > 0)
				{
					result.stackSize = currentRecipe.yield;

					GameRegistry.addShapedRecipe(result, registrationArgs);

					return;
				}
			}
			catch (Exception e)
			{
			}

			LogWrapper.severe("[%s] Failed to register recipe for: %s. Check your config file.", SimpleFluidTanks.MOD_ID, result.getUnlocalizedName());
		}
	}

	/**
	 * Generates the arguments for the Forge recipe registration API call
	 * from the specified recipe.
	 * 
	 * @param recipe
	 * The recipe to get the arguments for.
	 * @return
	 * The generated arguments or <code>null</code> if a component of the recipe
	 * could't be found or if an component identifier is missing.
	 */
	private static Object[] getRegistrationArgs(Recipe recipe)
	{
		ArrayList<Object> args = new ArrayList<Object>();

		for (String patternRow : recipe.pattern.rows)
		{
			args.add(patternRow);
		}

		for (RecipeComponent component : recipe.components)
		{
			ItemStack componentItem = GameRegistry.findItemStack(component.modId, component.itemId, 1);

			if (componentItem == null || component.identifier == null || component.identifier.length() == 0) return null;

			char id = component.identifier.charAt(0);
			args.add((id == RecipePattern.EMPTY_SLOT) ? ' ' : id);
			args.add(componentItem);
		}

		return args.toArray();
	}
}
