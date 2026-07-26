package dev.shadowsoffire.apotheosis.loot.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import org.jetbrains.annotations.Nullable;

public class GemLootModifier extends ContextualLootModifier {
   public static final MapCodec<GemLootModifier> CODEC = RecordCodecBuilder.mapCodec(
      inst -> codecStart(inst).and(GemLootModifier.GemTableEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries)).apply(inst, GemLootModifier::new)
   );
   protected final List<GemLootModifier.GemTableEntry> entries;

   public GemLootModifier(LootItemCondition[] conditions, int priority, List<GemLootModifier.GemTableEntry> entries) {
      super(conditions, priority);
      this.entries = entries;
   }

   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx, GenContext gCtx) {
      for (GemLootModifier.GemTableEntry entry : this.entries) {
         if (entry.pattern.matches(ctx.getQueriedLootTableId())) {
            if (!(ctx.getRandom().nextFloat() <= entry.chance())) {
               break;
            }

            Purity purity = Purity.random(gCtx, entry.purities);
            Gem gem;
            if (!entry.gems.isEmpty()) {
               List<Weighted<Gem>> resolved = entry.gems.stream().<Weighted<Gem>>mapMulti(TieredWeights.wrapFilterHolders(gCtx)).toList();
               gem = (Gem)((Weighted)WeightedRandom.getRandomItem(ctx.getRandom(), resolved, Weighted::weight).get()).value();
            } else {
               gem = GemRegistry.INSTANCE.getRandomItem(gCtx);
            }

            if (gem != null) {
               generatedLoot.add(gem.toStack(purity));
               break;
            }

            Apotheosis.LOGGER.error("A GemLootModifier (entry {}) failed to resolve a gem for table {}!", entry.toString(), ctx.getQueriedLootTableId());
         }
      }

      return generatedLoot;
   }

   public MapCodec<? extends IGlobalLootModifier> codec() {
      return CODEC;
   }

   @Nullable
   private Gem unwrap(DynamicHolder<Gem> holder) {
      if (!holder.isBound()) {
         Apotheosis.LOGGER.error("A GemLootModifier failed to resolve the Gem {}!", holder.getId());
         return null;
      } else {
         return (Gem)holder.get();
      }
   }

   public record GemTableEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<Gem>> gems, Set<Purity> purities) {
      public static final Codec<GemLootModifier.GemTableEntry> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(GemLootModifier.GemTableEntry::pattern),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(GemLootModifier.GemTableEntry::chance),
               PlaceboCodecs.setOf(GemRegistry.INSTANCE.holderCodec()).optionalFieldOf("gems", Set.of()).forGetter(GemLootModifier.GemTableEntry::gems),
               PlaceboCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of()).forGetter(GemLootModifier.GemTableEntry::purities)
            )
            .apply(inst, GemLootModifier.GemTableEntry::new)
      );
   }
}
