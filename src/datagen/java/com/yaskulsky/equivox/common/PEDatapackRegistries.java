package com.yaskulsky.equivox.common;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.registries.PEDamageTypes;
import com.yaskulsky.equivox.gameObjs.registries.PEDamageTypes.PEDamageType;
import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;

public final class PEDatapackRegistries {

	public static final RegistrySetBuilder RELOADABLE = new RegistrySetBuilder()
			.add(Registries.DAMAGE_TYPE, context -> {
				for (PEDamageType damageType : PEDamageTypes.DAMAGE_TYPES.values()) {
					context.register(damageType.key(), new DamageType(damageType.msgId(), damageType.exhaustion()));
				}
			});

	public static final Set<String> MOD_NAMESPACES = Set.of(PECore.MODID);

	private PEDatapackRegistries() {}
}
