package net.zarathul.simplefluidtanks.configuration;

/**
 * Represents a component of a {@link Recipe}.
 */
public class RecipeComponent
{
	public static final String EMPTY_IDENTIFIER = "-";

	public final String identifier;
	public final String modId;
	public final String itemId;

	public RecipeComponent(String modId, String itemId)
	{
		this(EMPTY_IDENTIFIER, modId, itemId);
	}

	public RecipeComponent(String identifier, String modId, String itemId)
	{
		this.identifier = identifier;
		this.modId = modId;
		this.itemId = itemId;
	}
}
