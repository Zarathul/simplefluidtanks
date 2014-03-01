package net.zarathul.simplefluidtanks.common;

import java.util.ArrayList;

import net.minecraft.util.StatCollector;

/**
 * Provides helper methods for retrieving localized strings
 */
public final class LocalizationHelper
{
	/**
	 * Gets the localized formatted strings for the specified key and formatting arguments.
	 * @param key
	 * The base key without an index (e.g. "myKey" gets "myKey0", "myKey1" ... etc.).
	 * @param args
	 * Formatting arguments.
	 * @return
	 */
	public static ArrayList<String> multiLineTranslateToLocal(String key, Object ... args)
	{
		ArrayList<String> lines = new ArrayList<String>();
		
		if (key != null)
		{
			int x = 0;
			String currentKey = key + x;
			
			// func_94522_b checks if the specified key exists
			while (StatCollector.func_94522_b(currentKey))
			{
				lines.add(StatCollector.translateToLocalFormatted(currentKey, args));
				currentKey = key + ++x;
			}
		}
		
		return lines;
	}
}
