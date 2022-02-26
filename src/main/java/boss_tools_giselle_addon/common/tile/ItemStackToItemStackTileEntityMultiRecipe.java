package boss_tools_giselle_addon.common.tile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import boss_tools_giselle_addon.common.inventory.ItemHandlerHelper3;
import boss_tools_giselle_addon.common.item.crafting.IS2ISRecipeCache;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.mrscauthd.boss_tools.crafting.ItemStackToItemStackRecipe;
import net.mrscauthd.boss_tools.crafting.ItemStackToItemStackRecipeType;
import net.mrscauthd.boss_tools.gauge.IGaugeValue;
import net.mrscauthd.boss_tools.inventory.StackCacher;
import net.mrscauthd.boss_tools.machines.tile.ItemStackToItemStackTileEntity;

public abstract class ItemStackToItemStackTileEntityMultiRecipe extends ItemStackToItemStackTileEntity
{
	public static final String KEY_AUTO_PULL = "auto_pull";
	public static final String KEY_AUTO_EJECT = "auto_eject";
	public static final String KEY_AUTO_TIMER = "auto_timer";

	private StackCacher itemStackCacher;
	private ItemStackToItemStackRecipe cachedRecipe = null;

	public ItemStackToItemStackTileEntityMultiRecipe(TileEntityType<? extends ItemStackToItemStackTileEntityMultiRecipe> type)
	{
		super(type);

		this.itemStackCacher = new StackCacher();
		this.cachedRecipe = null;
	}

	@Override
	public List<IGaugeValue> getGaugeValues()
	{
		List<IGaugeValue> list = super.getGaugeValues();

		if (this.getCachedRecipe() != null)
		{
			list.add(this.getCookTimeGaugeValue());
		}

		return list;
	}

	public ItemStackToItemStackRecipe getCachedRecipe()
	{
		return this.cachedRecipe;
	}

	@Override
	protected boolean onCanInsertItem(int index, ItemStack stack, @Nullable Direction direction)
	{
		if (index == this.getSlotIngredient() && this.nullOrMatch(direction, Direction.UP))
		{
			return this.test(stack) != null;
		}
		else if (index == this.getSlotOutput() && direction == null)
		{
			return true;
		}

		return super.onCanInsertItem(index, stack, direction);
	}

	public List<IRecipeType<? extends IRecipe<IInventory>>> getRecipeTypes()
	{
		return new ArrayList<>();
	}

	public ItemStackToItemStackRecipe test(ItemStack itemStack)
	{
		World world = this.getLevel();
		return IS2ISRecipeCache.cache(world.getRecipeManager(), world, itemStack, this.getRecipeTypes());
	}

	@Override
	public ItemStackToItemStackRecipeType<?> getRecipeType()
	{
		return null;
	}

	protected void clearRecipeCache()
	{
		this.itemStackCacher.set(ItemStack.EMPTY);
		this.cachedRecipe = null;
		this.setMaxTimer(0);
		this.resetTimer();
	}

	@Override
	protected void tickProcessing()
	{
		super.tickProcessing();

		this.tickAutoTimer();
	}

	protected void tickAutoTimer()
	{
		int autoTimer = this.getAutoTimer();
		autoTimer++;

		if (autoTimer >= this.getAutoMaxTimer())
		{
			autoTimer = 0;
			int amount = 1;

			if (this.isAutoPull() == true)
			{
				this.tryPull(amount);
			}

			if (this.isAutoEject() == true)
			{
				this.tryEject(amount);
			}

		}

		this.setAutoTimer(autoTimer);
	}

	protected LazyOptional<IItemHandler> getTargetItemHandler(Direction direction)
	{
		BlockPos pos = this.getBlockPos().offset(direction.getNormal());
		TileEntity blockEntity = this.getLevel().getBlockEntity(pos);

		if (blockEntity != null)
		{
			return blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
		}
		else
		{
			return LazyOptional.empty();
		}

	}

	protected void tryPull(int amount)
	{
		Direction direction = Direction.UP;
		IItemHandler fromItemHandler = this.getTargetItemHandler(direction).orElse(null);

		if (fromItemHandler != null)
		{
			IItemHandler toItemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(null);

			if (fromItemHandler != null)
			{
				ItemHandlerHelper3.tryStackTransfer(fromItemHandler, toItemHandler, amount);
			}

		}

	}

	protected void tryEject(int amount)
	{
		Direction direction = Direction.DOWN;
		IItemHandler toItemHandler = this.getTargetItemHandler(direction).orElse(null);

		if (toItemHandler != null)
		{
			IItemHandler fromItemHandler = this.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(null);

			if (fromItemHandler != null)
			{
				ItemHandlerHelper3.tryStackTransfer(fromItemHandler, toItemHandler, amount);
			}

		}

	}

	@Override
	protected ItemStackToItemStackRecipe cacheRecipe()
	{
		ItemStack itemStack = this.getItem(this.getSlotIngredient());

		if (itemStack == null || itemStack.isEmpty())
		{
			this.itemStackCacher.set(itemStack);
			this.cachedRecipe = null;
			this.setMaxTimer(0);
		}
		else if (!this.itemStackCacher.test(itemStack))
		{
			this.itemStackCacher.set(itemStack);
			this.cachedRecipe = this.test(itemStack);

			if (this.cachedRecipe != null)
			{
				this.setMaxTimer(this.cachedRecipe.getCookTime());
			}
			else
			{
				this.setMaxTimer(0);
			}

		}

		return this.cachedRecipe;
	}

	public boolean isAutoPull()
	{
		return this.getTileData().getBoolean(KEY_AUTO_PULL);
	}

	public void setAutoPull(boolean autoPull)
	{
		if (this.isAutoPull() != autoPull)
		{
			this.getTileData().putBoolean(KEY_AUTO_PULL, autoPull);
			this.setChanged();
		}

	}

	public boolean isAutoEject()
	{
		return this.getTileData().getBoolean(KEY_AUTO_EJECT);
	}

	public void setAutoEject(boolean autoEject)
	{
		if (this.isAutoEject() != autoEject)
		{
			this.getTileData().putBoolean(KEY_AUTO_EJECT, autoEject);
			this.setChanged();
		}

	}

	public int getAutoTimer()
	{
		return this.getTileData().getInt(KEY_AUTO_TIMER);
	}

	public void setAutoTimer(int autoTimer)
	{
		autoTimer = Math.max(autoTimer, 0);

		if (this.getAutoTimer() != autoTimer)
		{
			this.getTileData().putInt(KEY_AUTO_TIMER, autoTimer);
			this.setChanged();
		}

	}

	public int getAutoMaxTimer()
	{
		return 10;
	}

}
