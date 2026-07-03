package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import moze_intel.projecte.gameObjs.block_entities.EmcChestBlockEntity;
import moze_intel.projecte.gameObjs.registration.impl.BlockRegistryObject;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

//Only used on the client
public class ChestRenderer implements BlockEntityRenderer<EmcChestBlockEntity, ChestRenderState> {

	public ChestRenderer(BlockEntityRendererProvider.Context context, Identifier texture, BlockRegistryObject<?, ?> type) {
	}

	@Override
	public @NotNull ChestRenderState createRenderState() {
		return new ChestRenderState();
	}

	@Override
	public void submit(@NotNull ChestRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState camera) {
		//TODO 26.1: Port EMC chest rendering to extractRenderState/submit API.
	}
}
