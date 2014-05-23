package net.zarathul.simplefluidtanks;

import java.util.ArrayList;
import java.util.Arrays;

import net.zarathul.simplefluidtanks.common.Utils;

/**
 * Represents recipe data from the config file. Does not contain any information
 * on the crafted items besides the amount.
 */
public class Recipe
{
	public int yield;
	public RecipePattern pattern;
	public RecipeComponent[] components;

	public Recipe(int yield, RecipePattern pattern, RecipeComponent[] components)
	{
		this.yield = yield;
		this.pattern = pattern;
		this.components = components;
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
