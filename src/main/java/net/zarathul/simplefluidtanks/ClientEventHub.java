package net.zarathul.simplefluidtanks;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.zarathul.simplefluidtanks.rendering.BakedTankFluidModel;
import net.zarathul.simplefluidtanks.rendering.BakedTankModel;

import java.util.ArrayList;
import java.util.Map;

public class ClientEventHub
{
	@SubscribeEvent
	public static void onModelBakeEvent(ModelBakeEvent event)
	{
		// generate fluid models for all registered fluids for 16 levels each

		Fluid fluid;
		IBakedModel[] bakedFluidModels;

		for (Map.Entry<ResourceLocation, Fluid> entry : ForgeRegistries.FLUIDS.getEntries())
		{
			fluid = entry.getValue();
			// Why the EMPTY fluid and the flowing variants are in the registry now is beyond me.
			if (fluid == Fluids.EMPTY || fluid == Fluids.FLOWING_WATER || fluid == Fluids.FLOWING_LAVA) continue;

			bakedFluidModels = new IBakedModel[BakedTankModel.FLUID_LEVELS];

			for (int x = 0; x < BakedTankModel.FLUID_LEVELS; x++)
			{
				bakedFluidModels[x] = new BakedTankFluidModel(fluid, x + 1);
			}

			BakedTankModel.FLUID_MODELS.put(entry.getKey(), bakedFluidModels);
		}

		// get ModelResourceLocations of all tank block variants from the registry except "inventory"

		Map<ResourceLocation, IBakedModel> registry = event.getModelRegistry();
		ArrayList<ResourceLocation> modelLocations = Lists.newArrayList();

		// as of 1.11.2 (maybe earlier) all resource names must be all lower case
		String modelPath = SimpleFluidTanks.TANK_BLOCK_NAME;

		for (ResourceLocation modelLoc : registry.keySet())
		{
			if (modelLoc instanceof ModelResourceLocation &&
				modelLoc.getNamespace().equals(SimpleFluidTanks.MOD_ID) &&
				modelLoc.getPath().equals(modelPath) &&
				!((ModelResourceLocation)modelLoc).getVariant().equals("inventory"))
			{
				modelLocations.add(modelLoc);
			}
		}

		// replace the registered tank block variants with BakedTankModels

		IBakedModel registeredModel;
		IBakedModel replacementModel;

		for (ResourceLocation loc : modelLocations)
		{
			registeredModel = event.getModelRegistry().get(loc);
			replacementModel = new BakedTankModel(registeredModel);
			event.getModelRegistry().put(loc, replacementModel);
		}
	}
}
