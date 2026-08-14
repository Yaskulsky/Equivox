package com.yaskulsky.equivox.integration.jade;

import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.blocks.TransmutationProvider;
import com.yaskulsky.equivox.utils.EMCHelper;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class JadeDataProvider implements IBlockComponentProvider {

	static final JadeDataProvider INSTANCE = new JadeDataProvider();

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (state.getBlock() instanceof TransmutationProvider && state.hasProperty(TransmutationProvider.ONLINE)) {
			// AE2 GridNodeStateDataProvider: plain gray line in the same Jade body
			boolean online = state.getValue(TransmutationProvider.ONLINE);
			tooltip.add((online
					? PELang.TRANSMUTATION_PROVIDER_ONLINE.translate()
					: PELang.TRANSMUTATION_PROVIDER_OFFLINE.translate()).withStyle(ChatFormatting.GRAY));
			return;
		}
		if (EquivoxConfig.server.misc.lookingAtDisplay.get()) {
			long value = IEMCProxy.INSTANCE.getValue(accessor.getBlock());
			if (value > 0) {
				tooltip.add(EMCHelper.getEmcTextComponent(value, 1));
			}
		}
	}

	@Override
	public Identifier getUid() {
		return PEJadeConstants.EMC_PROVIDER;
	}
}
