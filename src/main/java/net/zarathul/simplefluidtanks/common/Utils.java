package net.zarathul.simplefluidtanks.common;

import java.util.ArrayList;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.zarathul.simplefluidtanks.blocks.TankBlock;
import net.zarathul.simplefluidtanks.blocks.ValveBlock;
import net.zarathul.simplefluidtanks.configuration.Config;
import net.zarathul.simplefluidtanks.rendering.TankModelFactory;
import net.zarathul.simplefluidtanks.tileentities.TankBlockEntity;
import net.zarathul.simplefluidtanks.tileentities.ValveBlockEntity;

/**
 * General utility class.
 */
public final class Utils
{
	public static final ItemStack FILLED_BOTTLE = new ItemStack(Items.potionitem);

	/**
	 * Chances that other mods register fluid containers while the game is already running are low. So cache the container data.
	 */
	private static FluidContainerData[] cachedFluidContainerData = null;

	/**
	 * Gets the {@link TileEntity} at the specified coordinates, cast to the specified type.
	 * 
	 * @param access
	 * An {@link IBlockAccess} implementation. Usually the world.
	 * @param entityType
	 * The type the {@link TileEntity} should be cast to.
	 * @param coords
	 * The coordinates of the {@link TileEntity}.
	 * @return The cast {@link TileEntity} or <code>null</code> if no {@link TileEntity} was found or the types didn't match.
	 */
	public static final <T extends TileEntity> T getTileEntityAt(IBlockAccess access, Class<T> entityType, BlockPos coords)
	{
		if (access != null && entityType != null && coords != null)
		{
			TileEntity entity = access.getTileEntity(coords);

			if (entity != null && entity.getClass() == entityType)
			{
				return (T) entity;
			}
		}

		return null;
	}

	/**
	 * Gets the {@link ValveBlock}s {@link TileEntity} for a linked {@link TankBlock}.
	 * 
	 * @return The valves {@link ValveBlockEntity}<br>
	 * or<br>
	 * <code>null</code> if no linked {@link ValveBlock} was found.
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link TankBlock}s coordinates.
	 */
	public static ValveBlockEntity getValve(IBlockAccess world, BlockPos pos)
	{
		if (world != null && pos != null)
		{
			TankBlockEntity tankEntity = Utils.getTileEntityAt(world, TankBlockEntity.class, pos);

			if (tankEntity != null)
			{
				ValveBlockEntity valveEntity = tankEntity.getValve();

				return valveEntity;
			}
		}

		return null;
	}

	/**
	 * A predicate that returns <code>true</code> if passed string is neither <code>null</code> nor empty.
	 */
	private static final Predicate<String> stringNotNullOrEmpty = new Predicate<String>()
	{
		@Override
		public boolean apply(String item)
		{
			return !Strings.isNullOrEmpty(item);
		}
	};

	/**
	 * Checks a list of strings for <code>null</code> and empty elements.
	 * 
	 * @param items
	 * The list of strings to check.
	 * @return
	 * <code>true</code> if the list neither contains <code>null</code> elements nor empty strings, otherwise <code>false</code>.
	 */
	public static final boolean notNullorEmpty(Iterable<String> items)
	{
		return Iterables.all(items, stringNotNullOrEmpty);
	}

	/**
	 * Gets the localized formatted strings for the specified key and formatting arguments.
	 * 
	 * @param key
	 * The base key without an index (e.g. "myKey" gets "myKey0", "myKey1" ... etc.).
	 * @param args
	 * Formatting arguments.
	 * @return
	 */
	public static final ArrayList<String> multiLineTranslateToLocal(String key, Object... args)
	{
		ArrayList<String> lines = new ArrayList<String>();

		if (key != null)
		{
			int x = 0;
			String currentKey = key + x;

			while (StatCollector.canTranslate(currentKey))
			{
				lines.add(StatCollector.translateToLocalFormatted(currentKey, args));
				currentKey = key + ++x;
			}
		}

		return lines;
	}
	
	/**
	 * Calculates the fluid level for the specified fill percentage.
	 * 
	 * @param percentage
	 * The fill percentage.
	 * @return
	 * A value between 0 and {@code TankModelFactory.FLUID_LEVELS}.
	 */
	public static int getFluidLevel(int fillPercentage)
	{
		int level = (int)Math.round((fillPercentage / 100.0d) * TankModelFactory.FLUID_LEVELS);
		
		// Make sure that even for small amounts the fluid is rendered at the first level.
		return (fillPercentage > 0) ? Math.max(1, level) : 0;
	}
	
	public static final void fillDrainFluidContainer(EntityPlayer player, ItemStack containerStack, ValveBlockEntity valveEntity)
	{
		if (player == null || containerStack == null || containerStack.stackSize < 1 || valveEntity == null) return;
		
		if (FluidContainerRegistry.isEmptyContainer(containerStack) ||
			isEmptyComplexContainer(containerStack) ||
			(containerStack.getItem() instanceof IFluidContainerItem && player.isSneaking()))
		{
			fillContainerFromTank(player, containerStack, valveEntity);
		}
		else
		{
			drainContainerIntoTank(player, containerStack, valveEntity);
		}
	}

	/**
	 * Fills an empty container with the liquid contained in the multiblock tank.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @param player
	 * The player holding the container.
	 * @param containerStack
	 * The container {@link ItemStack}.
	 * @param valveEntity
	 * The affected {@link ValveBlock}s {@link TileEntity} ({@link ValveBlockEntity}).
	 */
	private static final void fillContainerFromTank(EntityPlayer player, ItemStack containerStack, ValveBlockEntity valveEntity)
	{
		if (valveEntity.getFluid() == null) return;
		
		boolean doItemExchange = false;
		ItemStack filledContainer;

		if (containerStack.getItem() instanceof IFluidContainerItem)
		{
			// Handle IFluidContainerItem items
			
			doItemExchange = containerNeedsUnstacking(containerStack);
			IFluidContainerItem containerItem = (IFluidContainerItem) containerStack.getItem();
			filledContainer = containerStack;
			
			if (doItemExchange)
			{
				filledContainer = containerStack.copy();
				filledContainer.stackSize = 1;
			}
			
			int fillFluidAmount = containerItem.fill(filledContainer, valveEntity.getFluid(), true);
			valveEntity.drain(null, fillFluidAmount, true);
		}
		else
		{
			// Handle filling by exchange items

			filledContainer = Utils.fillFluidContainer(valveEntity.getFluid(), containerStack);

			if (filledContainer != null)
			{
				int containerCapacity = Utils.getFluidContainerCapacity(valveEntity.getFluid(), containerStack);

				if (containerCapacity > 0)
				{
					FluidStack drainedFluid = valveEntity.drain(null, containerCapacity, true);
					doItemExchange = (drainedFluid != null && drainedFluid.amount == containerCapacity);
				}
			}
		}
		
		// Give filled container to the player
		
		if (doItemExchange)
		{
			exchangeItems(player, containerStack, filledContainer);
		}
	}

	/**
	 * Drains the contents of a container into the multiblock tank.
	 * 
	 * @param world
	 * The world.
	 * @param pos
	 * The {@link ValveBlock}s coordinates.
	 * @param player
	 * The player holding the container.
	 * @param containerStack
	 * The container {@link ItemStack}.
	 * @param valveEntity
	 * The affected {@link ValveBlock}s {@link TileEntity} ({@link ValveBlockEntity}).
	 */
	private static final void drainContainerIntoTank(EntityPlayer player, ItemStack containerStack, ValveBlockEntity valveEntity)
	{
		if (valveEntity.isFull()) return;
		
		boolean doItemExchange = false;
		ItemStack emptyContainer = null;

		if (containerStack.getItem() instanceof IFluidContainerItem)
		{
			// Handle IFluidContainerItem items
			
			doItemExchange = containerNeedsUnstacking(containerStack);
			IFluidContainerItem containerItem = (IFluidContainerItem) containerStack.getItem();
			emptyContainer = containerStack;
			
			if (doItemExchange)
			{
				emptyContainer = containerStack.copy();
				emptyContainer.stackSize = 1;
			}
			
			FluidStack containerFluid = containerItem.getFluid(emptyContainer);
			FluidStack tankFluid = valveEntity.getFluid();

			if (tankFluid == null || tankFluid.isFluidEqual(containerFluid))
			{
				int drainAmount = Math.min(valveEntity.getRemainingCapacity(), containerFluid.amount);
				// Drain the fluid from the container first because the amount per drain could be limited
				FluidStack drainedFluid = containerItem.drain(emptyContainer, drainAmount, true);
				valveEntity.fill(null, drainedFluid, true);
			}
		}
		else
		{
			// Handle empty by exchange items

			FluidStack containerFluid = Utils.getFluidForFilledItem(containerStack);

			// Don't consume the container contents in creative mode
			if (valveEntity.fill(null, containerFluid, true) > 0 && !player.capabilities.isCreativeMode)
			{
				emptyContainer = FluidContainerRegistry.drainFluidContainer(containerStack);
				doItemExchange = (emptyContainer != null);
			}
		}
		
		// Give the emptied container to the player
		
		if (doItemExchange)
		{
			exchangeItems(player, containerStack, emptyContainer);
		}
	}
	
	/**
	 * Exchanges one of the items in the original stack with a new item.
	 * 
	 * @param player
	 * The player in whose inventory the items should be exchanged.
	 * @param originalStack
	 * The original stack of items.
	 * @param newStack
	 * The new item stack.
	 */
	private static final void exchangeItems(EntityPlayer player, ItemStack originalStack, ItemStack newStack)
	{
		if (player == null || originalStack == null || newStack == null) return;
		
		if (--originalStack.stackSize <= 0)
		{
			player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
		}

		// Add new ItemStack to player inventory or drop it to the ground if the inventory is full or we're dealing with a fake player

		if (player instanceof FakePlayer || !player.inventory.addItemStackToInventory(newStack))
		{
			player.worldObj.spawnEntityInWorld(new EntityItem(player.worldObj, player.posX + 0.5D, player.posY + 1.5D, player.posZ + 0.5D, newStack));
		}
		else
		{
			if (player instanceof EntityPlayerMP)
			{
				((EntityPlayerMP)player).sendContainerToPlayer(player.inventoryContainer);
			}
			
			float pitch = ((player.worldObj.rand.nextFloat() - player.worldObj.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F;
            player.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "random.pop", 0.2F, pitch);
		}
	}
	
	/**
	 * Tries to ascertain if a IFluidContainerItem has to be unstacked before manipulating the contents.
	 * 
	 * @param containerStack
	 * A stack of the container item that should be checked.
	 * @return
	 * <code>true</code> if the container item returns the same capacity for different
	 * stack sizes, otherwise <code>false</code>.
	 */
	private static final boolean containerNeedsUnstacking(ItemStack containerStack)
	{
		if (containerStack == null || containerStack.stackSize < 2) return false;
		
		ItemStack sizeOneContainerStack = containerStack.copy();
		sizeOneContainerStack.stackSize = 1;
		
		IFluidContainerItem container = (IFluidContainerItem)containerStack.getItem();
		
		return container.getCapacity(containerStack) == container.getCapacity(sizeOneContainerStack);
	}

	/**
	 * Checks if an item is a container that implements {@code IFluidContainerItem} and is empty.
	 * 
	 * @param item
	 * The container to check.
	 * @return
	 * {@code true} if the container is empty and implements {@code IFluidContainerItem}, otherwise {@code false}.
	 */
	private static final boolean isEmptyComplexContainer(ItemStack item)
	{
		if (item == null) return false;

		if (item.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem) item.getItem();
			FluidStack containerFluid = container.getFluid(item);

			return (containerFluid == null || containerFluid.amount == 0);
		}

		return false;
	}

	/**
	 * Wrapper method for FluidContainerRegistry.getContainerCapacity that overrides the default bottle volume if configured.
	 * 
	 * @param fluid
	 * The fluid the container can hold.
	 * @param container
	 * The container.
	 * @return
	 * The containers capacity or 0 if the container could not be found.
	 */
	private static final int getFluidContainerCapacity(FluidStack fluid, ItemStack container)
	{
		if (fluid == null || container == null) return 0;

		// override default bottle volume
		if (Config.overrideBottleVolume > 0 && (container.isItemEqual(FluidContainerRegistry.EMPTY_BOTTLE) || container.isItemEqual(FILLED_BOTTLE))) return Config.overrideBottleVolume;

		return FluidContainerRegistry.getContainerCapacity(fluid, container);
	}

	/**
	 * Wrapper method for FluidContainerRegistry.fillFluidContainer that overrides the default bottle volume if configured.
	 * 
	 * @param fluid
	 * FluidStack containing the type and amount of fluid to fill.
	 * @param container
	 * ItemStack representing the empty container.
	 * @return
	 * Filled container if successful, otherwise null.
	 */
	private static ItemStack fillFluidContainer(FluidStack fluid, ItemStack container)
	{
		if (container == null || fluid == null) return null;

		// override default bottle volume
		if (Config.overrideBottleVolume > 0 && container.isItemEqual(FluidContainerRegistry.EMPTY_BOTTLE))
		{
			if (cachedFluidContainerData == null)
			{
				// cache container data if we haven't done it already
				cachedFluidContainerData = FluidContainerRegistry.getRegisteredFluidContainerData();
			}

			for (FluidContainerData data : cachedFluidContainerData)
			{
				if (container.isItemEqual(data.emptyContainer) && fluid.isFluidEqual(data.fluid) && fluid.amount >= Config.overrideBottleVolume)
				{
					return data.filledContainer.copy();
				}
			}
		}

		return FluidContainerRegistry.fillFluidContainer(fluid, container);
	}

	/**
	 * Wrapper method for FluidContainerRegistry.getFluidForFilledItem that overrides the default bottle volume.
	 * 
	 * @param filledContainer
	 * The fluid container.
	 * @return
	 * FluidStack representing stored fluid.
	 */
	private static FluidStack getFluidForFilledItem(ItemStack filledContainer)
	{
		if (filledContainer == null) return null;

		FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(filledContainer);

		// override default bottle volume
		if (fluid != null && Config.overrideBottleVolume > 0 && filledContainer.isItemEqual(FILLED_BOTTLE))
		{
			fluid.amount = Config.overrideBottleVolume;
		}

		return fluid;
	}
}
