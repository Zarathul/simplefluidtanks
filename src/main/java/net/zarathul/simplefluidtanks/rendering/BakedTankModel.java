package net.zarathul.simplefluidtanks.rendering;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import net.zarathul.simplefluidtanks.blocks.TankBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BakedTankModel implements IBakedModel
{
	public static final int FLUID_LEVELS = 16;

	// Fluid model cache: The HashMap key corresponds to the fluid name, the model array index to the fluid level.
	public static final HashMap<ResourceLocation, IBakedModel[]> FLUID_MODELS = new HashMap<>();
	
	private IBakedModel baseModel;
	
	public BakedTankModel(IBakedModel baseModel)
	{
		this.baseModel = baseModel;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
	{
		// WTF TODO here?
		return null;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		// TODO: why the fuck is this a linked list ?
		List<BakedQuad> quads = new LinkedList<BakedQuad>();

		if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED)
		{
			// Frame

			quads.addAll(baseModel.getQuads(state, side, rand));
		}
		else if (MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT)
		{
			// Fluid

			boolean cullFluidTop = state.get(TankBlock.CullFluidTop);
			int fluidLevel = state.get(TankBlock.FluidLevel);
			ResourceLocation fluidName = state.get(TankBlock.FluidName);

			// The top quad of the fluid model needs a separate culling logic from the
			// rest of the tank, because the top needs to be visible if the tank isn't
			// full, even if there's a tank above.
			// (Note that 'side' is null for quads that don't have a cullface annotation in the .json.
			// The tank model has cullface annotations for every side.)
			if ((side != null && side != Direction.UP) || (side == null && !cullFluidTop))
			{
				if (fluidLevel > 0 && fluidLevel <= FLUID_LEVELS && FLUID_MODELS.containsKey(fluidName))
				{
					IBakedModel fluidModel = FLUID_MODELS.get(fluidName)[fluidLevel - 1];
					quads.addAll(fluidModel.getQuads(null, side, rand));
				}
			}
		}

		return quads;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return false;
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
	public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data)
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
