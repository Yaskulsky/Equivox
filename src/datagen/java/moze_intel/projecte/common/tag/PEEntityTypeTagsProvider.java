package moze_intel.projecte.common.tag;

import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class PEEntityTypeTagsProvider extends EntityTypeTagsProvider {

	public PEEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, PECore.MODID);
	}

	@Override
	protected void addTags(@NotNull HolderLookup.Provider provider) {
		//Note: Intentionally does not include Axolotls, Allays, or Sniffers
		tag(PETags.Entities.RANDOMIZER_PEACEFUL).add(
				EntityType.ARMADILLO,
				EntityType.BAT,
				EntityType.BEE,
				EntityType.CAMEL,
				EntityType.CAT,
				EntityType.CHICKEN,
				EntityType.COD,
				EntityType.COW,
				EntityType.DOLPHIN,
				EntityType.DONKEY,
				EntityType.FOX,
				EntityType.FROG,
				EntityType.GLOW_SQUID,
				EntityType.GOAT,
				EntityType.HORSE,
				EntityType.LLAMA,
				EntityType.MOOSHROOM,
				EntityType.MULE,
				EntityType.OCELOT,
				EntityType.PANDA,
				EntityType.PARROT,
				EntityType.PIG,
				EntityType.POLAR_BEAR,
				EntityType.PUFFERFISH,
				EntityType.RABBIT,
				EntityType.SALMON,
				EntityType.SHEEP,
				EntityType.SQUID,
				EntityType.STRIDER,
				EntityType.TADPOLE,
				EntityType.TRADER_LLAMA,
				EntityType.TROPICAL_FISH,
				EntityType.TURTLE,
				EntityType.VILLAGER,
				EntityType.WANDERING_TRADER,
				EntityType.WOLF
		);
		tag(PETags.Entities.RANDOMIZER_HOSTILE).add(
				EntityType.BLAZE,
				EntityType.BOGGED,
				EntityType.BREEZE,
				EntityType.CREEPER,
				EntityType.DROWNED,
				EntityType.ENDERMAN,
				EntityType.ENDERMITE,
				EntityType.EVOKER,
				EntityType.GHAST,
				EntityType.GUARDIAN,
				EntityType.HOGLIN,
				EntityType.HUSK,
				EntityType.PHANTOM,
				EntityType.PIGLIN,
				EntityType.PIGLIN_BRUTE,
				EntityType.PILLAGER,
				EntityType.RABBIT,
				EntityType.SHULKER,
				EntityType.SILVERFISH,
				EntityType.SKELETON,
				EntityType.SKELETON_HORSE,
				EntityType.SLIME,
				EntityType.SPIDER,
				EntityType.STRAY,
				EntityType.VEX,
				EntityType.VINDICATOR,
				EntityType.WITCH,
				EntityType.WITHER_SKELETON,
				EntityType.ZOGLIN,
				EntityType.ZOMBIE,
				EntityType.ZOMBIE_HORSE,
				EntityType.ZOMBIE_VILLAGER,
				EntityType.ZOMBIFIED_PIGLIN
		);
		tag(PETags.Entities.BLACKLIST_SWRG);
		tag(PETags.Entities.BLACKLIST_INTERDICTION);
		//Vanilla tags
		tag(EntityTypeTags.ARROWS).add(PEEntityTypes.HOMING_ARROW.get());
		tag(EntityTypeTags.IMPACT_PROJECTILES).add(
				PEEntityTypes.FIRE_PROJECTILE.get(),
				PEEntityTypes.LAVA_PROJECTILE.get(),
				PEEntityTypes.LENS_PROJECTILE.get(),
				PEEntityTypes.SWRG_PROJECTILE.get(),
				PEEntityTypes.WATER_PROJECTILE.get()
		);
	}
}
