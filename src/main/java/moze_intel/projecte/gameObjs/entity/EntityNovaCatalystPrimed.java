package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.blocks.ProjectETNT;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EntityNovaCatalystPrimed extends EntityNovaPrimed {

	public EntityNovaCatalystPrimed(EntityType<EntityNovaCatalystPrimed> type, Level level) {
		super(type, level);
	}

	public EntityNovaCatalystPrimed(Level level, double x, double y, double z, LivingEntity placer) {
		super(level, x, y, z, placer);
	}

	@Override
	protected BlockRegistryObject<ProjectETNT, ?> getBlock() {
		return PEBlocks.NOVA_CATALYST;
	}

	@NotNull
	@Override
	public EntityType<?> getType() {
		return PEEntityTypes.NOVA_CATALYST_PRIMED.get();
	}
}