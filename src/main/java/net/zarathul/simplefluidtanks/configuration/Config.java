package net.zarathul.simplefluidtanks.configuration;

import java.io.File;

import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.registration.Registry;

/**
 * Provides helper methods to load the mods config.
 */
public final class Config
{
	private static Configuration config = null;

	// default values

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

	public static final Recipe defaultWrenchRecipe = new Recipe
	(
		1,
		new RecipePattern(
			String.format("%1$sI%1$s", RecipePattern.EMPTY_SLOT),
			String.format("%sII", RecipePattern.EMPTY_SLOT),
			String.format("I%1$s%1$s", RecipePattern.EMPTY_SLOT)
		),
		new RecipeComponent[]
		{
			new RecipeComponent("I", "minecraft", "iron_ingot")
		}
	);

	private static final int defaultBucketsPerTank = 16;
	private static final int defaultOverrideBottleVolume = 250;
	private static final boolean defaultWrenchEnabled = true;
	private static final float defaultTankBlockHardness = 50;
	private static final float defaultTankBlockResistance = 1000;
	private static final float defaultValveBlockHardness = 50;
	private static final float defaultValveBlockResistance = 1000;

	// settings

	public static int bucketsPerTank;
	public static int overrideBottleVolume;
	public static boolean wrenchEnabled;
	public static float tankBlockHardness;
	public static float tankBlockResistance;
	public static float valveBlockHardness;
	public static float valveBlockResistance;
	public static Recipe tankBlockRecipe;
	public static Recipe valveBlockRecipe;
	public static Recipe wrenchRecipe;

	// config file categories

	public static final String CATEGORY_MISC = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "misc";
	public static final String CATEGORY_BLOCKS = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "blocks";
	public static final String CATEGORY_RECIPES = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "recipes";

	private static final String CATEGORY_BLOCKS_TANKBLOCK = CATEGORY_BLOCKS + Configuration.CATEGORY_SPLITTER + "tank";
	private static final String CATEGORY_BLOCKS_VALVEBLOCK = CATEGORY_BLOCKS + Configuration.CATEGORY_SPLITTER + "valve";

	private static final String CATEGORY_RECIPES_TANKBLOCK = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "tank";
	private static final String CATEGORY_RECIPES_VALVEBLOCK = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "valve";
	private static final String CATEGORY_RECIPES_WRENCH = CATEGORY_RECIPES + Configuration.CATEGORY_SPLITTER + "wrench";

	/**
	 * Gets the loaded configuration.
	 * 
	 * @return
	 * The last loaded configuration or <code>null</code> if no config has been loaded yet.
	 */
	public static final Configuration getConfig()
	{
		return config;
	}

	/**
	 * Loads the mods settings from the specified file.
	 * 
	 * @param configFile
	 * The file to load the settings from.
	 */
	public static final void load(File configFile)
	{
		config = new Configuration(configFile);
		config.load();
		sync();
	}

	/**
	 * Synchronizes the config GUI and the config file.
	 */
	public static void sync()
	{
		Property prop;

		// misc

		config.getCategory(CATEGORY_MISC).setLanguageKey("configui.category.misc").setComment(StatCollector.translateToLocal("configui.category.misc.tooltip"));

		prop = config.get(CATEGORY_MISC, "bucketsPerTank", defaultBucketsPerTank);
		prop.comment = StatCollector.translateToLocal("configui.bucketsPerTank.tooltip");
		prop.setLanguageKey("configui.bucketsPerTank").setRequiresWorldRestart(true).setMinValue(1);
		bucketsPerTank = prop.getInt();

		prop = config.get(CATEGORY_MISC, "wrenchEnabled", defaultWrenchEnabled);
		prop.comment = StatCollector.translateToLocal("configui.wrenchEnabled.tooltip");
		prop.setLanguageKey("configui.wrenchEnabled").setRequiresMcRestart(true);
		wrenchEnabled = prop.getBoolean();

		prop = config.get(CATEGORY_MISC, "overrideBottleVolume", defaultOverrideBottleVolume);
		prop.comment = StatCollector.translateToLocal("configui.overrideBottleVolume.tooltip");
		prop.setLanguageKey("configui.overrideBottleVolume").setMinValue(0).setMaxValue(FluidContainerRegistry.BUCKET_VOLUME);
		overrideBottleVolume = prop.getInt();

		// blocks

		config.getCategory(CATEGORY_BLOCKS).setLanguageKey("configui.category.blocks").setComment(StatCollector.translateToLocal("configui.category.blocks.tooltip"));
		config.getCategory(CATEGORY_BLOCKS_TANKBLOCK).setLanguageKey("configui.category.tank");
		config.getCategory(CATEGORY_BLOCKS_VALVEBLOCK).setLanguageKey("configui.category.valve");

		String blockHardnessKey = "hardness";
		String blockResistanceKey = "resistance";

		String blockHardnessComment = StatCollector.translateToLocal("configui.blockHardness.tooltip");
		String blockResistanceComment = StatCollector.translateToLocal("configui.blockResistance.tooltip");

		prop = config.get(CATEGORY_BLOCKS_TANKBLOCK, blockHardnessKey, defaultTankBlockHardness, blockHardnessComment);
		prop.setLanguageKey("configui.blockHardness").setRequiresMcRestart(true).setMinValue(-1.0).setMaxValue(1000000.0);
		tankBlockHardness = (float) prop.getDouble();

		prop = config.get(CATEGORY_BLOCKS_TANKBLOCK, blockResistanceKey, defaultTankBlockResistance, blockResistanceComment);
		prop.setLanguageKey("configui.blockResistance").setRequiresMcRestart(true).setMinValue(1.0).setMaxValue(1000000.0);
		tankBlockResistance = (float) prop.getDouble();

		prop = config.get(CATEGORY_BLOCKS_VALVEBLOCK, blockHardnessKey, defaultValveBlockHardness, blockHardnessComment);
		prop.setLanguageKey("configui.blockHardness").setRequiresMcRestart(true).setMinValue(-1.0).setMaxValue(1000000.0);
		valveBlockHardness = (float) prop.getDouble();

		prop = config.get(CATEGORY_BLOCKS_VALVEBLOCK, blockResistanceKey, defaultValveBlockResistance, blockResistanceComment);
		prop.setLanguageKey("configui.blockResistance").setRequiresMcRestart(true).setMinValue(1.0).setMaxValue(1000000.0);
		valveBlockResistance = (float) prop.getDouble();

		// recipes

		config.getCategory(CATEGORY_RECIPES).setLanguageKey("configui.category.recipes").setComment(StatCollector.translateToLocal("configui.category.recipes.tooltip"));
		config.getCategory(CATEGORY_RECIPES_TANKBLOCK).setLanguageKey("configui.category.tank");
		config.getCategory(CATEGORY_RECIPES_VALVEBLOCK).setLanguageKey("configui.category.valve");
		config.getCategory(CATEGORY_RECIPES_WRENCH).setLanguageKey("configui.category.wrench");

		tankBlockRecipe = loadRecipe(config, CATEGORY_RECIPES_TANKBLOCK, Config.defaultTankBlockRecipe);
		valveBlockRecipe = loadRecipe(config, CATEGORY_RECIPES_VALVEBLOCK, Config.defaultValveBlockRecipe);
		wrenchRecipe = loadRecipe(config, CATEGORY_RECIPES_WRENCH, Config.defaultWrenchRecipe);

		if (config.hasChanged())
		{
			config.save();
		}
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
		Property prop = config.get(category, "shapeless", defaultRecipe.isShapeless);
		prop.comment = StatCollector.translateToLocal("configui.shapeless.tooltip");
		prop.setLanguageKey("configui.shapeless").setRequiresMcRestart(true);
		boolean shapeless = prop.getBoolean();

		prop = config.get(category, "yield", defaultRecipe.yield);
		prop.comment = StatCollector.translateToLocal("configui.yield.tooltip");
		prop.setLanguageKey("configui.yield").setRequiresMcRestart(true).setMinValue(1).setMaxValue(64);
		int yield = prop.getInt();

		prop = config.get(category, "components", defaultRecipe.getComponentList());
		prop.comment = StatCollector.translateToLocal("configui.components.tooltip");
		prop.setLanguageKey("configui.components").setRequiresMcRestart(true).setMaxListLength(27);
		String[] components = prop.getStringList();

		prop = config.get(category, "pattern", defaultRecipe.pattern.rows.toArray(new String[0])).setIsListLengthFixed(true).setMaxListLength(3);
		prop.comment = StatCollector.translateToLocal("configui.pattern.tooltip");
		prop.setLanguageKey("configui.pattern").setRequiresMcRestart(true);
		String[] pattern = prop.getStringList();

		return new Recipe(yield, (shapeless) ? null : new RecipePattern(pattern), Recipe.toComponents(components));
	}
}
