package net.zarathul.simplefluidtanks;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/**
 * Provides helper methods to load the mods config.
 */
public final class Config
{
	// settings
	public static int bucketsPerTank = 16;
	public static boolean wrenchEnabled = true;
	public static float tankBlockHardness = 50;
	public static float tankBlockResistance = 1000;
	public static float valveBlockHardness = 50;
	public static float valveBlockResistance = 1000;
	public static Recipe tankBlockRecipe;
	public static Recipe valveBlockRecipe;
	public static Recipe wrenchRecipe;
	// config file comments etc.
	private static final String CATEGORY_MAIN = "simple fluid tanks";
	private static final String BUCKETS_PER_TANK_KEY = "BucketsPerTank";
	private static final String BUCKETS_PER_TANK_COMMENT = "The amount of fluid that can be stored per tank (measured in buckets).";
	private static final String WRENCH_ENABLED_KEY = "WrenchEnabled";
	private static final String WRENCH_ENABLED_COMMENT = "Set to false to disable the recipe for the wrench.";

	private static final String CATEGORY_BLOCKS = "blocks";
	private static final String CATEGORY_BLOCKS_COMMENT = "Here you can adjust some of the blocks properties.";
	private static final String CATEGORY_BLOCKS_TANKBLOCK = CATEGORY_BLOCKS + Configuration.CATEGORY_SPLITTER + "tankBlock";
	private static final String CATEGORY_BLOCKS_VALVEBLOCK = CATEGORY_BLOCKS + Configuration.CATEGORY_SPLITTER + "valveBlock";
	private static final String BLOCK_HARDNESS_COMMENT = "The amount of hits the block can take before it breaks.";
	private static final String BLOCK_RESISTANCE_COMMENT = "The blocks resistance to explosions.";
	private static final String BLOCK_HARDNESS_KEY = "hardness";
	private static final String BLOCK_RESISTANCE_KEY = "resistance";

	private static final String CATEGORY_RECIPES = "recipes";
	private static final String CATEGORY_RECIPES_COMMENT = "Here you can customize the recipes crafting patterns, components and the amount of resulting items. " +
			"If a modified recipe can't be registered with Forge, the default recipe will be used instead. Note: \"" + RecipePattern.EMPTY_SLOT + "\" signifies an empty slot in the recipe pattern.";
	private static final String CATEGORY_RECIPES_TANKBLOCK = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "tankBlock";
	private static final String CATEGORY_RECIPES_VALVEBLOCK = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "valveBlock";
	private static final String CATEGORY_RECIPES_WRENCH = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "wrench";
	private static final String RECIPE_PATTERN_KEY = "pattern";
	private static final String RECIPE_COMPONENTS_KEY = "components";
	private static final String RECIPE_YIELD_KEY = "yield";

	/**
	 * Loads the mods settings from the specified file.
	 * 
	 * @param configFile
	 * The file to load the settings from.
	 */
	public static final void load(File configFile)
	{
		Configuration config = new Configuration(configFile);
		config.load();

		bucketsPerTank = config.get(CATEGORY_MAIN, BUCKETS_PER_TANK_KEY, bucketsPerTank, BUCKETS_PER_TANK_COMMENT).getInt();
		wrenchEnabled = config.get(CATEGORY_MAIN, WRENCH_ENABLED_KEY, wrenchEnabled, WRENCH_ENABLED_COMMENT).getBoolean(wrenchEnabled);

		// Block settings
		tankBlockHardness = (float) config.get(CATEGORY_BLOCKS_TANKBLOCK, BLOCK_HARDNESS_KEY, tankBlockHardness, BLOCK_HARDNESS_COMMENT).getDouble(tankBlockHardness);
		tankBlockResistance = (float) config.get(CATEGORY_BLOCKS_TANKBLOCK, BLOCK_RESISTANCE_KEY, tankBlockResistance, BLOCK_RESISTANCE_COMMENT).getDouble(tankBlockResistance);
		valveBlockHardness = (float) config.get(CATEGORY_BLOCKS_VALVEBLOCK, BLOCK_HARDNESS_KEY, valveBlockHardness, BLOCK_HARDNESS_COMMENT).getDouble(valveBlockHardness);
		valveBlockResistance = (float) config.get(CATEGORY_BLOCKS_VALVEBLOCK, BLOCK_RESISTANCE_KEY, valveBlockResistance, BLOCK_RESISTANCE_COMMENT).getDouble(valveBlockResistance);

		config.getCategory(CATEGORY_BLOCKS).setComment(CATEGORY_BLOCKS_COMMENT);

		// Recipes
		tankBlockRecipe = loadRecipe(config, CATEGORY_RECIPES_TANKBLOCK, Recipes.defaultTankBlockRecipe);
		valveBlockRecipe = loadRecipe(config, CATEGORY_RECIPES_VALVEBLOCK, Recipes.defaultValveBlockRecipe);
		wrenchRecipe = loadRecipe(config, CATEGORY_RECIPES_WRENCH, Recipes.defaultWrenchRecipe);

		config.getCategory(CATEGORY_RECIPES).setComment(CATEGORY_RECIPES_COMMENT);

		config.save();
	}

	/**
	 * Loads a recipe from the config file.
	 * 
	 * @param config
	 * The configuration interface.
	 * @param category
	 * The category containing the recipe.
	 * @param defaultRecipe
	 * The default values for the recipe.
	 * @return
	 * The recipe loaded from the config.
	 */
	private static Recipe loadRecipe(Configuration config, String category, Recipe defaultRecipe)
	{
		String[] pattern = config.get(category, RECIPE_PATTERN_KEY, defaultRecipe.pattern.rows).getStringList();

		String[] components = config.get(category, RECIPE_COMPONENTS_KEY, defaultRecipe.getComponentList()).getStringList();

		int yield = config.get(category, RECIPE_YIELD_KEY, defaultRecipe.yield).getInt();

		return new Recipe(yield, new RecipePattern(pattern), Recipe.toComponents(components));
	}
}
