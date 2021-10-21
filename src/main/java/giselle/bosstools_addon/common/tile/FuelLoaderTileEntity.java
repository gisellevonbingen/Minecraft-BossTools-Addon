package giselle.bosstools_addon.common.tile;

import java.util.List;

import javax.annotation.Nullable;

import giselle.bosstools_addon.common.adapter.FuelEntityAdapter;
import giselle.bosstools_addon.common.adapter.FuelEntityAdapterRocket1;
import giselle.bosstools_addon.common.adapter.FuelEntityAdapterRocket2;
import giselle.bosstools_addon.common.adapter.FuelEntityAdapterRocket3;
import giselle.bosstools_addon.common.adapter.FuelEntityAdapterRover;
import giselle.bosstools_addon.common.fluid.FluidUtil2;
import giselle.bosstools_addon.common.inventory.ItemHandlerHelper2;
import giselle.bosstools_addon.common.inventory.container.FuelLoaderContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.mrscauthd.boss_tools.block.FuelBlock;
import net.mrscauthd.boss_tools.entity.RocketEntity;
import net.mrscauthd.boss_tools.entity.RocketTier2Entity;
import net.mrscauthd.boss_tools.entity.RocketTier3Entity;
import net.mrscauthd.boss_tools.entity.RoverEntity;

public class FuelLoaderTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	public static final int SLOT_FLUID_SOURCE = 0;
	public static final int SLOT_FLUID_SINK = 1;

	private IFluidHandler fluidTank;
	private ItemStackHandler fluidInventory;
	private ItemStackHandler inputInventory;
	private ItemStackHandler outputInventory;
	private CombinedInvWrapper itemHandler;

	public FuelLoaderTileEntity()
	{
		super(AddonTiles.FUEL_LOADER.get());

		this.fluidTank = new FluidTank(8000, fs -> FluidUtil2.isEquivalentTo(fs, this.getFluid()))
		{
			@Override
			protected void onContentsChanged()
			{
				super.onContentsChanged();
				onContentChanged();
			}
		};
		this.fluidInventory = new ItemStackHandler(2)
		{
			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
			{
				if (slot == getInternalSlotFluidSource() && FluidUtil2.canDrain(stack, getFluid()) == true)
				{
					return super.insertItem(slot, stack, simulate);
				}
				else if (slot == getInternalSlotFluidSink() && FluidUtil2.canFill(stack, getFluid()) == true)
				{
					return super.insertItem(slot, stack, simulate);
				}

				return stack;
			}

			@Override
			protected void onContentsChanged(int slot)
			{
				super.onContentsChanged(slot);
				onContentChanged();
			}

			@Override
			public int getSlotLimit(int slot)
			{
				return 1;
			}

		};
		this.inputInventory = new ItemStackHandler(6)
		{
			@Override
			protected void onContentsChanged(int slot)
			{
				super.onContentsChanged(slot);
				onContentChanged();
			}
		};
		this.outputInventory = new ItemStackHandler(6)
		{
			@Override
			protected void onContentsChanged(int slot)
			{
				super.onContentsChanged(slot);
				onContentChanged();
			}
		};
		this.itemHandler = new CombinedInvWrapper(this.getFluidInventory(), this.getInputInventory(), this.getOutputInventory())
		{
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate)
			{
				int index = this.getIndexForSlot(slot);
				int handlerSlot = this.getSlotFromIndex(slot, index);
				IItemHandlerModifiable handler = this.getHandlerFromIndex(index);

				if (handler == getInputInventory())
				{
					return ItemStack.EMPTY;
				}
				else if (handler == getFluidInventory())
				{
					ItemStack stack = this.getStackInSlot(slot);

					if (handlerSlot == getInternalSlotFluidSource() && FluidUtil2.canDrain(stack, getFluid()) == false)
					{
						return super.extractItem(slot, amount, simulate);
					}
					else if (handlerSlot == getInternalSlotFluidSink() && FluidUtil2.canFill(stack, getFluid()) == false)
					{
						return super.extractItem(slot, amount, simulate);
					}

					return ItemStack.EMPTY;
				}

				return super.extractItem(slot, amount, simulate);
			};

			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
			{
				int index = this.getIndexForSlot(slot);
				int handlerSlot = this.getSlotFromIndex(slot, index);
				IItemHandlerModifiable handler = this.getHandlerFromIndex(index);

				if (handler == getFluidInventory() && handlerSlot == getInternalSlotFluidSink())
				{
					return stack;
				}
				else if (handler == getOutputInventory())
				{
					return stack;
				}

				return super.insertItem(slot, stack, simulate);
			};

		};
	}

	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(this.getBlockPos(), 0, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag()
	{
		return this.save(new CompoundNBT());
	}

	@Override
	public final void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
	{
		this.load(this.getBlockState(), packet.getTag());
	}

	protected void onContentChanged()
	{
		this.setChanged();

		AbstractChunkProvider chunkSource = this.getLevel().getChunkSource();

		if (chunkSource instanceof ServerChunkProvider)
		{
			((ServerChunkProvider) chunkSource).blockChanged(this.getBlockPos());
		}

	}

	public void openGui(ServerPlayerEntity entity)
	{
		NetworkHooks.openGui(entity, this, this.getBlockPos());
	}

	@Override
	@Nullable
	public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player)
	{
		return new FuelLoaderContainer(windowId, inv, this);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("container.boss_tools_addon.fuel_loader");
	}

	@Override
	public void load(BlockState blockState, CompoundNBT compound)
	{
		super.load(blockState, compound);

		this.readMetadata(compound);
	}

	@Override
	public CompoundNBT save(CompoundNBT compound)
	{
		super.save(compound);

		this.saveMetadata(compound);

		return compound;
	}

	protected void readMetadata(CompoundNBT compound)
	{
		CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(this.getFluidTank(), null, compound.getCompound("fluidTank"));
		this.fluidInventory.deserializeNBT(compound.getCompound("fluidInventory"));
		this.inputInventory.deserializeNBT(compound.getCompound("inputInventory"));
		this.outputInventory.deserializeNBT(compound.getCompound("outputInventory"));
	}

	protected void saveMetadata(CompoundNBT compound)
	{
		compound.put("fluidTank", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(this.getFluidTank(), null));
		compound.put("fluidInventory", this.fluidInventory.serializeNBT());
		compound.put("inputInventory", this.inputInventory.serializeNBT());
		compound.put("outputInventory", this.outputInventory.serializeNBT());
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return LazyOptional.of(this::getFluidTank).cast();
		}
		else if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return LazyOptional.of(this::getItemHandler).cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public void tick()
	{
		this.processTank();
		this.exchangeFuelItemAround();
	}

	public void processTank()
	{
		IItemHandlerModifiable itemHandler = this.getFluidInventory();
		int transferPerTick = this.getTransferPerTick();
		IFluidHandler fluidTank = this.getFluidTank();
		FluidUtil2.drainSource(itemHandler, this.getInternalSlotFluidSource(), fluidTank, transferPerTick);
		FluidUtil2.fillSink(itemHandler, this.getInternalSlotFluidSink(), fluidTank, transferPerTick);
	}

	public int getTransferPerTick()
	{
		return 256;
	}

	public boolean exchangeFuelItemAround()
	{
		World level = this.getLevel();
		double workingLength = this.getWorkingLength();
		AxisAlignedBB workingArea = new AxisAlignedBB(this.getBlockPos()).inflate(workingLength, 0.0D, workingLength);
		List<Entity> entities = level.getEntities(null, workingArea);
		boolean worked = false;

		for (Entity entity : entities)
		{
			FuelEntityAdapter adapter = this.createAdapter(entity);

			if (this.exchangeFuelItem(adapter) == true)
			{
				worked = true;
			}

		}

		return worked;
	}

	public double getWorkingLength()
	{
		return 2.0D;
	}

	public FuelEntityAdapter createAdapter(Entity entity)
	{
		FuelEntityAdapter adapter = null;

		if (entity instanceof RoverEntity.CustomEntity)
		{
			adapter = new FuelEntityAdapterRover((RoverEntity.CustomEntity) entity);
		}
		else if (entity instanceof RocketEntity.CustomEntity)
		{
			adapter = new FuelEntityAdapterRocket1((RocketEntity.CustomEntity) entity);
		}
		else if (entity instanceof RocketTier2Entity.CustomEntity)
		{
			adapter = new FuelEntityAdapterRocket2((RocketTier2Entity.CustomEntity) entity);
		}
		else if (entity instanceof RocketTier3Entity.CustomEntity)
		{
			adapter = new FuelEntityAdapterRocket3((RocketTier3Entity.CustomEntity) entity);
		}

		return adapter;
	}

	public boolean exchangeFuelItem(FuelEntityAdapter adapter)
	{
		if (adapter == null)
		{
			return false;
		}

		IItemHandler _itemHandler = adapter.getEntity().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);

		if (_itemHandler instanceof IItemHandlerModifiable)
		{
			IItemHandlerModifiable itemHandler = (IItemHandlerModifiable) _itemHandler;
			int fuelSlot = adapter.getFuelSlot();
			this.takeEmptyItem(adapter, itemHandler, fuelSlot);

			if (adapter.canInsertFuel() == true)
			{
				this.giveFullitem(adapter, itemHandler, fuelSlot);
			}

		}

		return false;
	}

	public void giveFullitem(FuelEntityAdapter adapter, IItemHandlerModifiable itemHandler, int fuelSlot)
	{
		ItemStack stackInSlot = itemHandler.getStackInSlot(fuelSlot);

		if (stackInSlot.isEmpty() == true)
		{
			Fluid fluid = this.getFluid();
			ItemStack fuelFullItem = new ItemStack(adapter.getFuelFullItem());
			ItemStack fuelEmptyItem = FluidUtil2.makeEmpty(fuelFullItem, fluid);
			FluidStack fluidStack = new FluidStack(fluid, FluidUtil2.getMaxCapacity(fuelEmptyItem));
			IFluidHandler fluidTank = this.getFluidTank();

			IItemHandlerModifiable inputInventory = this.getInputInventory();
			int inputSlot = ItemHandlerHelper2.indexOf(inputInventory, fuelEmptyItem.getItem());

			if (inputSlot > -1 && fluidTank.drain(fluidStack, FluidAction.SIMULATE).getAmount() == fluidStack.getAmount())
			{
				inputInventory.extractItem(inputSlot, 1, false);
				itemHandler.setStackInSlot(fuelSlot, fuelFullItem);
				fluidTank.drain(fluidStack, FluidAction.EXECUTE);
			}

		}

	}

	public void takeEmptyItem(FuelEntityAdapter adapter, IItemHandlerModifiable itemHandler, int fuelSlot)
	{
		ItemStack stackInSlot = itemHandler.getStackInSlot(fuelSlot);

		if (FluidUtil2.canDrain(stackInSlot, this.getFluid()) == false)
		{
			ItemStack remain = ItemHandlerHelper.insertItemStacked(this.getOutputInventory(), stackInSlot, false);
			itemHandler.setStackInSlot(fuelSlot, remain);
		}

	}

	public Fluid getFluid()
	{
		return FuelBlock.still;
	}

	public IFluidHandler getFluidTank()
	{
		return this.fluidTank;
	}

	public int getInternalSlotFluidSource()
	{
		return SLOT_FLUID_SOURCE;
	}

	public int getInternalSlotFluidSink()
	{
		return SLOT_FLUID_SINK;
	}

	public IItemHandlerModifiable getFluidInventory()
	{
		return this.fluidInventory;
	}

	public IItemHandlerModifiable getInputInventory()
	{
		return this.inputInventory;
	}

	public IItemHandlerModifiable getOutputInventory()
	{
		return this.outputInventory;
	}

	public CombinedInvWrapper getItemHandler()
	{
		return this.itemHandler;
	}

}
