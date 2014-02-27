package simplefluidtanks.blocks;

import java.util.HashSet;
import java.util.Random;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import simplefluidtanks.SimpleFluidTanks;
import simplefluidtanks.common.BlockCoords;
import simplefluidtanks.common.Utils;
import simplefluidtanks.tileentities.TankBlockEntity;
import simplefluidtanks.tileentities.ValveBlockEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Represents a tank in the mods multiblock structure.
 */
public class TankBlock extends WrenchableBlock
{
	private HashSet<BlockCoords> ignorePreDestroyEvent;
	
	public TankBlock(int blockId)
	{
		super(blockId, TankMaterial.tankMaterial);
		
		setUnlocalizedName(SimpleFluidTanks.REGISTRY_TANKBLOCK_NAME);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setHardness(2.5f);
		setResistance(1000f);
		setStepSound(soundGlassFootstep);
		
		ignorePreDestroyEvent = new HashSet<BlockCoords>();
	}

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	@SideOnly(Side.CLIENT)
	public Icon[] getIcons()
	{
		return icons;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return icons[0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		icons = new Icon[]
		{
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_closed"),				//  0
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_open"),					//  1

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_bottom"),			//  2
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left_right"),			//  3

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_right"),				//  4
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_bottom_right"),			//  5
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_bottom_left"),			//  6
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_left"),				//  7

			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_left_right_top"),		//  8
			iconRegister.registerIcon(SimpleFluidTanks.MOD_ID + ":tank_top_bottom_right"),		//  9
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
		BlockCoords tankCoords = new BlockCoords(x, y, z);
		
		// ignore the event if the tanks coordinates are on the ignore list
		if (ignorePreDestroyEvent.contains(tankCoords))
		{
			ignorePreDestroyEvent.remove(tankCoords);
			
			return;
		}
		
		reset(world, x, y, z);
	}
	
	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
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
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public boolean canRenderInPass(int pass)
	{
		return (pass == 1);
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TankBlockEntity();
	}

	@Override
	protected void handleToolWrenchClick(World world, int x, int y, int z, EntityPlayer player, ItemStack equippedItemStack)
	{
		// dismantle aka. instantly destroy the tank and drop the appropriate item, telling the connected valve to rebuild in the process
		if (player.isSneaking())
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, x, y, z);
			ValveBlockEntity valveEntity = null;
			
			if (tankEntity.isPartOfTank())
			{
				valveEntity = tankEntity.getValve();
				// makes the tankblock ignore its preDestroy event, this way there will be no reset of the whole tank
				ignorePreDestroyEvent.add(new BlockCoords(x, y, z));
			}
			
			// destroy the tankblock (blockId 0 is air)
			world.setBlock(x, y, z, 0);
			// last two parameters are metadata and fortune
			dropBlockAsItem(world, x, y, z, 0, 0);
			
			if (valveEntity != null)
			{
				rebuild(world, valveEntity.xCoord, valveEntity.yCoord, valveEntity.zCoord);
			}
		}
	}
	
	/**
	 * Tells the {@link ValveBlockEntity} this {@link TankBlock} is connected to, to unlink all its connected {@link TankBlock}s.
	 * @param world
	 * The world.
	 * @param x
	 * The {@link TankBlock}s x-coordinate.
	 * @param y
	 * The {@link TankBlock}s y-coordinate.
	 * @param z
	 * The {@link TankBlock}s z-coordinate.
	 */
	private void reset(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			ValveBlockEntity valveEntity = getValve(world, x, y, z);
			
			if (valveEntity != null)
			{
				valveEntity.reset();
			}
		}
	}
	
	/**
	 * Tells the {@link ValveBlockEntity} to rebuild.
	 * @param world
	 * The world.
	 * @param x
	 * The {@link ValveBlock}s x-coordinate.
	 * @param y
	 * The {@link ValveBlock}s y-coordinate.
	 * @param z
	 * The {@link ValveBlock}s z-coordinate.
	 */
	private void rebuild(World world, int x, int y, int z)
	{
		if (!world.isRemote)
		{
			ValveBlockEntity valveEntity = Utils.getTileEntityAt(world, ValveBlockEntity.class, x, y, z);
			
			if (valveEntity != null)
			{
				valveEntity.rebuild();
			}
		}
	}
	
	/**
	 * Gets the {@link ValveBlock}s {@link TileEntity} the {@link TankBlock} is linked to.
	 * @return
	 * The valves {@link ValveBlockEntity}<br>
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
	private ValveBlockEntity getValve(World world, int x, int y, int z)
	{
		TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, x, y, z);
		
		if (tankEntity != null)
		{
			ValveBlockEntity valveEntity = tankEntity.getValve();
			
			if (valveEntity != null)
			{
				return valveEntity;
			}
		}
		
		return null;
	}
}
