package net.zarathul.simplefluidtanks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.RegistrySimple;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IRetexturableModel;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.zarathul.simplefluidtanks.rendering.TankModelFactory;

/**
 * Hosts Forge event handlers on the client side.
 */
public final class ClientEventHub
{
	@SubscribeEvent
	public void OnConfigChanged(OnConfigChangedEvent event)
	{
		if (SimpleFluidTanks.MOD_ID.equals(event.modID))
		{
			Config.sync();
		}
	}
	
	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		// generate fluid models for all registered fluids for 16 levels each
		
		IRetexturableModel[] fluidModels = new IRetexturableModel[TankModelFactory.FLUID_LEVELS];
		
		try
		{
			// load the fluid models for the different levels from the .json files
			
			for (int x = 0; x < TankModelFactory.FLUID_LEVELS; x++)
			{
				// Note: We have to use ResourceLocation here instead of ModelResourceLocation. Because if ModelResourceLocation is used, 
				// the ModelLoader expects to find a  BlockState .json for the model.
				fluidModels[x] = (IRetexturableModel)event.modelLoader.getModel(new ResourceLocation(SimpleFluidTanks.MOD_ID + ":block/fluid_" + String.valueOf(x)));
			}
		}
		catch (IOException e)
		{
			SimpleFluidTanks.log.fatal("Failed loading fluid model. Fluid block model missing or inaccessible.");
			
			return;
		}
		
		Function<ResourceLocation, TextureAtlasSprite> textureGetter = new Function<ResourceLocation, TextureAtlasSprite>()
		{
			@Override
			public TextureAtlasSprite apply(ResourceLocation location)
			{
				return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
			}
		};
		
		IModel retexturedModel;
		
		// retexture and cache the loaded fluid models for each registered fluid
		
		String fluidTextureLoc;
		Fluid fluid;
		
		for (Entry<String, Fluid> entry : FluidRegistry.getRegisteredFluids().entrySet())
		{
			for (int x = 0; x < fluidModels.length; x++)
			{
				fluid = entry.getValue();
				fluidTextureLoc = (fluid.getStill() != null)
						? fluid.getStill().toString()
						: (fluid.getFlowing() != null)
						? fluid.getFlowing().toString()
						: null;
				
				if (fluidTextureLoc == null)
				{
					SimpleFluidTanks.log.warn(String.format("Fluid '%s' is missing both still and flowing textures. Defaulting to water texture.", entry.getKey()));
					fluidTextureLoc = FluidRegistry.WATER.getStill().toString();
				}
				
				retexturedModel = fluidModels[x].retexture(new ImmutableMap.Builder()
						.put("fluid", fluidTextureLoc)
						.build());

				TankModelFactory.FLUID_MODELS[x].put(
						entry.getKey(),
						retexturedModel.bake(retexturedModel.getDefaultState(), Attributes.DEFAULT_BAKED_FORMAT, textureGetter));
			}
		}
		
		// get ModelResourceLocations of all tank block variants from the registry except "inventory"
		
		RegistrySimple<ModelResourceLocation, IBakedModel> registry = (RegistrySimple)event.modelRegistry;
		ArrayList<ModelResourceLocation> modelLocations = Lists.newArrayList();
		
		for (ModelResourceLocation modelLoc : registry.getKeys())
		{
			if (modelLoc.getResourceDomain().equals(SimpleFluidTanks.MOD_ID)
				&& modelLoc.getResourcePath().equals(Registry.TANK_BLOCK_NAME)
				&& !modelLoc.getVariant().equals("inventory"))
			{
				modelLocations.add(modelLoc);
			}
		}
		
		// replace the registered tank block variants with TankModelFactories
		
		IBakedModel registeredModel;
		TankModelFactory modelFactory;
		
		for (ModelResourceLocation loc : modelLocations)
		{
			registeredModel = event.modelRegistry.getObject(loc);
			modelFactory = new TankModelFactory(registeredModel);
			event.modelRegistry.putObject(loc, modelFactory);
		}
	}
}
