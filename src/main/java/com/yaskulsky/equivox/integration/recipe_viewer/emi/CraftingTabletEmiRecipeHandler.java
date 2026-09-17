package com.yaskulsky.equivox.integration.recipe_viewer.emi;

import java.util.ArrayList;
import java.util.List;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.gameObjs.container.IArcaneCraftingMenu;
import com.yaskulsky.equivox.network.packets.to_server.ArcaneTabletRecipeTransferPKT;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import dev.emi.emi.api.recipe.EmiPlayerInventory;

/**
 * EMI + fill into Equivox 3x3 (inventory first, then EMC / knowledge).
 * Compiled when an EMI 26.1 API is on the classpath; ATM11 currently ships JEI.
 */
public class CraftingTabletEmiRecipeHandler<C extends AbstractContainerMenu & IArcaneCraftingMenu> implements EmiRecipeHandler<C> {

	@Override
	public EmiPlayerInventory getInventory(AbstractContainerScreen<C> screen) {
		return new EmiPlayerInventory(screen.getMenu().getCraftingPlayer());
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<C> context) {
		return supportsRecipe(recipe);
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<C> context) {
		List<List<ItemStack>> stacks = new ArrayList<>();
		List<ItemStack> empty = List.of(ItemStack.EMPTY);
		List<EmiIngredient> inputs = recipe.getInputs();
		int max = Math.min(9, inputs.size());
		for (int i = 0; i < 9; i++) {
			if (i >= max) {
				stacks.add(empty);
				continue;
			}
			List<ItemStack> options = new ArrayList<>();
			for (EmiStack emiStack : inputs.get(i).getEmiStacks()) {
				ItemStack item = emiStack.getItemStack();
				if (!item.isEmpty()) {
					options.add(item);
				}
			}
			if (options.isEmpty()) {
				stacks.add(empty);
			} else {
				options.sort((a, b) -> Long.compare(IEMCProxy.INSTANCE.getValue(a), IEMCProxy.INSTANCE.getValue(b)));
				stacks.add(options);
			}
		}
		boolean maxTransfer = context.getAmount() > 1;
		ClientPacketDistributor.sendToServer(new ArcaneTabletRecipeTransferPKT(stacks, maxTransfer));
		return true;
	}
}
