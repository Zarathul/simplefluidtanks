package net.zarathul.simplefluidtanks.configuration;

import com.google.common.collect.ImmutableList;

/**
 * Represents the pattern for a {@link Recipe}.
 */
public class RecipePattern
{
	public static final char EMPTY_SLOT = '-';

	public final ImmutableList<String> rows;

	public RecipePattern(String... rows)
	{
		this.rows = ImmutableList.copyOf(rows);
	}
}
