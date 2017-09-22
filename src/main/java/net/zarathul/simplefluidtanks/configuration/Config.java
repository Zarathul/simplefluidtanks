package net.zarathul.simplefluidtanks.configuration;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

/**
 * Provides helper methods to load the mods config.
 */
public final class Config
{
	private static Configuration config = null;

	// default values
	
	private static final int defaultBucketsPerTank = 32;
	private static final boolean defaultWrenchEnabled = true;
	private static final float defaultTankBlockHardness = 50;
	private static final float defaultTankBlockResistance = 1000;
	private static final float defaultValveBlockHardness = 50;
	private static final float defaultValveBlockResistance = 1000;

	// settings

	public static int bucketsPerTank;
	public static boolean wrenchEnabled;
	public static float tankBlockHardness;
	public static float tankBlockResistance;
	public static float valveBlockHardness;
	public static float valveBlockResistance;

	// config file categories

	public static final String CATEGORY_MISC = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "misc";
	public static final String CATEGORY_BLOCKS = Configuration.CATEGORY_GENERAL + Configuration.CATEGORY_SPLITTER + "blocks";

	private static final String CATEGORY_BLOCKS_TANKBLOCK = CATEGORY_BLOCKS + Configuration.CATEGORY_SPLITTER + "tank";
	private static final String CATEGORY_BLOCKS_VALVEBLOCK = CATEGORY_BLOCKS + Configuration.CATEGORY_SPLITTER + "valve";

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

		config.getCategory(CATEGORY_MISC).setLanguageKey("configui.category.misc").setComment(I18n.translateToLocal("configui.category.misc.tooltip"));

		prop = config.get(CATEGORY_MISC, "bucketsPerTank", defaultBucketsPerTank);
		prop.setComment(I18n.translateToLocal("configui.bucketsPerTank.tooltip"));
		prop.setLanguageKey("configui.bucketsPerTank").setRequiresWorldRestart(true).setMinValue(1);
		bucketsPerTank = prop.getInt();

		prop = config.get(CATEGORY_MISC, "wrenchEnabled", defaultWrenchEnabled);
		prop.setComment(I18n.translateToLocal("configui.wrenchEnabled.tooltip"));
		prop.setLanguageKey("configui.wrenchEnabled").setRequiresMcRestart(true);
		wrenchEnabled = prop.getBoolean();

		// blocks

		config.getCategory(CATEGORY_BLOCKS).setLanguageKey("configui.category.blocks").setComment(I18n.translateToLocal("configui.category.blocks.tooltip"));
		config.getCategory(CATEGORY_BLOCKS_TANKBLOCK).setLanguageKey("configui.category.tank");
		config.getCategory(CATEGORY_BLOCKS_VALVEBLOCK).setLanguageKey("configui.category.valve");

		String blockHardnessKey = "hardness";
		String blockResistanceKey = "resistance";

		String blockHardnessComment = I18n.translateToLocal("configui.blockHardness.tooltip");
		String blockResistanceComment = I18n.translateToLocal("configui.blockResistance.tooltip");

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

		if (config.hasChanged())
		{
			config.save();
		}
	}
}
