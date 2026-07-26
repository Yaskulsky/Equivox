package dev.shadowsoffire.apotheosis.tiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apoth.Advancements;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apoth.Stats;
import dev.shadowsoffire.apotheosis.net.WorldTierPayload;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public enum WorldTier implements StringRepresentable {
   HAVEN("haven"),
   FRONTIER("frontier"),
   ASCENT("ascent"),
   SUMMIT("summit"),
   PINNACLE("pinnacle");

   public static final IntFunction<WorldTier> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO);
   public static final Codec<WorldTier> CODEC = StringRepresentable.fromValues(WorldTier::values);
   public static final StreamCodec<ByteBuf, WorldTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
   private String name;

   private WorldTier(String name) {
      this.name = name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public MutableComponent toComponent() {
      return Apotheosis.lang("text", "world_tier." + this.getSerializedName(), new Object[0]);
   }

   public Identifier getUnlockAdvancement() {
      return switch (this) {
         case HAVEN -> Advancements.WORLD_TIER_HAVEN;
         case FRONTIER -> Advancements.WORLD_TIER_FRONTIER;
         case ASCENT -> Advancements.WORLD_TIER_ASCENT;
         case SUMMIT -> Advancements.WORLD_TIER_SUMMIT;
         case PINNACLE -> Advancements.WORLD_TIER_PINNACLE;
      };
   }

   public static WorldTier getTier(Player player) {
      if (player instanceof FakePlayer fp) {
         MinecraftServer server = fp.level().getServer();
         ServerPlayer realPlayer = server.getPlayerList().getPlayer(fp.getUUID());
         if (realPlayer != null) {
            WorldTier realTier = getTier(realPlayer);
            fp.setData(Attachments.WORLD_TIER, realTier);
            return realTier;
         }
      }

      return (WorldTier)player.getData(Attachments.WORLD_TIER);
   }

   public static void setTier(Player player, WorldTier tier) {
      WorldTier oldTier = (WorldTier)player.getData(Attachments.WORLD_TIER);
      if (oldTier != tier || isTutorialActive(player)) {
         player.setData(Attachments.WORLD_TIER, tier);
         if (player instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new WorldTierPayload(tier), new CustomPacketPayload[0]);

            for (TierAugment aug : TierAugmentRegistry.getAugments(oldTier, Target.PLAYERS)) {
               aug.remove(sp.level(), player);
            }

            for (TierAugment aug : TierAugmentRegistry.getAugments(tier, Target.PLAYERS)) {
               aug.apply(sp.level(), player);
            }

            player.setData(Attachments.TIER_AUGMENTS_APPLIED, true);
            player.awardStat(Stats.WORLD_TIERS_ACTIVATED);
         }
      }
   }

   public static boolean isUnlocked(Player player, WorldTier tier) {
      return ApothMiscUtil.hasAdvancement(player, tier.getUnlockAdvancement());
   }

   public static boolean isTutorialActive(Player player) {
      return FMLEnvironment.getDist().isClient() && player.level().isClientSide()
         ? WorldTier.ClientAccess.isTutorialActive(player)
         : getTier(player) == HAVEN && ((ServerPlayer)player).getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(Stats.WORLD_TIERS_ACTIVATED)) == 0;
   }

   public static <T> MapCodec<Map<WorldTier, T>> mapCodec(Codec<T> elementCodec) {
      return Codec.simpleMap(CODEC, elementCodec, Keyable.forStrings(() -> Arrays.stream(values()).map(StringRepresentable::getSerializedName)));
   }

   private static class ClientAccess {
      private static boolean isTutorialActive(Player player) {
         return WorldTier.getTier(player) == WorldTier.HAVEN
            && Minecraft.getInstance().player.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(Stats.WORLD_TIERS_ACTIVATED)) == 0;
      }
   }
}
