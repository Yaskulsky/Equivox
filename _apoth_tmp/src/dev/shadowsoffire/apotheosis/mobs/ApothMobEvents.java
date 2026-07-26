package dev.shadowsoffire.apotheosis.mobs;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apoth.DataMaps;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.AugmentRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Augmentation;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ApothMobEvents {
   public static final String APOTH_MINIBOSS = "apoth.miniboss";
   public static final String APOTH_MINIBOSS_PLAYER = "apoth.miniboss.player";
   private static final Marker MARKER = MarkerFactory.getMarker(ApothMobEvents.class.getSimpleName());

   @SubscribeEvent(priority = EventPriority.LOW)
   public void finalizeMobSpawns(FinalizeSpawnEvent e) {
      debugLog("Finalizing spawn for: {}", e.getEntity().getName().getString());
      if (!e.isCanceled() && !e.isSpawnCancelled()) {
         Player player = e.getLevel().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1.0, false);
         if (player == null) {
            debugLog("Discarding due to lack of player context.");
         } else {
            Mob mob = e.getEntity();
            RandomSource rand = e.getLevel().getRandom();
            GenContext ctx = GenContext.forPlayerAtPos(rand, player, mob.blockPosition());
            if (this.trySpawnInvader(e, mob, ctx, player)) {
               debugLog("Successfully spawned an invader. Skipping Augmentations and Elites.");
            } else {
               this.tryAugmentations(e.getLevel(), mob, e.getSpawnType(), ctx);
               if (!this.trySpawnElite(e, mob, ctx, player)) {
                  ;
               }
            }
         }
      } else {
         debugLog("Discarding due to cancellation.");
      }
   }

   private boolean trySpawnInvader(FinalizeSpawnEvent e, Mob mob, GenContext ctx, Player player) {
      if ((e.getSpawnType() == EntitySpawnReason.NATURAL || e.getSpawnType() == EntitySpawnReason.CHUNK_GENERATION) && mob instanceof Monster) {
         ServerLevelAccessor sLevel = e.getLevel();
         long gameTime = sLevel.getGameTime();
         if ((Long)player.getData(Attachments.INVADER_COOLDOWN) > gameTime) {
            debugLog("[Invaders]: Spawn cooldown is active for the context player {}.", player.getName().getString());
            return false;
         } else {
            ResourceKey<DimensionType> dimId = sLevel.getLevel().dimensionTypeRegistration().getKey();
            InvaderSpawnRules rules = (InvaderSpawnRules)sLevel.registryAccess()
               .lookupOrThrow(Registries.DIMENSION_TYPE)
               .getData(DataMaps.INVADER_SPAWN_RULES, dimId);
            if (rules == null) {
               debugLog("[Invaders]: No invader spawn rules present for dimension {}", dimId);
               return false;
            } else {
               float chance = (Float)rules.spawnChances().get(ctx.tier());
               SurfaceType surface = rules.surfaceType();
               if (ctx.rand().nextFloat() > chance) {
                  debugLog("[Invaders]: Failed random chance roll.");
                  return false;
               } else {
                  if (surface.test(sLevel, BlockPos.containing(e.getX(), e.getY(), e.getZ()))) {
                     debugLog("[Invaders]: Succeeded at random chance roll and surface test.");
                     Invader item = InvaderRegistry.INSTANCE.getRandomItem(ctx);
                     if (item == null) {
                        Apotheosis.LOGGER
                           .error(
                              "Attempted to spawn an Invader in dimension {} using configured spawn rules {} but no bosses were made available.", dimId, rules
                           );
                        return false;
                     }

                     if (!item.basicData().canSpawn(mob, sLevel, e.getSpawnType())) {
                        debugLog("[Invaders]: Failed invader spawn conditions.");
                        return false;
                     }

                     Mob boss = item.createBoss(sLevel, BlockPos.containing(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), ctx);
                     if (AdventureConfig.bossAutoAggro && !player.isCreative()) {
                        boss.setTarget(player);
                     }

                     if (canSpawn(sLevel, boss, player.distanceToSqr(boss))) {
                        sLevel.addFreshEntityWithPassengers(boss);
                        e.setCanceled(true);
                        e.setSpawnCancelled(true);
                        sendInvaderSpawnNotification(sLevel.getLevel(), boss);
                        long end = gameTime + rules.cooldown().orElse(AdventureConfig.bossSpawnCooldown).intValue();
                        applyClusteredCooldown(sLevel.getLevel(), player, ctx.tier(), boss, end);
                        debugLog("[Invaders]: Successfully spawned an invader {} at {}", boss.getName().getString(), boss.blockPosition());
                        return true;
                     }

                     debugLog("Failed entity spawn checks.");
                  } else {
                     debugLog("[Invaders]: Failed surface test " + surface);
                  }

                  return false;
               }
            }
         }
      } else {
         debugLog("[Invaders]: Failed invader preconditions.");
         return false;
      }
   }

   public static void sendInvaderSpawnNotification(ServerLevel sLevel, Mob invader) {
      Component name = getName(invader);
      DynamicHolder<LootRarity> rarity = getRarity(invader);
      if (name != null && rarity.isBound()) {
         sLevel.players()
            .forEach(
               p -> {
                  if (isWithinAnnounceRange(p, invader)) {
                     p.connection
                        .send(
                           new ClientboundSetActionBarTextPacket(
                              Component.translatable("info.apotheosis.boss_spawn", new Object[]{name, (int)invader.getX(), (int)invader.getY()})
                           )
                        );
                     PacketDistributor.sendToPlayer(p, new BossSpawnPayload(invader.blockPosition(), rarity), new CustomPacketPayload[0]);
                  }
               }
            );
      } else {
         Apotheosis.LOGGER
            .warn(
               "An Invader {} ({}) has spawned without a name ({}) or rarity ({})!",
               new Object[]{invader.getName().getString(), EntityType.getKey(invader.getType()), name, rarity}
            );
      }
   }

   private static void applyClusteredCooldown(ServerLevel level, Player trigger, WorldTier tier, Mob boss, long end) {
      applyCooldown(trigger, end);
      int clustered = 0;

      for (ServerPlayer p : level.players()) {
         if (p != trigger && WorldTier.getTier(p) == tier && isWithinAnnounceRange(p, boss)) {
            applyCooldown(p, end);
            clustered++;
         }
      }

      debugLog("[Invaders]: Applied spawn cooldown ending at {} to {} and {} clustered player(s).", end, trigger.getName().getString(), clustered);
   }

   private static void applyCooldown(Player player, long end) {
      if (end > (Long)player.getData(Attachments.INVADER_COOLDOWN)) {
         player.setData(Attachments.INVADER_COOLDOWN, end);
      }
   }

   private static boolean isWithinAnnounceRange(Player player, Entity target) {
      Vec3 tPos = new Vec3(target.getX(), player.getY(), target.getZ());
      return player.distanceToSqr(tPos) <= AdventureConfig.bossAnnounceRange * AdventureConfig.bossAnnounceRange;
   }

   private void tryAugmentations(ServerLevelAccessor level, Mob mob, EntitySpawnReason type, GenContext ctx) {
      float healthPct = mob.getHealth() / mob.getMaxHealth();

      for (TierAugment aug : TierAugmentRegistry.getAugments(ctx.tier(), Target.MONSTERS)) {
         aug.apply(level, mob);
      }

      mob.setData(Attachments.TIER_AUGMENTS_APPLIED, true);

      for (Augmentation aug : AugmentRegistry.getAll()) {
         if (aug.canApply(level, mob, type, ctx)) {
            if (ctx.rand().nextFloat() <= aug.chance()) {
               debugLog("Applying augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
               aug.apply(mob, ctx);
            } else {
               debugLog("Roll failed for augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
            }
         } else {
            debugLog("Skipped augmentation {}", AugmentRegistry.INSTANCE.getKey(aug));
         }
      }

      mob.setHealth(healthPct * mob.getMaxHealth());
   }

   private boolean trySpawnElite(FinalizeSpawnEvent e, Mob mob, GenContext ctx, Player player) {
      ServerLevelAccessor sLevel = e.getLevel();
      Elite item = EliteRegistry.INSTANCE.getRandomItem(ctx, mob);
      if (item == null) {
         debugLog("No Elites were available for {} and {}", ctx, mob);
         return false;
      } else if (!item.basicData().canSpawn(mob, sLevel, e.getSpawnType())) {
         debugLog("The elite {} was selected but could not spawn based on spawn conditions.", EliteRegistry.INSTANCE.getKey(item));
         return false;
      } else if (ctx.rand().nextFloat() <= item.getChance()) {
         mob.getPersistentData().putString("apoth.miniboss", EliteRegistry.INSTANCE.getKey(item).toString());
         mob.getPersistentData().putString("apoth.miniboss.player", player.getUUID().toString());
         if (!item.basicData().finalizeSpawn()) {
            e.setCanceled(true);
         }

         debugLog("Successfully spawned the elite {} at {}", EliteRegistry.INSTANCE.getKey(item), mob.blockPosition());
         return true;
      } else {
         return false;
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void delayedEliteMobs(EntityJoinLevelEvent e) {
      if (!e.getLevel().isClientSide() && e.getEntity() instanceof Mob mob) {
         CompoundTag data = mob.getPersistentData();
         if (data.contains("apoth.miniboss") && data.contains("apoth.miniboss.player")) {
            String key = data.getString("apoth.miniboss").orElse("");

            try {
               UUID playerId = UUID.fromString((String)data.getString("apoth.miniboss.player").orElseThrow());
               Player player = e.getLevel().getPlayerByUUID(playerId);
               if (player == null) {
                  player = e.getLevel().getNearestPlayer(mob, -1.0);
               }

               if (player != null) {
                  GenContext ctx = GenContext.forPlayerAtPos(e.getLevel().getRandom(), player, mob.blockPosition());
                  Elite item = (Elite)EliteRegistry.INSTANCE.getValue(Identifier.tryParse(key));
                  if (item != null) {
                     item.transformMiniboss((ServerLevel)e.getLevel(), mob, ctx);
                  }
               }
            } catch (Exception var9) {
               Apotheosis.LOGGER.error("Failure while initializing the Apothic Elite " + key, var9);
            }
         }
      }
   }

   private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
      return playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance()
            && entity.removeWhenFarAway(playerDist)
         ? false
         : entity.checkSpawnRules(world, EntitySpawnReason.NATURAL) && entity.checkSpawnObstruction(world);
   }

   @Nullable
   private static Component getName(Mob boss) {
      return boss.getSelfAndPassengers()
         .filter(e -> e.getPersistentData().contains("apoth.boss"))
         .findFirst()
         .<Component>map(Entity::getCustomName)
         .orElse(null);
   }

   @Nullable
   private static DynamicHolder<LootRarity> getRarity(Mob boss) {
      return boss.getSelfAndPassengers()
         .filter(e -> e.getPersistentData().contains("apoth.boss"))
         .findFirst()
         .map(ApothMobEvents::getRarityHolder)
         .orElse(RarityRegistry.INSTANCE.emptyHolder());
   }

   private static DynamicHolder<LootRarity> getRarityHolder(Entity entity) {
      Identifier id = Identifier.tryParse(entity.getPersistentData().getString("apoth.boss.rarity").orElse(""));
      return RarityRegistry.INSTANCE.holder(id);
   }

   private static void debugLog(String msg, Object... args) {
      if (Apotheosis.DEBUG_MOBS) {
         Apotheosis.LOGGER.debug(MARKER, msg, args);
      }
   }
}
