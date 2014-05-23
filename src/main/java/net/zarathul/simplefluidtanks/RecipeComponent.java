package net.zarathul.simplefluidtanks;

/**
 * Represents a component of a {@link Recipe}.
 */
public class RecipeComponent
{
	public final String identifier;
	public final String modId;
	public final String itemId;

	public RecipeComponent(String identifier, String modId, String itemId)
	{
		this.identifier = identifier;
		this.modId = modId;
		this.itemId = itemId;
	}
}
