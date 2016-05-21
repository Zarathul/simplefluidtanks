package net.zarathul.simplefluidtanks.blocks;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
import net.zarathul.simplefluidtanks.blocks.blockstate.StringProperty;
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
	public static final IUnlistedProperty<String> FluidName = new StringProperty("fluidName");
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

		setRegistryName(Registry.TANK_BLOCK_NAME);
		setUnlocalizedName(Registry.TANK_BLOCK_NAME);
		setCreativeTab(SimpleFluidTanks.creativeTab);
		setHardness(Config.tankBlockHardness);
		setResistance(Config.tankBlockResistance);
		setStepSound(SoundType.GLASS);
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
	protected BlockStateContainer createBlockState()
	{
		IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[]
		{
			FluidLevel, FluidName, CullFluidTop
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
			int fluidLevel = Utils.getFluidLevel(tankEntity.getFillPercentage());
			
			boolean tankAboveIsEmpty = true;
			boolean sameValve = false;
			
			if (tankAbove != null)
			{
				tankAboveIsEmpty = tankAbove.isEmpty();
				ValveBlockEntity valve = tankEntity.getValve();
				ValveBlockEntity valveAbove = tankAbove.getValve();
				sameValve = valve != null && valveAbove != null && valve == valveAbove;
			}
			
			// Only cull the fluids top face if the tank above is not empty and both tanks 
			// are part of the same multiblock (share the same valve).
			boolean cullFluidTop = !tankAboveIsEmpty && sameValve;
			
			return extendedState
					.withProperty(FluidName, (tankFluid != null) ? tankFluid.getName() : "")
					.withProperty(FluidLevel, fluidLevel)
					.withProperty(CullFluidTop, cullFluidTop);
		}
		
		return extendedState
				.withProperty(FluidName, "")
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
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
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
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullBlock(IBlockState state)
	{
		return true;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}
    
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TankBlockEntity();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
		// Only cull faces touching tank blocks that belong to the same multi block.
		TankBlockEntity adjacentTankEntity = Utils.getTileEntityAt(blockAccess, TankBlockEntity.class, pos.offset(side));
		TankBlockEntity tankEntity = Utils.getTileEntityAt(blockAccess, TankBlockEntity.class, pos);
		ValveBlockEntity adjacentTankValve = (adjacentTankEntity != null) ?  adjacentTankEntity.getValve() : null;
		ValveBlockEntity tankValve = (tankEntity != null) ?  tankEntity.getValve() : null;
		
		return (adjacentTankEntity == null || (adjacentTankValve != tankValve));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		return false;
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
