package net.zarathul.simplefluidtanks.common;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.rendering.BakedTankModel;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * General utility class.
 */
public final class Utils
{
	/**
	 * Gets the {@link TileEntity} at the specified coordinates, cast to the specified type.
	 * 
	 * @param world
	 * The world.
	 * @param tileType
	 * The type the {@link TileEntity} should be cast to.
	 * @param pos
	 * The coordinates of the {@link TileEntity}.
	 * @return The {@link TileEntity} or <code>null</code> if no {@link TileEntity} was found or the types didn't match.
	 */
 	public static final <T extends TileEntity> T getTileEntityAt(World world, Class<T> tileType, BlockPos pos)
	{
		if (world != null && tileType != null && pos != null)
		{
			TileEntity tile = world.getChunkAt(pos).getTileEntity(pos, Chunk.CreateEntityType.CHECK);

			if (tile != null && tile.getClass() == tileType)
			{
				return (T) tile;
			}
		}

		return null;
	}
	
	/**
	 * Causes rerender of the block without triggering a block update.
	 * Also synchronizes TileEntity data with the client if called server side.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The location of the block.
	 */
	public static final void syncBlockAndRerender(World world, BlockPos pos)
	{
		if (world == null || pos == null) return;
		
		BlockState state = world.getBlockState(pos);
		
		world.markAndNotifyBlock(pos, null, state, state, 2);
	}

	/**
	 * Gets the {@link ValveBlock}s {@link TileEntity} for a linked {@link TankBlock}.
	 * 
	 * @return The valves {@link ValveBlockEntity}<br>
	 * or<br>
	 * <code>null</code> if no linked {@link ValveBlock} was found.
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link TankBlock}s coordinates.
	 */
	public static ValveBlockEntity getValve(World world, BlockPos pos)
	{
		if (world != null && pos != null)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);

			if (tankEntity != null)
			{
				ValveBlockEntity valveEntity = tankEntity.getValve();

				return valveEntity;
			}
		}

		return null;
	}

	/**
	 * A predicate that returns <code>true</code> if passed string is neither <code>null</code> nor empty.
	 */
	private static final Predicate<String> stringNotNullOrEmpty = new Predicate<String>()
	{
		@Override
		public boolean apply(String item)
		{
			return !Strings.isNullOrEmpty(item);
		}
	};

	/**
	 * Checks a list of strings for <code>null</code> and empty elements.
	 * 
	 * @param items
	 * The list of strings to check.
	 * @return
	 * <code>true</code> if the list neither contains <code>null</code> elements nor empty strings, otherwise <code>false</code>.
	 */
	public static final boolean notNullorEmpty(Iterable<String> items)
	{
		return Iterables.all(items, stringNotNullOrEmpty);
	}

	/**
	 * Gets the localized formatted strings for the specified key and formatting arguments.
	 * 
	 * @param key
	 * The base key without an index (e.g. "myKey" gets "myKey0", "myKey1" ... etc.).
	 * @param args
	 * Formatting arguments.
	 * @return
	 * A list of localized ITextComponents for the specified key, or an empty list if the key was not found.
	 */
	public static final ArrayList<ITextComponent> multiLineTranslateToLocal(String key, Object... args)
	{
		ArrayList<ITextComponent> lines = new ArrayList<>();

		if (key != null)
		{
			int x = 0;
			String currentKey = key + x;
			LanguageMap I18nMap = LanguageMap.getInstance();

			while (I18nMap.exists(currentKey))
			{
				// Get translated text and apply formatting because 'translateToLocalFormatted()' is gone .. yay ....
				lines.add(new StringTextComponent(String.format(I18nMap.translateKey(currentKey), args)));
				currentKey = key + ++x;
			}
		}

		return lines;
	}
	
	/**
	 * Calculates the fluid level for the specified fill percentage.
	 * 
	 * @param fillPercentage
	 * The fill percentage.
	 * @return
	 * A value between 0 and {@code BakedTankModel.FLUID_LEVELS} (inclusive).
	 */
	public static int getFluidLevel(int fillPercentage)
	{
		int level = (int)Math.round((fillPercentage / 100.0d) * BakedTankModel.FLUID_LEVELS);
		
		// Make sure that even for small amounts the fluid is rendered at the first level.
		return (fillPercentage > 0) ? Math.max(1, level) : 0;
	}
	
	/**
	 * Calculates the comparator redstone signal strength based on the quotient of the specified values.
	 * 
	 * @param numerator
	 * The numerator.
	 * @param denominator
	 * The denominator.
	 * @return
	 * A value between 0 and 15.
	 */
	public static int getComparatorLevel(float numerator, float denominator)
	{
		int level = (denominator != 0) ? ((int) Math.floor((numerator / denominator) * 14.0f)) + ((numerator > 0) ? 1 : 0) : 0;
		
		return level;
	}

	// Belongs to getMetricFormattedNumber
	private static final int FACTOR = 1000;
	private static final double FACTOR_LOG = Math.log(FACTOR);
	private static final char[] METRIC_SUFFIXES = { 'k', 'M', 'G', 'T', 'P', 'E' };

	/**
	 * Shortens a number using metric suffixes and applies the provided format and locale.
	 *
	 * @param number
	 * The number to shorten. Numbers lower than 1000 remain unchanged.
	 * @param shortFormat
	 * The string format to apply in case {@code number} gets shortened (3 arguments). The first argument is the
	 * shortened number (floating point), the second is the metric suffix and the third is the passed in object
	 * ({@code misc}) which can be anything.
	 * @param longFormat
	 * The string format to apply in case {@code number} is not shortened (2 arguments). The first argument is the
	 * unmodified number (decimal) and the second is the passed in object ({@code misc}) which can be anything.
	 * @param locale
	 * The locale to use for the number format.
	 * @param misc
	 * Can be used to add additional text to the output string.
	 * @return
	 * <c>null</c> if either {@code shortFormat}, {@code longFormat} or {@code locale} is <c>null</c>, otherwise the
	 * potentially shortened and formatted number.
	 */
	public static String getMetricFormattedNumber(long number, String shortFormat, String longFormat, Locale locale, Object misc)
	{
		if (shortFormat == null || longFormat == null || locale == null) return null;
		if (number < FACTOR) return String.format(locale, longFormat, number, misc);

		int exponent = (int)(Math.log(number) / FACTOR_LOG);

		return String.format(locale, shortFormat, number / Math.pow(FACTOR, exponent), METRIC_SUFFIXES[exponent - 1], misc);
	}

	/**
	 * Shortens a number using metric suffixes and applies the provided format (UK locale).
	 *
	 * @param number
	 * The number to shorten. Numbers lower than 1000 remain unchanged.
	 * @param shortFormat
	 * The string format to apply in case {@code number} gets shortened (3 arguments). The first argument is the
	 * shortened number (floating point), the second is the metric suffix and the third is the passed in object
	 * ({@code misc}) which can be anything.
	 * @param longFormat
	 * The string format to apply in case {@code number} is not shortened (2 arguments). The first argument is the
	 * unmodified number (decimal) and the second is the passed in object ({@code misc}) which can be anything.
	 * @param misc
	 * Can be used to add additional text to the output string.
	 * @return
	 * <c>null</c> if either {@code shortFormat}, {@code longFormat} or {@code locale} is <c>null</c>, otherwise the
	 * potentially shortened and formatted number.
	 */
	public static String getMetricFormattedNumber(long number, String shortFormat, String longFormat, Object misc)
	{
		return getMetricFormattedNumber(number, shortFormat, longFormat, Locale.UK, misc);
	}

	/**
	 * Cache for {@code isInterfaceAvailable()} return values.
	 */
	private static HashMap<String, Boolean> InterfaceLookupCache = new HashMap<>();

	/**
	 * Checks if the given interface is available. This is used to call into APIs of other mods that may not always be
	 * there.
	 *
	 * @param packageName
	 * The name of the package containing the interface.
	 * @param interfaceName
	 * The name of the interface to check.
	 *
	 * @return
	 * <c>true</c> if the interface exists, otherwise <c>false</c>.
	 */
	public static boolean isInterfaceAvailable(String packageName, String interfaceName)
	{
		String FullyQualifiedName = packageName + "." + interfaceName;

		if (InterfaceLookupCache.containsKey(FullyQualifiedName)) return InterfaceLookupCache.get(FullyQualifiedName);

		try
		{
			Class<?> Interface = Class.forName(FullyQualifiedName);
			InterfaceLookupCache.put(FullyQualifiedName, true);

			return true;
		}
		catch (Exception e)
		{
			InterfaceLookupCache.put(FullyQualifiedName, false);

			return false;
		}
	}

	/**
	 * Checks if an item is a wrench.
	 *
	 * @param item
	 * The item to check.
	 *
	 * @return
	 * <c>true</c> if the item is a wrench, otherwise <c>false</c>.
	 */
	public static boolean isWrenchItem(Item item)
	{
		// TODO: Add support for other wrenches or tools
		return (item == SimpleFluidTanks.wrenchItem/*
		|| (Utils.isInterfaceAvailable("cofh.api.item", "IToolHammer") && item instanceof IToolHammer)
		|| (Utils.isInterfaceAvailable("blusunrize.immersiveengineering.api.tool", "ITool") && item instanceof ITool)
		|| (Utils.isInterfaceAvailable("appeng.api.implementations.items", "IAEWrench") && item instanceof IAEWrench)*/);
	}
}
