package net.zarathul.simplefluidtanks.blocks;

import java.util.Random;

import net.minecraft.block.properties.IProperty;
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
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.registration.Registry;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * Represents a tank in the mods multiblock structure.
 */
public class TankBlock extends WrenchableBlock
{
	public static final IUnlistedProperty<Integer> FluidLevel = new Properties.PropertyAdapter<Integer>(PropertyInteger.create("fluidLevel", 0, 16));
	public static final IUnlistedProperty<Integer> FluidId = new Properties.PropertyAdapter<Integer>(PropertyInteger.create("fluidId", 0, 4096));
	public static final IUnlistedProperty<Boolean> CullFluidTop = new Properties.PropertyAdapter<Boolean>(PropertyBool.create("cullFluidTop"));
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
		IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[]
		{
			FluidLevel, FluidId, CullFluidTop
		};
		
		IProperty[] listedProperties = new IProperty[]
		{
			DOWN, UP, NORTH, SOUTH, WEST, EAST
		};
		
		return new ExtendedBlockState(this, listedProperties, unlistedProperties);
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		IExtendedBlockState extendedState = (IExtendedBlockState)state;
		
		TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);
		TankBlockEntity tankAbove = Utils.getTileEntityAt(world, TankBlockEntity.class, pos.up());
		
		if (tankEntity != null && tankEntity.isPartOfTank())
		{
			Fluid tankFluid = tankEntity.getFluid();
			int fluidLevel = (int)Math.round((tankEntity.getFillPercentage() / 100.0d) * 16);
			
			return extendedState
					.withProperty(FluidId, (tankFluid != null) ? tankFluid.getID() : 0)
					.withProperty(FluidLevel, fluidLevel)
					.withProperty(CullFluidTop, (tankAbove != null && !tankAbove.isEmpty()));
		}
		
		return extendedState
				.withProperty(FluidId, 0)
				.withProperty(FluidLevel, 0)
				.withProperty(CullFluidTop, false);
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);
		
		if (tankEntity != null && tankEntity.isPartOfTank())
		{
			return state
					.withProperty(DOWN, tankEntity.isConnected(EnumFacing.DOWN))
					.withProperty(UP, tankEntity.isConnected(EnumFacing.UP))
					.withProperty(NORTH, tankEntity.isConnected(EnumFacing.NORTH))
					.withProperty(SOUTH, tankEntity.isConnected(EnumFacing.SOUTH))
					.withProperty(WEST, tankEntity.isConnected(EnumFacing.WEST))
					.withProperty(EAST, tankEntity.isConnected(EnumFacing.EAST));
		}
		
		return state
				.withProperty(DOWN, false)
				.withProperty(UP, false)
				.withProperty(NORTH, false)
				.withProperty(SOUTH, false)
				.withProperty(WEST, false)
				.withProperty(EAST, false);
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
		// Only cull faces touching tank blocks that belong to the same multi block.
		
		TankBlockEntity adjacentTankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);
		TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos.offset(side.getOpposite()));
		ValveBlockEntity adjacentTankValve = (adjacentTankEntity != null) ?  adjacentTankEntity.getValve() : null;
		ValveBlockEntity tankValve = (tankEntity != null) ?  tankEntity.getValve() : null;
		
		return (adjacentTankEntity == null || (adjacentTankValve != tankValve));
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
