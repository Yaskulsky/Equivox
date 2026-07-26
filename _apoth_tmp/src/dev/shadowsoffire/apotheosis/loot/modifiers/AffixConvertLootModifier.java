package dev.shadowsoffire.apotheosis.loot.modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Set;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class AffixConvertLootModifier extends ContextualLootModifier {
   public static final MapCodec<AffixConvertLootModifier> CODEC = RecordCodecBuilder.mapCodec(
      inst -> codecStart(inst)
         .and(AffixConvertLootModifier.AffixConversionEntry.CODEC.listOf().fieldOf("entries").forGetter(g -> g.entries))
         .apply(inst, AffixConvertLootModifier::new)
   );
   protected final List<AffixConvertLootModifier.AffixConversionEntry> entries;

   public AffixConvertLootModifier(LootItemCondition[] conditions, int priority, List<AffixConvertLootModifier.AffixConversionEntry> entries) {
      super(conditions, priority);
      this.entries = entries;
   }

   @Override
   protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context, GenContext gCtx) {
      for (AffixConvertLootModifier.AffixConversionEntry entry : this.entries) {
         if (entry.pattern.matches(context.getQueriedLootTableId())) {
            RandomSource rand = context.getRandom();
            if (!(entry.chance() <= 0.0F)) {
               ObjectListIterator var7 = generatedLoot.iterator();

               while (var7.hasNext()) {
                  ItemStack s = (ItemStack)var7.next();
                  if (!LootCategory.forItem(s).isNone() && AffixHelper.getAffixes(s).isEmpty() && rand.nextFloat() <= entry.chance()) {
                     LootRarity rarity = LootRarity.randomFromHolders(gCtx, entry.rarities);
                     LootController.createLootItem(s, rarity, gCtx);
                     NameHelper.setItemName(rand, s);
                  }
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

   public record AffixConversionEntry(LootPatternMatcher pattern, float chance, Set<DynamicHolder<LootRarity>> rarities) {
      public static final Codec<AffixConvertLootModifier.AffixConversionEntry> CODEC = RecordCodecBuilder.create(
         inst -> inst.group(
               LootPatternMatcher.CODEC.fieldOf("pattern").forGetter(AffixConvertLootModifier.AffixConversionEntry::pattern),
               Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(AffixConvertLootModifier.AffixConversionEntry::chance),
               PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec())
                  .optionalFieldOf("rarities", Set.of())
                  .forGetter(AffixConvertLootModifier.AffixConversionEntry::rarities)
            )
            .apply(inst, AffixConvertLootModifier.AffixConversionEntry::new)
      );
   }
}
