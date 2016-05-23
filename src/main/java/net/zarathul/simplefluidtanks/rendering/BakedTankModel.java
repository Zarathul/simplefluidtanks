package net.zarathul.simplefluidtanks.rendering;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.zarathul.simplefluidtanks.blocks.TankBlock;

public class BakedTankModel implements IBakedModel
{
	public static final int FLUID_LEVELS = 16;
	
	// Fluid model cache: Array index corresponds to the fluid level, the HashMap key to the fluid name.
	public static final HashMap<String, IBakedModel>[] FLUID_MODELS = new HashMap[FLUID_LEVELS];
	
	static
	{
		for (int x = 0; x < FLUID_LEVELS; x++)
		{
			FLUID_MODELS[x] = new HashMap<String, IBakedModel>();
		}
	}
	
	private IBakedModel baseModel;
	
	public BakedTankModel(IBakedModel baseModel)
	{
		this.baseModel = baseModel;
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
	{
		IExtendedBlockState exState = (IExtendedBlockState)state;
		boolean cullFluidTop = exState.getValue(TankBlock.CullFluidTop);
		int fluidLevel = exState.getValue(TankBlock.FluidLevel);
		String fluidName = exState.getValue(TankBlock.FluidName);
		
		List<BakedQuad> quads = new LinkedList<BakedQuad>();
		quads.addAll(baseModel.getQuads(state, side, rand));
		
		if (fluidLevel > 0 && fluidLevel <= FLUID_LEVELS)
		{
			HashMap<String, IBakedModel> fluidModels = FLUID_MODELS[fluidLevel - 1];
			
			if (fluidModels.containsKey(fluidName))
			{
				// The top quad of the fluid model needs a separate culling logic from the 
				// rest of the tank, because the top needs to be visible if the tank isn't
				// full, even if there's a tank above.
				// (side is null for quads that don't have a cullface annotation in the .json)
				if (side != null || !cullFluidTop)
				{
					quads.addAll(fluidModels.get(fluidName).getQuads(null, side, rand));
				}
			}
		}
		
		return quads;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return baseModel.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return baseModel.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return baseModel.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return baseModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return null;
	}
}
