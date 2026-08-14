package com.yaskulsky.equivox.integration.top;

import java.util.function.Function;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.api.proxy.IEMCProxy;
import com.yaskulsky.equivox.config.EquivoxConfig;
import com.yaskulsky.equivox.gameObjs.blocks.TransmutationProvider;
import com.yaskulsky.equivox.utils.EMCHelper;
import com.yaskulsky.equivox.utils.text.PELang;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

//Registered via IMC
@SuppressWarnings("unused")
public class PEProbeInfoProvider implements IProbeInfoProvider, Function<ITheOneProbe, Void> {

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
		if (blockState.getBlock() instanceof TransmutationProvider && blockState.hasProperty(TransmutationProvider.ONLINE)) {
			boolean online = blockState.getValue(TransmutationProvider.ONLINE);
			probeInfo.mcText((online
					? PELang.TRANSMUTATION_PROVIDER_ONLINE.translate()
					: PELang.TRANSMUTATION_PROVIDER_OFFLINE.translate()).withStyle(ChatFormatting.GRAY));
			return;
		}
		if (EquivoxConfig.server.misc.lookingAtDisplay.get()) {
			long value = IEMCProxy.INSTANCE.getValue(blockState.getBlock());
			if (value > 0) {
				probeInfo.mcText(EMCHelper.getEmcTextComponent(value, 1));
			}
		}
	}

	@Override
	public Identifier getID() {
		return PECore.rl("emc");
	}

	@Override
	public Void apply(ITheOneProbe iTheOneProbe) {
		iTheOneProbe.registerProvider(this);
		return null;
	}
}