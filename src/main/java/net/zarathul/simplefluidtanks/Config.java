package net.zarathul.simplefluidtanks;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Provides helper methods to load the mods config.
 */
public final class Config
{
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
		
		SimpleFluidTanks.bucketsPerTank = config.get(
				Config.CATEGORY_MAIN,
				Config.BUCKETS_PER_TANK_KEY,
				SimpleFluidTanks.bucketsPerTank,
				Config.BUCKETS_PER_TANK_COMMENT
				).getInt();
		
		SimpleFluidTanks.thermalExpansionModId = config.get(
				Config.CATEGORY_MOD_INTEROP,
				Config.TE_MOD_ID_KEY,
				SimpleFluidTanks.thermalExpansionModId,
				Config.TE_MOD_ID_COMMENT
				).getString();
		
		SimpleFluidTanks.thermalExpansionHardenedGlass = config.get(
				Config.CATEGORY_MOD_INTEROP,
				Config.TE_MOD_HARDENED_GLASS_KEY,
				SimpleFluidTanks.thermalExpansionHardenedGlass,
				Config.TE_MOD_HARDENED_GLASS_COMMENT
				).getString();
		
		SimpleFluidTanks.thermalExpansionBronzeIngot = config.get(
				Config.CATEGORY_MOD_INTEROP,
				Config.TE_MOD_BRONZE_INGOT_KEY,
				SimpleFluidTanks.thermalExpansionBronzeIngot,
				Config.TE_MOD_BRONZE_INGOT_COMMENT
				).getString();
		
		config.save();
	}
}
