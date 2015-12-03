package net.zarathul.simplefluidtanks.blocks;

import java.util.HashSet;
import java.util.Random;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.zarathul.simplefluidtanks.CommonEventHub;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents a tank in the mods multiblock structure.
 */
public class TankBlock extends WrenchableBlock
{
	public static final PropertyBool DOWN = PropertyBool.create("down");
	public static final PropertyBool UP = PropertyBool.create("up");
	public static final PropertyBool NORTH = PropertyBool.create("north");
	public static final PropertyBool SOUTH = PropertyBool.create("south");
	public static final PropertyBool WEST = PropertyBool.create("west");
	public static final PropertyBool EAST = PropertyBool.create("east");
	
	public TankBlock()
	{
		super(TankMaterial.tankMaterial);

		setUnlocalizedName(Registry.TANK_BLOCK_NAME);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setHardness(Config.tankBlockHardness);
		setResistance(Config.tankBlockResistance);
		setStepSound(soundTypeGlass);
		setHarvestLevel("pickaxe", 2);
		
		setDefaultState(this.blockState.getBaseState()
				.withProperty(DOWN, false)
				.withProperty(UP, false)
				.withProperty(NORTH, false)
				.withProperty(SOUTH, false)
				.withProperty(WEST, false)
				.withProperty(EAST, false));
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, DOWN, UP, NORTH, SOUTH, WEST, EAST);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);
		
		if (tankEntity != null)
		{
			if (tankEntity.isPartOfTank())
			{
				state = state.withProperty(DOWN, tankEntity.isConnected(EnumFacing.DOWN))
						.withProperty(UP, tankEntity.isConnected(EnumFacing.UP))
						.withProperty(NORTH, tankEntity.isConnected(EnumFacing.NORTH))
						.withProperty(SOUTH, tankEntity.isConnected(EnumFacing.SOUTH))
						.withProperty(WEST, tankEntity.isConnected(EnumFacing.WEST))
						.withProperty(EAST, tankEntity.isConnected(EnumFacing.EAST));
			}
			else
			{
				state = state.withProperty(DOWN, false)
						.withProperty(UP, false)
						.withProperty(NORTH, false)
						.withProperty(SOUTH, false)
						.withProperty(WEST, false)
						.withProperty(EAST, false);
			}
		}
		
		return state;
	}
	
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return 0;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
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
	public boolean isFullCube()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return 3;
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.TRANSLUCENT;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int unknown)
	{
		return new TankBlockEntity();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return (world.getBlockState(pos).getBlock() != this);
	}

	@Override
	public boolean requiresUpdates()
	{
		return false;
	}

	@Override
	protected void handleToolWrenchClick(World world, BlockPos pos, EntityPlayer player, ItemStack equippedItemStack)
	{
		// dismantle aka. instantly destroy the tank and drop the
		// appropriate item, telling the connected valve to rebuild in the process
		if (player.isSneaking())
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);
			ValveBlockEntity valveEntity = null;

			if (tankEntity != null && tankEntity.isPartOfTank())
			{
				valveEntity = tankEntity.getValve();
				// ignore the BlockBreak event for this TankBlock, this way
				// there will be no reset of the whole tank
				SimpleFluidTanks.commonEventHub.ignoreBlockBreak(pos);
			}

			// destroy the TankBlock
			world.setBlockToAir(pos);
			dropBlockAsItem(world, pos, this.getDefaultState(), 0);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
		}
	}
}
