package net.zarathul.simplefluidtanks.rendering;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;

public class BakedTankModel implements IBakedModel
{
	private IBakedModel baseModel;
	private String fluidName;
	private int fluidLevel;
	private boolean cullFluidTop;
	
	public BakedTankModel(IBakedModel baseModel, String fluidId, int fluidLevel, boolean cullFluidTop)
	{
		this.baseModel = baseModel;
		this.fluidName = fluidId;
		this.fluidLevel = fluidLevel;
		this.cullFluidTop = cullFluidTop;
	}

	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing facing)
	{
		List<BakedQuad> faceQuads = new LinkedList<BakedQuad>();
		
		faceQuads.addAll(baseModel.getFaceQuads(facing));
		
		if (fluidLevel > 0 && fluidLevel <= TankModelFactory.FLUID_LEVELS)
		{
			HashMap<String, IBakedModel> fluidModels = TankModelFactory.FLUID_MODELS[fluidLevel - 1];
			
			if (fluidModels.containsKey(fluidName))
			{
				faceQuads.addAll(fluidModels.get(fluidName).getFaceQuads(facing));
			}
		}
		
		return faceQuads;
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		List<BakedQuad> generalQuads = new LinkedList<BakedQuad>();
		
		generalQuads.addAll(baseModel.getGeneralQuads());
		
		if (fluidLevel > 0 && fluidLevel <= TankModelFactory.FLUID_LEVELS)
		{
			HashMap<String, IBakedModel> fluidModels = TankModelFactory.FLUID_MODELS[fluidLevel - 1];
			
			if (fluidModels.containsKey(fluidName))
			{
				// The fluid model needs a separate culling logic from the rest of the tank, 
				// because the top of the fluid is supposed to be visible if the tank block 
				// above is empty. (getGeneralQuads() handles quads that don't have a cullface
				// annotation in the .json)
				
				if (cullFluidTop)
				{
					for (BakedQuad quad : fluidModels.get(fluidName).getGeneralQuads())
					{
						if (quad.getFace() != EnumFacing.UP) generalQuads.add(quad);
					}
				}
				else
				{
					generalQuads.addAll(fluidModels.get(fluidName).getGeneralQuads());
				}
			}
		}
		
		return generalQuads;
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
}
