package net.zarathul.simplefluidtanks.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

/**
 * Provides helper methods to load the mods config.
 */
public final class Config
{
	// configs

	public static ForgeConfigSpec CommonConfigSpec;

	// config builders

	private static final ForgeConfigSpec.Builder CommonConfigBuilder = new ForgeConfigSpec.Builder();

	// default values
	
	private static final int defaultBucketsPerTank = 32;
	private static final float defaultTankBlockHardness = 50;
	private static final float defaultTankBlockResistance = 1000;
	private static final float defaultValveBlockHardness = 50;
	private static final float defaultValveBlockResistance = 1000;

	// settings

	public static ForgeConfigSpec.IntValue bucketsPerTank;
	public static ForgeConfigSpec.DoubleValue tankBlockHardness;
	public static ForgeConfigSpec.DoubleValue tankBlockResistance;
	public static ForgeConfigSpec.DoubleValue valveBlockHardness;
	public static ForgeConfigSpec.DoubleValue valveBlockResistance;

	static
	{
		// misc

		CommonConfigBuilder.push("misc");

		bucketsPerTank = CommonConfigBuilder.translation("config.buckets_per_tank")
				.comment("The amount of fluid a tank can hold measured in buckets.")
				.worldRestart()
				.defineInRange("bucketsPerTank", defaultBucketsPerTank, 1, Integer.MAX_VALUE);

		CommonConfigBuilder.pop();

		// blocks

		CommonConfigBuilder.push("blocks");

		tankBlockHardness = CommonConfigBuilder.translation("config.tankblock_hardness")
				.comment("The amount of hits the block can take before it breaks (-1 = indestructible).")
				.defineInRange("tankBlockHardness", defaultTankBlockHardness, -1.0d, Float.MAX_VALUE);

		tankBlockResistance = CommonConfigBuilder.translation("config.tankblock_resistance")
				.comment("The blocks resistance to explosions.")
				.defineInRange("tankBlockResistance", defaultTankBlockResistance, 1.0d, Float.MAX_VALUE);

		valveBlockHardness = CommonConfigBuilder.translation("config.valveblock_hardness")
				.comment("The amount of hits the block can take before it breaks (-1 = indestructible).")
				.defineInRange("valveBlockHardness", defaultValveBlockHardness, -1.0d, Float.MAX_VALUE);

		valveBlockResistance = CommonConfigBuilder.translation("config.valveblock_resistance")
				.comment("The blocks resistance to explosions.")
				.defineInRange("valveBlockResistance", defaultValveBlockResistance, 1.0d, Float.MAX_VALUE);

		CommonConfigBuilder.pop();
		CommonConfigSpec = CommonConfigBuilder.build();
	}

	/**
	 * Loads the mods settings from the specified file.
	 *
	 * @param configSpec
	 * The specification for the contents of the config file.
	 * @param path
	 * The path to the config file.	 */
	public static final void load(ForgeConfigSpec configSpec, Path path)
	{
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.writingMode(WritingMode.REPLACE)
				.build();

		configData.load();
		configSpec.setConfig(configData);
	}
}
