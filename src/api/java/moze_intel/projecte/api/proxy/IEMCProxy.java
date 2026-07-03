package moze_intel.projecte.api.proxy;

import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.ToLongFunction;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.IComponentProcessorHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface IEMCProxy extends ToLongFunction<ItemInfo> {

	/**
	 * The proxy for EMC-based API queries.
	 */
	IEMCProxy INSTANCE = ServiceLoader.load(IEMCProxy.class).findFirst()
			.orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IEMCProxy found, ProjectE may be absent, damaged, or outdated"));

	/**
	 * Queries the EMC value registry if the given item has an EMC value
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 *
	 * @param itemLike The item we want to query
	 *
	 * @return Whether the item has an emc value
	 */
	default boolean hasValue(@NotNull ItemLike itemLike) {
		return getValue(itemLike) > 0;
	}

	/**
	 * Queries the EMC value registry if the given item has an EMC value
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 *
	 * @param holder Represents the item we want to query
	 *
	 * @return Whether the item has an emc value
	 */
	default boolean hasValue(@NotNull Holder<Item> holder) {
		return getValue(holder) > 0;
	}

	/**
	 * Queries the EMC value registry if the given ItemStack has an EMC value
	 * <p>
	 * This will also use the damage value to check if the Item has an EMC value
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 *
	 * @param stack The stack we want to query
	 *
	 * @return Whether the ItemStack has an emc value
	 */
	default boolean hasValue(@NotNull ItemStack stack) {
		return getValue(stack) > 0;
	}

	/**
	 * Queries the EMC value registry if the given ItemInfo has an EMC value
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 *
	 * @param info The ItemInfo we want to query
	 *
	 * @return Whether the ItemInfo has an emc value
	 */
	default boolean hasValue(@NotNull ItemInfo info) {
		return getValue(info) > 0;
	}

	/**
	 * Queries the EMC value for the provided item
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 *
	 * @param itemLike The item we want to query
	 *
	 * @return The item's EMC value, or 0 if there is none
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	default long getValue(@NotNull ItemLike itemLike) {
		Item item = Objects.requireNonNull(itemLike).asItem();
		return item == Items.AIR ? 0 : getValue(ItemInfo.fromItem(item));
	}

	/**
	 * Queries the EMC value for the provided item
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 *
	 * @param holder Represents the item we want to query
	 *
	 * @return The item's EMC value, or 0 if there is none
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	default long getValue(@NotNull Holder<Item> holder) {
		Objects.requireNonNull(holder);
		return !holder.isBound() || holder.value() == Items.AIR ? 0 : getValue(ItemInfo.fromItem(holder));
	}

	/**
	 * Queries the EMC value for the provided stack
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 * <p>
	 * This takes into account bonuses such as stored emc in power items and enchantments
	 *
	 * @param stack The stack we want to query. Does not take into account stack size.
	 *
	 * @return The stack's EMC value, or 0 if there is none
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	default long getValue(@NotNull ItemStack stack) {
		return Objects.requireNonNull(stack).isEmpty() ? 0 : getValue(ItemInfo.fromStack(stack));
	}

	/**
	 * Queries the EMC value for the provided ItemInfo
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 * <p>
	 * This takes into account bonuses such as stored emc in power items and enchantments
	 *
	 * @param info The ItemInfo we want to query
	 *
	 * @return The stack's EMC value, or 0 if there is none
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	long getValue(@NotNull ItemInfo info);

	@Override
	@Range(from = 0, to = Long.MAX_VALUE)
	default long applyAsLong(@NotNull ItemInfo info) {
		return getValue(info);
	}

	/**
	 * Queries the EMC sell-value for the provided stack
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 *
	 * @param stack The stack we want to query. Does not take into account stack size.
	 *
	 * @return EMC the stack should yield when burned by transmutation, condensers, or relays
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	default long getSellValue(@NotNull ItemStack stack) {
		return Objects.requireNonNull(stack).isEmpty() ? 0 : getSellValue(ItemInfo.fromStack(stack));
	}

	/**
	 * Queries the EMC sell-value for the provided ItemInfo
	 * <p>
	 * Can be called at any time, but will only return valid results if a world is loaded
	 * <p>
	 * Can be called on both sides
	 *
	 * @param info The ItemInfo we want to query
	 *
	 * @return EMC the stack should yield when burned by transmutation, condensers, or relays
	 */
	@Range(from = 0, to = Long.MAX_VALUE)
	long getSellValue(@NotNull ItemInfo info);

	/**
	 * Gets an {@link ItemInfo} with the {@link net.minecraft.core.component.DataComponentPatch} reduced to what will be saved to knowledge/used for condensing.
	 *
	 * @param info The ItemInfo we want to trim to the data that will be used for persistence.
	 *
	 * @return An {@link ItemInfo} for the same item as the input info, but with a potentially reduced {@link net.minecraft.core.component.DataComponentPatch}, containing
	 * whatever data is persistent/matters.
	 */
	@NotNull
	default ItemInfo getPersistentInfo(@NotNull ItemInfo info) {
		return IComponentProcessorHelper.INSTANCE.getPersistentInfo(info);
	}
}