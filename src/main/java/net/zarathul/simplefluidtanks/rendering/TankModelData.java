package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;

public class TankModelData implements IModelData
{
	public ResourceLocation fluidName;
	public int fillLevel;
	public boolean cullFluidTop;

	public TankModelData(ResourceLocation fluidName, int fillLevel, boolean cullFluidTop)
	{
		this.fluidName = fluidName;
		this.fillLevel = fillLevel;
		this.cullFluidTop = cullFluidTop;
	}

	@Override
	public boolean hasProperty(ModelProperty<?> prop)
	{
		return false;
	}

	@Nullable
	@Override
	public <T> T getData(ModelProperty<T> prop)
	{
		return null;
	}

	@Nullable
	@Override
	public <T> T setData(ModelProperty<T> prop, T data)
	{
		return null;
	}
}
