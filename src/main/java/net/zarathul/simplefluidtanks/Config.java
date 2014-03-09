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
	public static String thermalExpansionModId = "ThermalExpansion";
	public static String thermalExpansionHardenedGlass = "glassHardened";
	public static String thermalExpansionBronzeIngot = "ingotBronze";
	
	// config file comments etc.
	private static final String CATEGORY_MAIN = "simple fluid tanks";
	private static final String BUCKETS_PER_TANK_KEY = "BucketsPerTank";
	private static final String BUCKETS_PER_TANK_COMMENT = "The amount of fluid that can be stored per tank (measured in buckets).";
	private static final String CATEGORY_MOD_INTEROP = "mod interop";
	private static final String TE_MOD_ID_KEY = "ThermalExpansionModId";
	private static final String TE_MOD_ID_COMMENT = "The mod id for Thermal Expansion. This is used to include TE items in the recipes.";
	private static final String TE_MOD_HARDENED_GLASS_KEY = "TE_HardenedGlass";
	private static final String TE_MOD_HARDENED_GLASS_COMMENT = "The game registry key of Thermal Expansions hardened glass item.";
	private static final String TE_MOD_BRONZE_INGOT_KEY = "TE_BronzeIngots";
	private static final String TE_MOD_BRONZE_INGOT_COMMENT = "The game registry key of Thermal Expansions bronze(tinkers alloy) ingot .";
	
	/**
	 * Loads the mods settings from the specified file.
	 * @param configFile
	 * The file to load the settings from.
	 */
	public static final void load(File configFile)
	{
		Configuration config = new Configuration(configFile);
		config.load();
		
		bucketsPerTank = config.get(
			CATEGORY_MAIN,
			BUCKETS_PER_TANK_KEY,
			bucketsPerTank,
			BUCKETS_PER_TANK_COMMENT
			).getInt();
		
		thermalExpansionModId = config.get(
			CATEGORY_MOD_INTEROP,
			TE_MOD_ID_KEY,
			thermalExpansionModId,
			TE_MOD_ID_COMMENT
			).getString();
		
		thermalExpansionHardenedGlass = config.get(
			CATEGORY_MOD_INTEROP,
			TE_MOD_HARDENED_GLASS_KEY,
			thermalExpansionHardenedGlass,
			TE_MOD_HARDENED_GLASS_COMMENT
			).getString();
		
		thermalExpansionBronzeIngot = config.get(
			CATEGORY_MOD_INTEROP,
			TE_MOD_BRONZE_INGOT_KEY,
			thermalExpansionBronzeIngot,
			TE_MOD_BRONZE_INGOT_COMMENT
			).getString();
		
		config.save();
	}
}
