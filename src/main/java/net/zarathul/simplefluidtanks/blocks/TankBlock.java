package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;
import net.zarathul.simplefluidtanks.SimpleFluidTanks;
import net.zarathul.simplefluidtanks.blocks.blockstate.ResourceLocationProperty;
import net.zarathul.simplefluidtanks.common.Utils;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

import javax.annotation.Nullable;
import java.util.HashSet;

/**
 * Represents a tank in the mods multiblock structure.
 */
public class TankBlock extends WrenchableBlock
{
	public static final ResourceLocationProperty FluidName = ResourceLocationProperty.create("fluid_name");
	public static final IntegerProperty FluidLevel = IntegerProperty.create("fluid_level", 0 , 16);
	public static final BooleanProperty CullFluidTop = BooleanProperty.create("cull_fluid_top");

	public static final BooleanProperty DOWN = BooleanProperty.create("down");
	public static final BooleanProperty UP = BooleanProperty.create("up");
	public static final BooleanProperty NORTH = BooleanProperty.create("north");
	public static final BooleanProperty SOUTH = BooleanProperty.create("south");
	public static final BooleanProperty WEST = BooleanProperty.create("west");
	public static final BooleanProperty EAST = BooleanProperty.create("east");
	
	private final HashSet<BlockPos> ignoreBlockBreakCoords;
	private final ResourceLocation EMPTY_FLUID_NAME = ForgeRegistries.FLUIDS.getKey(Fluids.EMPTY);
	private final BooleanProperty[] CONNECTION_PROPS_FROM_DIRECTION = new BooleanProperty[6];

	
	public TankBlock()
	{
		super(Block.Properties.create(SimpleFluidTanks.tankMaterial)
				.hardnessAndResistance(Config.tankBlockHardness.get().floatValue(), Config.tankBlockResistance.get().floatValue())
				.sound(SoundType.GLASS)
				.harvestLevel(2)
				.harvestTool(ToolType.PICKAXE));

		setRegistryName(SimpleFluidTanks.TANK_BLOCK_NAME);

		setDefaultState(this.getStateContainer().getBaseState()
						.with(DOWN,  false)
						.with(UP,    false)
						.with(NORTH, false)
						.with(SOUTH, false)
						.with(WEST,  false)
						.with(EAST,  false)
						.with(FluidName, EMPTY_FLUID_NAME)
						.with(FluidLevel, 0)
						.with(CullFluidTop, false));

		ignoreBlockBreakCoords = new HashSet<BlockPos>();

		CONNECTION_PROPS_FROM_DIRECTION[Direction.DOWN.getIndex()] = DOWN;
		CONNECTION_PROPS_FROM_DIRECTION[Direction.UP.getIndex()] = UP;
		CONNECTION_PROPS_FROM_DIRECTION[Direction.NORTH.getIndex()] = NORTH;
		CONNECTION_PROPS_FROM_DIRECTION[Direction.SOUTH.getIndex()] = SOUTH;
		CONNECTION_PROPS_FROM_DIRECTION[Direction.WEST.getIndex()] = WEST;
		CONNECTION_PROPS_FROM_DIRECTION[Direction.EAST.getIndex()] = EAST;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST, FluidName, FluidLevel, CullFluidTop);
	}

	public void updateBlockState(World world, BlockPos pos)
	{
		TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);
		if (tankEntity == null) return;;

		TankBlockEntity tankAbove = Utils.getTileEntityAt(world, TankBlockEntity.class, pos.up());
		BlockState state = world.getBlockState(pos);
		BlockState newState = null;

		if (tankEntity.isPartOfTank())
		{
			Fluid tankFluid = tankEntity.getFluid();
			ResourceLocation fluidName = (tankFluid != null) ? tankFluid.getRegistryName() : EMPTY_FLUID_NAME;
			int fluidLevel = tankEntity.getFillLevel();

			boolean tankAboveIsEmpty = true;
			boolean sameValve = false;

			if (tankAbove != null)
			{
				tankAboveIsEmpty = tankAbove.isEmpty();
				ValveBlockEntity valve = tankEntity.getValve();
				ValveBlockEntity valveAbove = tankAbove.getValve();
				sameValve = valveAbove != null && valve == valveAbove;
			}

			// Only cull the fluids top face if the tank above is not empty and both tanks
			// are part of the same multiblock (share the same valve).
			boolean cullFluidTop = !tankAboveIsEmpty && sameValve;

			newState = state
					.with(DOWN,  tankEntity.isConnected(Direction.DOWN))
					.with(UP,    tankEntity.isConnected(Direction.UP))
					.with(NORTH, tankEntity.isConnected(Direction.NORTH))
					.with(SOUTH, tankEntity.isConnected(Direction.SOUTH))
					.with(WEST,  tankEntity.isConnected(Direction.WEST))
					.with(EAST,  tankEntity.isConnected(Direction.EAST))
					.with(FluidName, fluidName)
					.with(FluidLevel, fluidLevel)
					.with(CullFluidTop, cullFluidTop);
		}

		world.setBlockState(pos, (newState != null) ? newState : getDefaultState(), 3);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
	{
		return (state.get(FluidLevel) == 0);
	}

	@Override
	public boolean canEntitySpawn(BlockState state, IBlockReader worldIn, BlockPos pos, EntityType<?> type)
	{
		return false;
	}

	@Override
	public float func_220080_a(BlockState state, IBlockReader worldIn, BlockPos pos)
	{
		return 1.0f;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer)
	{
		return (layer == BlockRenderLayer.CUTOUT_MIPPED || layer == BlockRenderLayer.TRANSLUCENT);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new TankBlockEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side)
	{
		// Don't render if there's a connection to another TankBlock on that side, or if
		// two TankBlocks, that are both not part of a multiblock structure, touch each other
		// on that side.
		return state.get(CONNECTION_PROPS_FROM_DIRECTION[side.getIndex()]) ||
			   ((adjacentBlockState.getBlock() == SimpleFluidTanks.tankBlock) &&
			   (!isPartOfMultiBlock(state) && !isPartOfMultiBlock(adjacentBlockState)));
	}

	@Override
	public boolean doesSideBlockRendering(BlockState state, IEnviromentBlockReader world, BlockPos pos, Direction face)
	{
		return false;
	}

	@Override
	public boolean ticksRandomly(BlockState state)
	{
		return false;
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (!world.isRemote)
		{
			if (newState.getBlock() != SimpleFluidTanks.tankBlock)
			{
				// ignore the event if the tanks coordinates are on the ignore list
				if (ignoreBlockBreakCoords.contains(pos))
				{
					ignoreBlockBreakCoords.remove(pos);

					return;
				}

				// get the valve the tank is connected to and disband the multiblock
				ValveBlockEntity valveEntity = Utils.getValve(world, pos);

				if (valveEntity != null)
				{
					valveEntity.disbandMultiblock(pos);
				}
			}
		}

		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	protected void handleToolWrenchClick(World world, BlockPos pos, PlayerEntity player, ItemStack equippedItemStack)
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
				ignoreBlockBreakCoords.add(pos);
			}

			world.destroyBlock(pos, true);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
		}
	}

	/**
	 * Tries to determines if a TankBlock part of a multiblock structure based on its BlockState.
	 *
	 * @param state
	 * The state of the block to check.
	 * @return
	 * <code>true</code> if any of the blocks connection properties is <code>true</code>, otherwise <code>false</code>.
	 */
	private boolean isPartOfMultiBlock(BlockState state)
	{
		return (state.get(DOWN) ||
				state.get(UP) ||
				state.get(NORTH) ||
				state.get(SOUTH) ||
				state.get(WEST) ||
				state.get(EAST) ||
				(state.get(FluidLevel) > 0) ||
				(!state.get(FluidName).equals(EMPTY_FLUID_NAME)));
	}
}
