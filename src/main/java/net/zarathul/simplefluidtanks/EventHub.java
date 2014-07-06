package net.zarathul.simplefluidtanks;

import net.zarathul.simplefluidtanks.configuration.Config;
import cpw.mods.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Hosts Forge and FML event handlers.
 */
public final class EventHub
{
	public EventHub()
	{
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		if (SimpleFluidTanks.MOD_ID.equals(event.modID) && !event.isWorldRunning)
		{
			Config.sync();
		}
	}
}
