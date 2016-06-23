package net.zarathul.simplefluidtanks.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.tileentities.ConnectorBlockEntity;

public class ConnectorBlock extends TankBlock {

	
	public ConnectorBlock()
	{
		super();

		setBlockName(Registry.CONNECTORBLOCK_NAME);
		setHardness(Config.connectorBlockHardness);
		setResistance(Config.connectorBlockResistance);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		icons = new IIcon[]
		{
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_closed"),				// 0
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_open"),					// 1

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_top_bottom"),			// 2
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_left_right"),			// 3

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_top_right"),				// 4
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_bottom_right"),			// 5
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_bottom_left"),			// 6
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_top_left"),				// 7

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_left_right_top"),		// 8
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_top_bottom_right"),		// 9
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_left_right_bottom"),		// 10
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_top_bottom_left"),		// 11

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_top"),					// 12
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_bottom"),				// 13
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_left"),					// 14
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":connector_right")					// 15
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return TankBlockRenderer.id;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown)
	{
		return new ConnectorBlockEntity();
	}

}
