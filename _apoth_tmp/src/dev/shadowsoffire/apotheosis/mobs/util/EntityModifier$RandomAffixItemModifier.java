package dev.shadowsoffire.apotheosis.mobs.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.loot.AffixLootEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apothic_attributes.modifiers.EquipmentSlotCompat;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import java.util.Arrays;
import java.util.Set;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public record EntityModifier$RandomAffixItemModifier(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries)
   implements EntityModifier {
   public static Codec<EntityModifier$RandomAffixItemModifier> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(
            PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
            PlaceboCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(a -> a.entries)
         )
         .apply(inst, EntityModifier$RandomAffixItemModifier::new)
   );

   public EntityModifier$RandomAffixItemModifier() {
      this(Set.of(), Set.of());
   }

   public Codec<? extends EntityModifier> getCodec() {
      return CODEC;
   }

   public void apply(Mob mob, GenContext ctx) {
      ItemStack stack = LootController.createAffixItemFromPools(this.rarities, this.entries, ctx);
      if (!stack.isEmpty()) {
         NameHelper.setItemName(mob.getRandom(), stack);
         stack.set(Components.FROM_MOB, true);
         LootCategory cat = LootCategory.forItem(stack);
         EquipmentSlot slot = Arrays.stream(EquipmentSlot.values())
            .filter(eSlot -> cat.getSlots().test(EquipmentSlotCompat.fromVanilla(eSlot)))
            .findAny()
            .orElse(EquipmentSlot.MAINHAND);
         mob.setItemSlot(slot, stack);
         mob.setGuaranteedDrop(slot);
      }
   }
}
