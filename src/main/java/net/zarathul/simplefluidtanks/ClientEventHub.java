package net.zarathul.simplefluidtanks;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.zarathul.simplefluidtanks.rendering.BakedTankFluidModel;
import net.zarathul.simplefluidtanks.rendering.BakedTankModel;

/**
 * Hosts Forge event handlers on the client side.
 */
public final class ClientEventHub
{
	@SubscribeEvent
	public void OnConfigChanged(OnConfigChangedEvent event)
	{
		if (SimpleFluidTanks.MOD_ID.equals(event.getModID()))
		{
			Config.sync();
		}
	}
	
	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		// generate fluid models for all registered fluids for 16 levels each
		
		Fluid fluid;
		IBakedModel[] bakedFluidModels;
		
		for (Entry<String, Fluid> entry : FluidRegistry.getRegisteredFluids().entrySet())
		{
			fluid = entry.getValue();
			
			bakedFluidModels = new IBakedModel[BakedTankModel.FLUID_LEVELS];
			
			for (int x = 0; x < BakedTankModel.FLUID_LEVELS; x++)
			{
				bakedFluidModels[x] = new BakedTankFluidModel(fluid, x + 1);
			}
			
			BakedTankModel.FLUID_MODELS.put(entry.getKey(), bakedFluidModels);
		}
		
		// get ModelResourceLocations of all tank block variants from the registry except "inventory"
		
		RegistrySimple<ModelResourceLocation, IBakedModel> registry = (RegistrySimple)event.getModelRegistry();
		ArrayList<ModelResourceLocation> modelLocations = Lists.newArrayList();
		
		// as of 1.11.2 (maybe earlier) all resource names must be all lower case
		String modelPath = Registry.TANK_BLOCK_NAME.toLowerCase();
		
		for (ModelResourceLocation modelLoc : registry.getKeys())
		{
			if (modelLoc.getResourceDomain().equals(SimpleFluidTanks.MOD_ID)
				&& modelLoc.getResourcePath().equals(modelPath)
				&& !modelLoc.getVariant().equals("inventory"))
			{
				modelLocations.add(modelLoc);
			}
		}
		
		// replace the registered tank block variants with BakedTankModels
		
		IBakedModel registeredModel;
		IBakedModel replacementModel;
		
		for (ModelResourceLocation loc : modelLocations)
		{
			registeredModel = event.getModelRegistry().getObject(loc);
			replacementModel = new BakedTankModel(registeredModel);
			event.getModelRegistry().putObject(loc, replacementModel);
		}
	}
}
