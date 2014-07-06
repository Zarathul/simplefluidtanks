package net.zarathul.simplefluidtanks.configuration;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

/**
 * The in-game config UI.
 */
public class ConfigGui extends GuiConfig
{
	public ConfigGui(GuiScreen parentScreen)
	{
		super(parentScreen, getConfigElements(), SimpleFluidTanks.MOD_ID, false, false, GuiConfig.getAbridgedConfigPath(Config.getConfig().getConfigFile().getPath()), SimpleFluidTanks.MOD_READABLE_NAME);
	}

	private static List<IConfigElement> getConfigElements()
	{
		return new ConfigElement(Config.getConfig().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements();
	}
}
