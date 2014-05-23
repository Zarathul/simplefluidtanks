package net.zarathul.simplefluidtanks;

/**
 * Represents the pattern for a {@link Recipe}.
 */
public class RecipePattern
{
	public static final char EMPTY_SLOT = '_';

	public String[] rows;

	public RecipePattern(String... rows)
	{
		this.rows = rows;
	}
}
