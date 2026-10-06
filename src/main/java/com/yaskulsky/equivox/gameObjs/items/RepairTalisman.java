package com.yaskulsky.equivox.gameObjs.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import com.yaskulsky.equivox.api.block_entity.IDMPedestal;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.api.capabilities.item.IAlchBagItem;
import com.yaskulsky.equivox.api.inventory.PEItemStacksHandler;
import com.yaskulsky.equivox.api.capabilities.item.IAlchChestItem;
import com.yaskulsky.equivox.api.capabilities.item.IPedestalItem;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.registries.PEDataComponentTypes;
import com.yaskulsky.equivox.integration.IntegrationHelper;
import com.yaskulsky.equivox.utils.ItemHelper;
import com.yaskulsky.equivox.utils.MathUtils;
import com.yaskulsky.equivox.utils.PlayerHelper;
import com.yaskulsky.equivox.utils.WorldHelper;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import com.yaskulsky.equivox.api.inventory.PEItemStacksHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RepairTalisman extends ItemPE implements IAlchBagItem, IAlchChestItem, IPedestalItem, ICapabilityAware, ISelfCraftingRemainder {

	private static final BiPredicate<ItemStack, Void> CAN_REPAIR_ITEM = (stack, ignored) -> !stack.isEmpty() &&
																							stack.getCapability(PECapabilities.MODE_CHANGER_ITEM_CAPABILITY) == null &&
																							ItemHelper.isRepairableDamagedItem(stack);
	private static final BiPredicate<ItemStack, Player> CAN_REPAIR_PLAYER_ITEM =
			(stack, player) -> CAN_REPAIR_ITEM.test(stack, null) && (stack != player.getMainHandItem() || !player.isSwinging());

	public RepairTalisman(Properties props) {
		super(props.component(PEDataComponentTypes.COOLDOWN, (byte) 0));
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity, @Nullable EquipmentSlot slot) {
		super.inventoryTick(stack, level, entity, slot);
		if (!level.isClientSide() && entity instanceof Player player && PlayerHelper.checkCooldown(player, this, EquivoxConfig.server.cooldown.player.repair)) {
			repairAllItems(player);
		}
	}

	@Override
	public <PEDESTAL extends BlockEntity & IDMPedestal> boolean updateInPedestal(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos,
			@NotNull PEDESTAL pedestal) {
		if (!level.isClientSide() && EquivoxConfig.server.cooldown.pedestal.repair.get() != -1) {
			if (pedestal.getActivityCooldown() == 0) {
				level.getEntitiesOfClass(Player.class, pedestal.getEffectBounds()).forEach(RepairTalisman::repairAllItems);
				pedestal.setActivityCooldown(level, pos, EquivoxConfig.server.cooldown.pedestal.repair.get());
			} else {
				pedestal.decrementActivityCooldown(level, pos);
			}
		}
		return false;
	}

	@NotNull
	@Override
	public List<Component> getPedestalDescription(float tickRate) {
		List<Component> list = new ArrayList<>();
		if (EquivoxConfig.server.cooldown.pedestal.repair.get() != -1) {
			list.add(PELang.PEDESTAL_REPAIR_TALISMAN_1.translateColored(ChatFormatting.BLUE));
			list.add(PELang.PEDESTAL_REPAIR_TALISMAN_2.translateColored(ChatFormatting.BLUE, MathUtils.tickToSecFormatted(EquivoxConfig.server.cooldown.pedestal.repair.get(), tickRate)));
		}
		return list;
	}

	@Override
	public boolean updateInAlchChest(@NotNull Level level, @NotNull BlockPos pos, @NotNull ItemStack stack) {
		if (!level.isClientSide()) {
			ResourceHandler<ItemResource> inv = WorldHelper.getItemHandler(level, pos, null);
			if (inv != null) {
				return updateInHandler(inv, stack);
			}
		}
		return false;
	}

	@Override
	public boolean updateInAlchBag(@NotNull PEItemStacksHandler inv, @NotNull Player player, @NotNull ItemStack stack) {
		return !player.level().isClientSide() && updateInHandler(inv, stack);
	}

	private boolean updateInHandler(@NotNull ResourceHandler<ItemResource> inv, @NotNull ItemStack stack) {
		byte coolDown = stack.getOrDefault(PEDataComponentTypes.COOLDOWN, (byte) 0);
		if (coolDown > 0) {
			stack.set(PEDataComponentTypes.COOLDOWN, (byte) (coolDown - 1));
			return true;
		} else if (repairAllItems(inv, null, CAN_REPAIR_ITEM)) {
			stack.set(PEDataComponentTypes.COOLDOWN, (byte) 19);
			return true;
		}
		return false;
	}

	@Override
	public void attachCapabilities(RegisterCapabilitiesEvent event) {
		IntegrationHelper.registerCuriosCapability(event, this);
	}

	private static void repairAllItems(Player player) {
		repairPlayerInventory(player.getInventory(), player);
		ResourceHandler<ItemResource> curios = IntegrationHelper.getCurioItemHandler(player);
		if (curios != null) {
			repairAllItems(curios, player, CAN_REPAIR_PLAYER_ITEM);
		}
	}

	private static boolean repairPlayerInventory(Inventory inventory, Player player) {
		boolean hasAction = false;
		for (int i = 0, slots = inventory.getContainerSize(); i < slots; i++) {
			ItemStack stack = inventory.getItem(i);
			if (CAN_REPAIR_PLAYER_ITEM.test(stack, player)) {
				stack.setDamageValue(stack.getDamageValue() - 1);
				hasAction = true;
			}
		}
		return hasAction;
	}

	private static <DATA> boolean repairAllItems(@Nullable ResourceHandler<ItemResource> inv, DATA data, BiPredicate<ItemStack, DATA> canRepairStack) {
		if (inv == null) {
			return false;
		}
		boolean hasAction = false;
		for (int i = 0, slots = PEItemStacksHandler.getSlotCount(inv); i < slots; i++) {
			ItemStack invStack = PEItemStacksHandler.getStack(inv, i);
			if (!canRepairStack.test(invStack, data)) {
				continue;
			}
			ItemStack repaired = invStack.copy();
			repaired.setDamageValue(repaired.getDamageValue() - 1);
			if (inv instanceof PEItemStacksHandler modifiable) {
				modifiable.setStackInSlot(i, repaired);
			} else {
				invStack.setDamageValue(invStack.getDamageValue() - 1);
			}
			hasAction = true;
		}
		return hasAction;
	}
}