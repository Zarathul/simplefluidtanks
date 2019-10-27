package net.zarathul.simplefluidtanks.blocks.blockstate;

import com.google.common.collect.ImmutableSet;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Optional;

public class ResourceLocationProperty extends Property<ResourceLocation>
{
	// Don't ask ...
	private static final String DELIMITER = "_42_";

	public static ResourceLocationProperty create(String name)
	{
		return  new ResourceLocationProperty(name, ResourceLocation.class);
	}

	private ResourceLocationProperty(String name, Class<ResourceLocation> valueClass)
	{
		super(name, valueClass);
	}

	@Override
	public Collection<ResourceLocation> getAllowedValues()
	{
		return ImmutableSet.copyOf(ForgeRegistries.FLUIDS.getKeys());
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
