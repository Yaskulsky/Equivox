package dev.shadowsoffire.apotheosis.loot.modifiers;

import dev.shadowsoffire.apotheosis.tiers.GenContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

public abstract class ContextualLootModifier extends LootModifier {
   protected ContextualLootModifier(LootItemCondition[] conditions, int priority) {
      super(conditions, priority);
   }

   protected final ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
      GenContext gCtx = GenContext.forLoot(ctx);
      return gCtx != null ? this.doApply(loot, ctx, gCtx) : loot;
   }

   protected abstract ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> var1, LootContext var2, GenContext var3);
}
