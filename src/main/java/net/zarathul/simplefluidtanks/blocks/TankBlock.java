package net.zarathul.simplefluidtanks.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
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
	// These properties store which side of the TankBlock is connected to another TankBlock
	// in the multiblock structure. This is mainly used for connected textures.
	public static final BooleanProperty DOWN = BooleanProperty.create("down");
	public static final BooleanProperty UP = BooleanProperty.create("up");
	public static final BooleanProperty NORTH = BooleanProperty.create("north");
	public static final BooleanProperty SOUTH = BooleanProperty.create("south");
	public static final BooleanProperty WEST = BooleanProperty.create("west");
	public static final BooleanProperty EAST = BooleanProperty.create("east");
	// This property is needed because with only the sided connection properties above, it is impossible
	// to tell if a TankBlock is actually a part of a mutliblock structure. TankBlocks can easily be
	// a part of a multiblock tank, without being directly connected to other TankBlocks.
	public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

	private final HashSet<BlockPos> ignoreBlockBreakCoords;
	private final ResourceLocation EMPTY_FLUID_NAME = ForgeRegistries.FLUIDS.getKey(Fluids.EMPTY);
	private final BooleanProperty[] DIRECTION_TO_CONNECTION = new BooleanProperty[6];

	
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
						.with(CONNECTED, false));

		ignoreBlockBreakCoords = new HashSet<BlockPos>();

		DIRECTION_TO_CONNECTION[Direction.DOWN.getIndex()] = DOWN;
		DIRECTION_TO_CONNECTION[Direction.UP.getIndex()] = UP;
		DIRECTION_TO_CONNECTION[Direction.NORTH.getIndex()] = NORTH;
		DIRECTION_TO_CONNECTION[Direction.SOUTH.getIndex()] = SOUTH;
		DIRECTION_TO_CONNECTION[Direction.WEST.getIndex()] = WEST;
		DIRECTION_TO_CONNECTION[Direction.EAST.getIndex()] = EAST;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST, CONNECTED);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
	{
		TileEntity tileEntity = reader.getTileEntity(pos);
		TankBlockEntity tankEntity = (tileEntity != null) ? (TankBlockEntity)tileEntity : null;

		return (tankEntity == null || tankEntity.getFillLevel() == 0);
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
		return state.get(DIRECTION_TO_CONNECTION[side.getIndex()]) ||
			   ((adjacentBlockState.getBlock() == SimpleFluidTanks.tankBlock) &&
			   (!state.get(CONNECTED) && !adjacentBlockState.get(CONNECTED)));
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
				}
				else
				{
					// get the valve the tank is connected to and disband the multiblock
					ValveBlockEntity valveEntity = Utils.getValve(world, pos);

					if (valveEntity != null)
					{
						valveEntity.disbandMultiblock(pos);
					}
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
				ignoreBlockBreakCoords.add(pos.toImmutable());
			}

			world.destroyBlock(pos, true);

			if (valveEntity != null)
			{
				valveEntity.formMultiblock();
			}
		}
	}
}
