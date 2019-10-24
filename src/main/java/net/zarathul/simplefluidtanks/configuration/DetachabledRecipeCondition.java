package net.zarathul.simplefluidtanks.configuration;

/*
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;

import java.util.function.BooleanSupplier;

public class DetachabledRecipeCondition implements IConditionFactory
{
	@Override
	public BooleanSupplier parse(JsonContext context, JsonObject json)
	{
		String configName = JsonUtils.getString(json, "name");

		return () ->
		{
			try
			{
				return (boolean)(Config.class.getField(configName).get(Config.getConfig()));

			}
			catch (Exception ex)
			{
				// FIXME: Add recipe name to error message if it ever becomes available.
				SimpleFluidTanks.log.error("Nonexistent config setting '" + configName + "' in detachable recipe condition.");
			}

			return true;
		};
	}
}

 */
