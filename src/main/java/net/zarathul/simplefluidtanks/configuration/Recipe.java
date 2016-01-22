package net.zarathul.simplefluidtanks.configuration;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.zarathul.simplefluidtanks.common.Utils;

/**
 * Represents recipe data from the config file. Does not contain any information
 * on the crafted items besides the amount.
 */
public class Recipe
{
	public final boolean isShapeless;
	public final int yield;
	public final RecipePattern pattern;
	public final RecipeComponent[] components;

	public Recipe(int yield, RecipeComponent[] components)
	{
		this(yield, null, components, true);
	}

	public Recipe(int yield, RecipePattern pattern, RecipeComponent[] components)
	{
		this(yield, pattern, components, (pattern == null));
	}

	private Recipe(int yield, RecipePattern pattern, RecipeComponent[] components, boolean isShapeless)
	{
		this.yield = yield;
		this.pattern = (isShapeless) ? null : pattern;
		this.components = components;
		this.isShapeless = isShapeless;
	}

	/**
	 * Generates a string array representation of the recipes components.
	 * 
	 * @return
	 * The recipes components in an string array or <code>null</code> if this
	 * recipe has no components.
	 */
	public String[] getComponentList()
	{
		if (components == null || components.length == 0) return null;

		ArrayList<String> componentList = new ArrayList<String>(components.length * 3);

		for (RecipeComponent component : components)
		{
			componentList.add(component.identifier);
			componentList.add(component.modId);
			componentList.add(component.itemId);
		}

		return componentList.toArray(new String[componentList.size()]);
	}

	/**
	 * Generates the arguments for the Forge recipe registration API call
	 * from the specified recipe.
	 * 
	 * @param recipe
	 * The recipe to get the arguments for.
	 * @return
	 * The generated arguments or <code>null</code> if a component of the recipe
	 * could't be found or if an component identifier is missing.
	 */
	public Object[] getRegistrationArgs()
	{
		ArrayList<Object> args = new ArrayList<Object>();

		if (!isShapeless)
		{
			for (String patternRow : pattern.rows)
			{
				args.add((patternRow.length() > 3) ? patternRow.substring(0, 3) : patternRow);
			}
		}

		Object componentArg = null;
		char id = 0;

		for (RecipeComponent component : components)
		{
			componentArg = (!component.modId.equals(RecipeComponent.OREDICT_IDENTIFIER)) ? GameRegistry.findItem(component.modId, component.itemId) : component.itemId;

			if (componentArg == null) return null;

			if (!isShapeless)
			{
				if (component.identifier == null || component.identifier.length() == 0) return null;

				id = component.identifier.charAt(0);

				if (id == RecipeComponent.EMPTY_IDENTIFIER.charAt(0)) return null;

				args.add(id);
			}

			args.add(componentArg);
		}

		return args.toArray();
	}

	/**
	 * Generates recipe components from the specified string array.
	 * 
	 * @param components
	 * The strings to generate the components from.
	 * @return
	 * The generated recipe components or <code>null</code> if the string array
	 * is <code>null</code>, has an invalid length or contains <code>null</code> or empty strings.
	 */
	public static RecipeComponent[] toComponents(String[] components)
	{
		if (components == null || components.length % 3 != 0 || !Utils.notNullorEmpty(Arrays.asList(components))) return null;

		int componentCount = components.length / 3;
		RecipeComponent[] componentList = new RecipeComponent[componentCount];

		for (int x = 0; x < componentCount; x++)
		{
			componentList[x] = new RecipeComponent(components[x * 3], components[x * 3 + 1], components[x * 3 + 2]);
		}

		return componentList;
	}
}
