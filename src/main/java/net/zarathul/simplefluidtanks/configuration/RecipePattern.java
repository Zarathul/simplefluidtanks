package net.zarathul.simplefluidtanks.configuration;

/**
 * Represents the pattern for a {@link Recipe}.
 */
public class RecipePattern
{
	public static final char EMPTY_SLOT = '-';

	public final String[] rows;

	public RecipePattern(String... rows)
	{
		this.rows = rows;
	}
}
