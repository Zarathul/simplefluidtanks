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
	
	// Fluid model cache: The HashMap key corresponds to the fluid name, the model array index to the fluid level.
	public static final HashMap<String, IBakedModel[]> FLUID_MODELS = new HashMap<>();
	
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
		
		// The top quad of the fluid model needs a separate culling logic from the 
		// rest of the tank, because the top needs to be visible if the tank isn't
		// full, even if there's a tank above.
		// (Note that 'side' is null for quads that don't have a cullface annotation in the .json.
		// The top side of the fluid model has a cullface annotation, the other sides don't.)
		if (side != null || !cullFluidTop)
		{
			if (fluidLevel > 0 && fluidLevel <= FLUID_LEVELS && FLUID_MODELS.containsKey(fluidName))
			{
				IBakedModel fluidModel = FLUID_MODELS.get(fluidName)[fluidLevel - 1];
				quads.addAll(fluidModel.getQuads(null, side, rand));
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
