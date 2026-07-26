package dev.shadowsoffire.apotheosis.loot.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import dev.shadowsoffire.apotheosis.util.NameHelper;
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

public class AffixLootModifier extends ContextualLootModifier {
   public static final MapCodec<AffixLootModifier> CODEC = RecordCodecBuilder.mapCodec(
      inst -> codecStart(inst)
         .and(AffixLootModifier.AffixTableEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries))
         .apply(inst, AffixLootModifier::new)
   );
   protected final List<AffixLootModifier.AffixTableEntry> entries;

   public AffixLootModifier(LootItemCondition[] conditions, int priority, List<AffixLootModifier.AffixTableEntry> entries) {
      super(conditions, priority);
      this.entries = entries;
   }

   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx, GenContext gCtx) {
      for (AffixLootModifier.AffixTableEntry entry : this.entries) {
         if (entry.pattern.matches(ctx.getQueriedLootTableId())) {
            if (ctx.getRandom().nextFloat() <= entry.chance()) {
               AffixLootEntry lootEntry;
               if (!entry.entries.isEmpty()) {
                  List<Weighted<AffixLootEntry>> resolved = entry.entries
                     .stream()
                     .<Weighted<AffixLootEntry>>mapMulti(TieredWeights.wrapFilterHolders(gCtx))
                     .toList();
                  lootEntry = (AffixLootEntry)((Weighted)WeightedRandom.getRandomItem(ctx.getRandom(), resolved, Weighted::weight).get()).value();
               } else {
                  lootEntry = AffixLootRegistry.INSTANCE.getRandomItem(gCtx);
               }

               LootRarity rarity;
               if (!entry.rarities.isEmpty()) {
                  rarity = LootRarity.randomFromHolders(gCtx, entry.rarities);
               } else {
                  rarity = LootRarity.random(gCtx, lootEntry.rarities());
               }

               ItemStack affixItem = LootController.createLootItem(lootEntry.stack(), rarity, gCtx);
               if (!affixItem.isEmpty()) {
                  NameHelper.setItemName(ctx.getRandom(), affixItem);
                  affixItem.set(Components.FROM_CHEST, true);
                  generatedLoot.add(affixItem);
               }
            }
            break;
         }
      }

      return generatedLoot;
   }

   public MapCodec<? extends IGlobalLootModifier> codec() {
      return CODEC;
   }

   @Nullable
   private AffixLootEntry unwrap(DynamicHolder<AffixLootEntry> holder) {
      if (!holder.isBound()) {
         Apotheosis.LOGGER.error("An AffixLootModifier failed to resolve the AffixLootEntry {}!", holder.getId());
         return null;
      } else {
         return (AffixLootEntry)holder.get();
      }
   }

   public record AffixTableEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<AffixLootEntry>> entries, Set<DynamicHolder<LootRarity>> rarities) {
      public static final Codec<AffixLootModifier.AffixTableEntry> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(AffixLootModifier.AffixTableEntry::pattern),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(AffixLootModifier.AffixTableEntry::chance),
               PlaceboCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec())
                  .optionalFieldOf("entries", Set.of())
                  .forGetter(AffixLootModifier.AffixTableEntry::entries),
               PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec())
                  .optionalFieldOf("rarities", Set.of())
                  .forGetter(AffixLootModifier.AffixTableEntry::rarities)
            )
            .apply(inst, AffixLootModifier.AffixTableEntry::new)
      );
   }
}
