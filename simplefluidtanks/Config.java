package simplefluidtanks;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Loads settings from config file.
 */
public final class Config
{
	public static final void load(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		
		SimpleFluidTanks.tankBlockId = config.getBlock(
				SimpleFluidTanks.CONFIG_CATEGORY_MAIN,
				SimpleFluidTanks.CONFIG_TANKBLOCK_ID_KEY,
				SimpleFluidTanks.CONFIG_DEFAULT_TANKBLOCK_ID,
				SimpleFluidTanks.CONFIG_TANKBLOCK_ID_COMMENT
				).getInt();
		
		SimpleFluidTanks.valveBlockId = config.getBlock(
				SimpleFluidTanks.CONFIG_CATEGORY_MAIN,
				SimpleFluidTanks.CONFIG_VALVEBLOCK_ID_KEY,
				SimpleFluidTanks.CONFIG_DEFAULT_VALVEBLOCK_ID,
				SimpleFluidTanks.CONFIG_VALVEBLOCK_ID_COMMENT
				).getInt();
		
		SimpleFluidTanks.bucketsPerTank = config.get(
				SimpleFluidTanks.CONFIG_CATEGORY_MAIN,
				SimpleFluidTanks.CONFIG_BUCKETS_PER_TANK_KEY,
				SimpleFluidTanks.CONFIG_DEFAULT_BUCKETS_PER_TANK,
				SimpleFluidTanks.CONFIG_BUCKETS_PER_TANK_COMMENT
				).getInt();
		
		SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_MOD_ID = config.get(
				SimpleFluidTanks.CONFIG_CATEGORY_MOD_INTEROP,
				SimpleFluidTanks.CONFIG_TE_MOD_ID_KEY,
				SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_MOD_ID,
				SimpleFluidTanks.CONFIG_TE_MOD_ID_COMMENT
				).getString();
		
		SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_HARDENED_GLASS = config.get(
				SimpleFluidTanks.CONFIG_CATEGORY_MOD_INTEROP,
				SimpleFluidTanks.CONFIG_TE_MOD_HARDENED_GLASS_KEY,
				SimpleFluidTanks.REGISTRY_THERMAL_EXPANSION_HARDENED_GLASS,
				SimpleFluidTanks.CONFIG_TE_MOD_HARDENED_GLASS_COMMENT
				).getString();
		
		config.save();
	}
}
