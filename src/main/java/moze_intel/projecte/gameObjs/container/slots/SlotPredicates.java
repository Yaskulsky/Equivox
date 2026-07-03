package moze_intel.projecte.gameObjs.container.slots;

import java.util.function.Predicate;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.entity.FuelValues;

public final class SlotPredicates {

	private static FuelValues furnaceFuelValues;

	private static FuelValues getFurnaceFuelValues() {
		FuelValues values = furnaceFuelValues;
		if (values == null) {
			values = FuelValues.vanillaBurnTimes(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), FeatureFlags.DEFAULT_FLAGS);
			furnaceFuelValues = values;
		}
		return values;
	}

	public static final Predicate<ItemStack> ALWAYS_FALSE = input -> false;

	public static final Predicate<ItemStack> HAS_EMC = IEMCProxy.INSTANCE::hasValue;

	public static final Predicate<ItemStack> COLLECTOR_LOCK = FuelMapper::isStackFuel;

	public static final Predicate<ItemStack> COLLECTOR_INV = input -> input.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY) != null ||
																	  (FuelMapper.isStackFuel(input) && !FuelMapper.isStackMaxFuel(input));

	// slotrelayklein, slotmercurialklein
	public static final Predicate<ItemStack> EMC_HOLDER = input -> input.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY) != null;

	// slotrelayinput
	public static final Predicate<ItemStack> RELAY_INV = input -> EMC_HOLDER.test(input) || HAS_EMC.test(input);

	public static final Predicate<ItemStack> FURNACE_FUEL = input -> EMC_HOLDER.test(input) || input.getBurnTime(RecipeType.SMELTING, getFurnaceFuelValues()) > 0;

	public static final Predicate<ItemStack> MERCURIAL_TARGET = input -> {
		if (input.isEmpty()) {
			return false;
		}
		BlockState state = ItemHelper.stackToState(input, null);
		return state != null && !state.hasBlockEntity() && IEMCProxy.INSTANCE.hasValue(input);
	};

	private SlotPredicates() {
	}
}
