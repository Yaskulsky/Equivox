package com.yaskulsky.equivox.integration.wthit;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.blocks.TransmutationProvider;
import com.yaskulsky.equivox.utils.EMCHelper;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.block.state.BlockState;

public class WTHITDataProvider implements IBlockComponentProvider {

	static final WTHITDataProvider INSTANCE = new WTHITDataProvider();

	@Override
	public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (state.getBlock() instanceof TransmutationProvider && state.hasProperty(TransmutationProvider.ONLINE)) {
			boolean online = state.getValue(TransmutationProvider.ONLINE);
			tooltip.addLine((online
					? PELang.TRANSMUTATION_PROVIDER_ONLINE.translate()
					: PELang.TRANSMUTATION_PROVIDER_OFFLINE.translate()).withStyle(ChatFormatting.GRAY));
			return;
		}
		if (EquivoxConfig.server.misc.lookingAtDisplay.get()) {
			long value = IEMCProxy.INSTANCE.getValue(accessor.getBlock());
			if (value > 0) {
				tooltip.addLine(EMCHelper.getEmcTextComponent(value, 1));
			}
		}
	}
}
