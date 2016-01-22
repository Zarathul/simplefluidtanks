package net.zarathul.simplefluidtanks.rendering;

import java.util.HashMap;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.zarathul.simplefluidtanks.blocks.TankBlock;

public class TankModelFactory implements ISmartBlockModel 
{
	public static final int FLUID_LEVELS = 16;
	
	// Fluid model cache: Array index corresponds to the fluid level, the HashMap key to the fluid id.
	public static final HashMap<Integer, IBakedModel>[] FLUID_MODELS = new HashMap[FLUID_LEVELS];
	
	static
	{
		for (int x = 0; x < FLUID_LEVELS; x++)
		{
			FLUID_MODELS[x] = new HashMap<Integer, IBakedModel>();
		}
	}
	
	private IBakedModel baseModel;
	
	public TankModelFactory(IBakedModel baseModel)
	{
		this.baseModel = baseModel;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGui3d()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public TextureAtlasSprite getTexture()
	{
		return baseModel.getTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IBakedModel handleBlockState(IBlockState state)
	{
		IExtendedBlockState exState = (IExtendedBlockState)state;
		
		return new BakedTankModel(
				baseModel,
				exState.getValue(TankBlock.FluidId),
				exState.getValue(TankBlock.FluidLevel),
				exState.getValue(TankBlock.CullFluidTop));
	}
}
