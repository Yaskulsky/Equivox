package moze_intel.projecte.gameObjs.registries;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.entity.EntityHomingArrow;
import moze_intel.projecte.gameObjs.entity.EntityLavaProjectile;
import moze_intel.projecte.gameObjs.entity.EntityLensProjectile;
import moze_intel.projecte.gameObjs.entity.EntityMobRandomizer;
import moze_intel.projecte.gameObjs.entity.EntityNovaCataclysmPrimed;
import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.entity.EntityWaterProjectile;
import moze_intel.projecte.gameObjs.registration.impl.EntityTypeDeferredRegister;
import moze_intel.projecte.gameObjs.registration.impl.EntityTypeRegistryObject;
import net.minecraft.SharedConstants;

public class PEEntityTypes {

	public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(PECore.MODID);

	public static final EntityTypeRegistryObject<EntityFireProjectile> FIRE_PROJECTILE = ENTITY_TYPES.registerNoGravThrowable("fire_projectile", EntityFireProjectile::new);
	public static final EntityTypeRegistryObject<EntityHomingArrow> HOMING_ARROW = ENTITY_TYPES.registerMisc("homing_arrow", EntityHomingArrow::new, builder -> builder
			//[VanillaCopy] from EntityType.ARROW
			.sized(0.5F, 0.5F)
			.eyeHeight(0.13F)
			.clientTrackingRange(4)
			.updateInterval(SharedConstants.TICKS_PER_SECOND)
	);
	public static final EntityTypeRegistryObject<EntityLavaProjectile> LAVA_PROJECTILE = ENTITY_TYPES.registerNoGravThrowable("lava_projectile", EntityLavaProjectile::new);
	public static final EntityTypeRegistryObject<EntityLensProjectile> LENS_PROJECTILE = ENTITY_TYPES.registerNoGravThrowable("lens_projectile", EntityLensProjectile::new);
	public static final EntityTypeRegistryObject<EntityMobRandomizer> MOB_RANDOMIZER = ENTITY_TYPES.registerNoGravThrowable("mob_randomizer", EntityMobRandomizer::new);
	public static final EntityTypeRegistryObject<EntityNovaCatalystPrimed> NOVA_CATALYST_PRIMED = ENTITY_TYPES.registerTnt("nova_catalyst_primed", EntityNovaCatalystPrimed::new);
	public static final EntityTypeRegistryObject<EntityNovaCataclysmPrimed> NOVA_CATACLYSM_PRIMED = ENTITY_TYPES.registerTnt("nova_cataclysm_primed", EntityNovaCataclysmPrimed::new);
	public static final EntityTypeRegistryObject<EntitySWRGProjectile> SWRG_PROJECTILE = ENTITY_TYPES.registerNoGravThrowable("swrg_projectile", EntitySWRGProjectile::new);
	public static final EntityTypeRegistryObject<EntityWaterProjectile> WATER_PROJECTILE = ENTITY_TYPES.registerNoGravThrowable("water_projectile", EntityWaterProjectile::new);
}