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
	private int fluidId;
	private int fluidLevel;
	private boolean cullFluidTop;
	
	public BakedTankModel(IBakedModel baseModel, int fluidId, int fluidLevel, boolean cullFluidTop)
	{
		this.baseModel = baseModel;
		this.fluidId = fluidId;
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
			HashMap<Integer, IBakedModel> fluidModels = TankModelFactory.FLUID_MODELS[fluidLevel - 1];
			
			if (fluidModels.containsKey(fluidId))
			{
				faceQuads.addAll(fluidModels.get(fluidId).getFaceQuads(facing));
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
			HashMap<Integer, IBakedModel> fluidModels = TankModelFactory.FLUID_MODELS[fluidLevel - 1];
			
			if (fluidModels.containsKey(fluidId))
			{
				// The fluid model needs a separate culling logic from the rest of the tank, 
				// because the top of the fluid is supposed to be visible if the tank block 
				// above is empty. (getGeneralQuads() handles quads that don't have a cullface
				// annotation in the .json)
				
				if (cullFluidTop)
				{
					for (BakedQuad quad : fluidModels.get(fluidId).getGeneralQuads())
					{
						if (quad.getFace() != EnumFacing.UP) generalQuads.add(quad);
					}
				}
				else
				{
					generalQuads.addAll(fluidModels.get(fluidId).getGeneralQuads());
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
	public TextureAtlasSprite getTexture()
	{
		return baseModel.getTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return baseModel.getItemCameraTransforms();
	}
}
