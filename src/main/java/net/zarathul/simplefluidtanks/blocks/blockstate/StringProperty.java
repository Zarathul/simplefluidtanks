package net.zarathul.simplefluidtanks.blocks.blockstate;

import com.google.common.collect.ImmutableList;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Optional;

public class StringProperty extends Property<ResourceLocation>
{
	// Don't ask ...
	private static final String DELIMITER = "_42_";

	public static StringProperty create(String name)
	{
		return  new StringProperty(name, ResourceLocation.class);
	}

	private StringProperty(String name, Class<ResourceLocation> valueClass)
	{
		super(name, valueClass);
	}

	@Override
	public Collection<ResourceLocation> getAllowedValues()
	{
		return ImmutableList.copyOf(ForgeRegistries.FLUIDS.getKeys());
	}

	@Override
	public Optional<ResourceLocation> parseValue(String value)
	{
		String[] components = value.split(DELIMITER);
		return Optional.of(new ResourceLocation(components[0], components[1]));
	}

	@Override
	public String getName(ResourceLocation value)
	{
		return value.getNamespace() + DELIMITER + value.getPath();
	}
}
