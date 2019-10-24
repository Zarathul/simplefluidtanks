package net.zarathul.simplefluidtanks.configuration.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * The factory providing the in-game config UI.
 */
@OnlyIn(Dist.CLIENT)
public final class ConfigGuiFactory
{
	private static ForgeConfigSpec[] configSpecs;

	public static Screen getConfigGui(Minecraft mc, Screen parent)
	{
		return new ConfigGui(new StringTextComponent("Simple Fluid Tanks Config"), parent, configSpecs);
	}

	public static void setConfigHolder(String classPath)
	{
		Class classHolder = null;

		try
		{
			classHolder = Class.forName(classPath);

		}
		catch (ClassNotFoundException ex)
		{
			SimpleFluidTanks.log.error("Config holder class not found.");
		}

		if (classHolder == null) return;

		List<ForgeConfigSpec> specs = new ArrayList<>();

		for (Field field : classHolder.getFields())
		{
			if (field.getType() == ForgeConfigSpec.class)
			{
				try
				{
					specs.add((ForgeConfigSpec)field.get(classHolder));
				}
				catch (IllegalAccessException ex)
				{
					SimpleFluidTanks.log.error("Could not access ForgeConfigSpec fields of the config holder class.");
				}
			}
		}

		configSpecs = new ForgeConfigSpec[specs.size()];
		specs.toArray(configSpecs);
	}
}
