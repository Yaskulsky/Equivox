package com.yaskulsky.equivox.gameObjs.container;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.yaskulsky.equivox.api.capabilities.IKnowledgeProvider;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.container.slots.arcane.ArcaneTabletHelper;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class ArcaneCraftingLogic {

	private static final int[] ROTATION_SLOTS = {0, 1, 2, 5, 8, 7, 6, 3};

	private ArcaneCraftingLogic() {
	}

	public static void slotChangedCraftingGrid(IArcaneCraftingMenu menu) {
		Player player = menu.getCraftingPlayer();
		Level level = player.level();
		if (level.isClientSide()) {
			return;
		}
		TransientCraftingContainer crafting = menu.getCraftSlots();
		ResultContainer result = menu.getResultSlots();
		CraftingInput input = CraftingInput.of(3, 3, crafting.getItems());
		ItemStack stack = ItemStack.EMPTY;
		Optional<RecipeHolder<CraftingRecipe>> optional = Objects.requireNonNull(level.getServer())
				.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
		if (optional.isPresent()) {
			RecipeHolder<CraftingRecipe> recipe = optional.get();
			stack = recipe.value().assemble(input);
			result.setRecipeUsed(recipe);
		}
		result.setItem(0, stack);
		menu.applyCraftingResult(stack);
	}

	public static void onRecipeTransfer(IArcaneCraftingMenu menu, List<List<ItemStack>> recipe, boolean transferAll) {
		clearCrafting(menu, false);
		fillCraftingSlots(menu, recipe, transferAll);
	}

	public static void fillCraftingSlots(IArcaneCraftingMenu menu, List<List<ItemStack>> recipe, boolean transferAll) {
		TransientCraftingContainer craftSlots = menu.getCraftSlots();
		int max = Math.min(recipe.size(), craftSlots.getContainerSize());
		transferItems(menu, recipe, max);
		if (transferAll) {
			for (int i = 0; i < 63; i++) {
				transferItems(menu, recipe, max);
			}
		}
		Player player = menu.getCraftingPlayer();
		if (player instanceof ServerPlayer serverPlayer) {
			menu.getProvider().syncEmc(serverPlayer);
		}
		slotChangedCraftingGrid(menu);
	}

	public static void transferItems(IArcaneCraftingMenu menu, List<List<ItemStack>> recipe, int max) {
		for (int i = 0; i < max; i++) {
			if (recipe.get(i) != null && !recipe.get(i).isEmpty()) {
				transferFromInventory(menu, i, recipe.get(i));
			}
		}
		for (int i = 0; i < max; i++) {
			if (recipe.get(i) != null && !recipe.get(i).isEmpty()) {
				transferFromTablet(menu, i, recipe.get(i));
			}
		}
	}

	private static boolean transferFromTablet(IArcaneCraftingMenu menu, int slot, List<ItemStack> possibilities) {
		IKnowledgeProvider provider = menu.getProvider();
		TransientCraftingContainer craftSlots = menu.getCraftSlots();
		List<ItemStack> sorted = new ArrayList<>(possibilities);
		sorted.sort(Comparator.comparingLong(IEMCProxy.INSTANCE::getValue));
		for (ItemStack stack : sorted) {
			ItemStack cleaned = ArcaneTabletHelper.cleanStack(stack);
			if (!provider.hasKnowledge(cleaned)) {
				continue;
			}
			long value = IEMCProxy.INSTANCE.getValue(cleaned);
			if (value <= 0 || provider.getEmc().compareTo(BigInteger.valueOf(value)) < 0) {
				continue;
			}
			ItemStack slotItem = craftSlots.getItem(slot);
			if (slotItem.isEmpty()) {
				craftSlots.setItem(slot, cleaned);
			} else if (slotItem.getCount() < slotItem.getMaxStackSize()
					&& ArcaneTabletHelper.areStacksEqual(slotItem, cleaned)) {
				slotItem.grow(1);
			} else {
				continue;
			}
			provider.setEmc(provider.getEmc().subtract(BigInteger.valueOf(value)));
			return true;
		}
		return false;
	}

	private static boolean transferFromInventory(IArcaneCraftingMenu menu, int slot, List<ItemStack> possibilities) {
		Inventory playerInv = menu.getCraftingPlayerInventory();
		TransientCraftingContainer craftSlots = menu.getCraftSlots();
		for (ItemStack possibility : possibilities) {
			ItemStack cleaned = ArcaneTabletHelper.cleanStack(possibility);
			for (int j = 0; j < playerInv.getContainerSize(); j++) {
				ItemStack stack = playerInv.getItem(j);
				if (!ArcaneTabletHelper.areStacksEqual(cleaned, ArcaneTabletHelper.cleanStack(stack))) {
					continue;
				}
				ItemStack slotItem = craftSlots.getItem(slot);
				if (slotItem.isEmpty()) {
					craftSlots.setItem(slot, stack.copyWithCount(1));
				} else if (slotItem.getCount() < slotItem.getMaxStackSize()
						&& ArcaneTabletHelper.areStacksEqual(slotItem, stack)) {
					slotItem.grow(1);
				} else {
					continue;
				}
				stack.shrink(1);
				if (stack.isEmpty()) {
					playerInv.setItem(j, ItemStack.EMPTY);
				}
				return true;
			}
		}
		return false;
	}

	public static void clearCrafting(IArcaneCraftingMenu menu, boolean force) {
		TransientCraftingContainer craftSlots = menu.getCraftSlots();
		Player player = menu.getCraftingPlayer();
		Inventory playerInv = menu.getCraftingPlayerInventory();
		IKnowledgeProvider provider = menu.getProvider();
		boolean emcUpdate = false;
		for (int i = 0; i < craftSlots.getContainerSize(); i++) {
			ItemStack stack = craftSlots.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			if (EquivoxConfig.server.difficulty.covalenceLoss.get() >= 1.0D && IEMCProxy.INSTANCE.hasValue(stack)
					&& ArcaneTabletHelper.tryLearnAndConvertToEmc(player, provider, stack)) {
				emcUpdate = true;
				craftSlots.setItem(i, ItemStack.EMPTY);
				continue;
			}
			craftSlots.setItem(i, ArcaneTabletHelper.returnToInventory(playerInv, player, stack, force));
		}
		if (emcUpdate && player instanceof ServerPlayer serverPlayer) {
			provider.syncEmc(serverPlayer);
		}
		AbstractContainerMenu asMenu = menu.asMenu();
		asMenu.slotsChanged(craftSlots);
		craftSlots.setChanged();
	}

	public static void rotateCrafting(IArcaneCraftingMenu menu, boolean clockwise) {
		TransientCraftingContainer craftSlots = menu.getCraftSlots();
		ItemStack[] stacks = new ItemStack[ROTATION_SLOTS.length];
		if (clockwise) {
			for (int i = 0; i < ROTATION_SLOTS.length; i++) {
				int j = i - 1;
				if (j < 0) {
					j = ROTATION_SLOTS.length - 1;
				}
				stacks[i] = craftSlots.getItem(ROTATION_SLOTS[j]);
			}
		} else {
			for (int i = 0; i < ROTATION_SLOTS.length; i++) {
				stacks[i] = craftSlots.getItem(ROTATION_SLOTS[(i + 1) % ROTATION_SLOTS.length]);
			}
		}
		for (int i = 0; i < ROTATION_SLOTS.length; i++) {
			craftSlots.setItem(ROTATION_SLOTS[i], stacks[i]);
		}
		menu.asMenu().slotsChanged(craftSlots);
		craftSlots.setChanged();
	}

	public static void balanceCrafting(IArcaneCraftingMenu menu) {
		TransientCraftingContainer craftSlots = menu.getCraftSlots();
		ArrayListMultimap<String, ItemStack> map = ArrayListMultimap.create();
		Multiset<String> itemCount = HashMultiset.create();
		for (int i = 0; i < craftSlots.getContainerSize(); i++) {
			ItemStack stack = craftSlots.getItem(i);
			if (!stack.isEmpty() && stack.getMaxStackSize() > 1) {
				String key = stackKey(stack);
				map.put(key, stack);
				itemCount.add(key, stack.getCount());
			}
		}
		for (String key : map.keySet()) {
			List<ItemStack> list = map.get(key);
			int totalCount = itemCount.count(key);
			int countPerStack = totalCount / list.size();
			int restCount = totalCount % list.size();
			for (ItemStack stack : list) {
				stack.setCount(countPerStack);
			}
			int idx = 0;
			while (restCount > 0) {
				ItemStack stack = list.get(idx);
				if (stack.getCount() < stack.getMaxStackSize()) {
					stack.grow(1);
					restCount--;
				}
				idx++;
				if (idx >= list.size()) {
					idx = 0;
				}
			}
		}
		menu.asMenu().slotsChanged(craftSlots);
		craftSlots.setChanged();
	}

	public static void spreadCrafting(IArcaneCraftingMenu menu) {
		TransientCraftingContainer craftSlots = menu.getCraftSlots();
		while (true) {
			ItemStack biggestStack = null;
			int biggestSize = 1;
			for (int i = 0; i < craftSlots.getContainerSize(); i++) {
				ItemStack stack = craftSlots.getItem(i);
				if (!stack.isEmpty() && stack.getCount() > biggestSize) {
					biggestStack = stack;
					biggestSize = stack.getCount();
				}
			}
			if (biggestStack == null) {
				return;
			}
			boolean emptyBiggestSlot = false;
			for (int i = 0; i < craftSlots.getContainerSize(); i++) {
				ItemStack stack = craftSlots.getItem(i);
				if (stack.isEmpty()) {
					if (biggestStack.getCount() > 1) {
						craftSlots.setItem(i, biggestStack.split(1));
					} else {
						emptyBiggestSlot = true;
					}
				}
			}
			if (!emptyBiggestSlot) {
				break;
			}
		}
		balanceCrafting(menu);
	}

	private static String stackKey(ItemStack stack) {
		String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		DataComponentPatch patch = stack.getComponentsPatch();
		return id + "|" + patch;
	}
}
