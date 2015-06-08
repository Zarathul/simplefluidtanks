package net.zarathul.simplefluidtanks.blocks;

import java.util.HashSet;
import java.util.Random;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.BlockCoords;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.zarathul.simplefluidtanks.rendering.TankBlockRenderer;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Represents a tank in the mods multiblock structure.
 */
public class TankBlock extends WrenchableBlock
{
	private final HashSet<BlockCoords> ignorePreDestroyEvent;

	public TankBlock()
	{
		super(TankMaterial.tankMaterial);

		setBlockName(Registry.TANKBLOCK_NAME);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setHardness(Config.tankBlockHardness);
		setResistance(Config.tankBlockResistance);
		setStepSound(soundTypeGlass);
		setHarvestLevel("pickaxe", 2);
		ignorePreDestroyEvent = new HashSet<BlockCoords>();
	}

	@SideOnly(Side.CLIENT)
	protected IIcon[] icons;

	@SideOnly(Side.CLIENT)
	public IIcon[] getIcons()
	{
		return icons;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		return icons[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		icons = new IIcon[]
		{
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_closed"),				// 0
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_open"),					// 1

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_bottom"),			// 2
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left_right"),			// 3

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_right"),				// 4
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_bottom_right"),			// 5
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_bottom_left"),			// 6
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_left"),				// 7

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left_right_top"),		// 8
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_bottom_right"),		// 9
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left_right_bottom"),		// 10
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_bottom_left"),		// 11

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top"),					// 12
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_bottom"),				// 13
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left"),					// 14
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_right")					// 15
		};
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int par5)
	{
		if (!world.isRemote)
		{
			BlockCoords tankCoords = new BlockCoords(x, y, z);

			// ignore the event if the tanks coordinates are on the ignore list
			if (ignorePreDestroyEvent.contains(tankCoords))
			{
				ignorePreDestroyEvent.remove(tankCoords);

				return;
			}

			ValveBlockEntity valveEntity = getValve(world, x, y, z);

			if (valveEntity != null)
			{
				valveEntity.disbandMultiblock();
			}
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		// allow torches, ladders etc. to be places on every side
		return true;
	}

	@Override
	public int quantityDropped(Random par1Random)
	{
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public boolean canRenderInPass(int pass)
	{
		return (pass == 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return TankBlockRenderer.id;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown)
	{
		return new TankBlockEntity();
	}

	@Override
	protected void handleToolWrenchClick(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack)
	{
		// dismantle aka. instantly destroy the tank and drop the
		// appropriate item, telling the connected valve to rebuild in the process
		if (player.isSneaking())
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, x, y, z);
			ValveBlockEntity valveEntity = null;

			if (tankEntity != null && tankEntity.isPartOfTank())
			{
				valveEntity = tankEntity.getValve();
				// makes the tankblock ignore its preDestroy event, this way
				// there will be no reset of the whole tank
				ignorePreDestroyEvent.add(new BlockCoords(x, y, z));
			}

			// destroy the tankblock
			world.setBlockToAir(x, y, z);
			// last two parameters are metadata and fortune
			dropBlockAsItem(world, x, y, z, 0, 0);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
		}
	}

	/**
	 * Gets the {@link ValveBlock}s {@link TileEntity} the {@link TankBlock} is linked to.
	 * 
	 * @return The valves {@link ValveBlockEntity}<br>
	 * or<br>
	 * <code>null</code> if the {@link TankBlock} is not linked to a {@link ValveBlock}.
	 * @param world
	 * The world.
	 * @param x
	 * The {@link TankBlock}s x-coordinate.
	 * @param y
	 * The {@link TankBlock}s y-coordinate.
	 * @param z
	 * The {@link TankBlock}s z-coordinate.
	 */
	private ValveBlockEntity getValve(IBlockAccess world, int x, int y, int z)
	{
		TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, x, y, z);

		if (tankEntity != null)
		{
			ValveBlockEntity valveEntity = tankEntity.getValve();

			return valveEntity;
		}

		return null;
	}
}
