package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.gameObjs.items.PhilosophersStone.PhilosophersStoneMode;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransmutationRenderingOverlay implements GuiLayer {

	private record OutlineTarget(BlockPos pos, VoxelShape shape) {
	}

	private final Minecraft mc = Minecraft.getInstance();
	@Nullable
	private Block transmutationResult;
	private long lastGameTime;
	@Nullable
	private List<OutlineTarget> outlineTargets = List.of();
	private float outlineAlpha;

	public TransmutationRenderingOverlay() {
		NeoForge.EVENT_BUS.addListener(this::onBlockOutlineExtract);
	}

	@Override
	public void render(@NotNull GuiGraphicsExtractor graphics, @NotNull DeltaTracker delta) {
		if (!mc.options.hideGui && transmutationResult != null) {
			graphics.item(new ItemStack(transmutationResult), 1, 1);
			long gameTime = mc.level == null ? 0 : mc.level.getGameTime();
			if (lastGameTime != gameTime) {
				//If the game time changed, so we aren't actually still hovering a block set our
				// result to null. We do this after rendering it just in case there is a single
				// frame where this may actually be valid based on the order the events are fired
				transmutationResult = null;
				lastGameTime = gameTime;
			}
		}
	}

	private void onBlockOutlineExtract(ExtractBlockOutlineRenderStateEvent event) {
		Camera camera = event.getCamera();
		Entity entity = camera.entity();
		if (!(entity instanceof Player player)) {
			clearOverlay();
			return;
		}
		Level level = event.getLevel();
		lastGameTime = level.getGameTime();
		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty()) {
			stack = player.getOffhandItem();
		}
		if (stack.isEmpty() || !stack.is(PEItems.PHILOSOPHERS_STONE)) {
			clearOverlay();
			return;
		}
		boolean isSneaking = player.isSecondaryUseActive();
		PhilosophersStone philoStone = (PhilosophersStone) stack.getItem();
		//Note: We use the philo stone's ray trace instead of the event's ray trace as we want to make sure that we
		// can properly take fluid into account/ignore it when needed
		BlockHitResult rtr = philoStone.getHitBlock(level, player, isSneaking);
		if (rtr.getType() != HitResult.Type.BLOCK) {
			clearOverlay();
			return;
		}
		int charge = philoStone.getCharge(stack);
		PhilosophersStoneMode mode = philoStone.getMode(stack);
		Object2ReferenceMap<BlockPos, BlockState> changes = PhilosophersStone.getChanges(level, rtr.getBlockPos(), rtr.getDirection(), player.getDirection(),
				isSneaking, mode, charge);
		if (changes.isEmpty()) {
			clearOverlay();
			return;
		}
		transmutationResult = changes.values().iterator().next().getBlock();
		outlineAlpha = ProjectEConfig.client.pulsatingOverlay.get() ? getPulseProportion() * 0.60F : 0.35F;
		CollisionContext selectionContext = event.getCollisionContext();
		List<OutlineTarget> targets = new ArrayList<>(changes.size());
		for (BlockPos pos : changes.keySet()) {
			BlockState state = level.getBlockState(pos);
			if (!state.isAir()) {
				VoxelShape shape = state.getShape(level, pos, selectionContext);
				if (!shape.isEmpty()) {
					targets.add(new OutlineTarget(pos.immutable(), shape));
				}
			}
		}
		outlineTargets = List.copyOf(targets);
		if (!outlineTargets.isEmpty()) {
			event.addCustomRenderer(OUTLINE_RENDERER);
		}
	}

	private void clearOverlay() {
		transmutationResult = null;
		outlineTargets = List.of();
	}

	private final CustomBlockOutlineRenderer OUTLINE_RENDERER = this::renderTransmutationOutlines;

	private boolean renderTransmutationOutlines(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource buffer, PoseStack poseStack,
			boolean translucentPass, LevelRenderState levelRenderState) {
		List<OutlineTarget> targets = outlineTargets;
		if (targets.isEmpty()) {
			return false;
		}
		Vec3 viewPosition = levelRenderState.cameraRenderState.pos;
		VertexConsumer builder = buffer.getBuffer(PERenderType.TRANSMUTATION_OVERLAY);
		int color = ((int) (outlineAlpha * 255) << 24) | 0xFFFFFF;
		for (OutlineTarget target : targets) {
			BlockPos pos = target.pos;
			poseStack.pushPose();
			poseStack.translate(pos.getX() - viewPosition.x, pos.getY() - viewPosition.y, pos.getZ() - viewPosition.z);
			ShapeRenderer.renderShape(poseStack, builder, target.shape, 0, 0, 0, color, 2.0F);
			poseStack.popPose();
		}
		return false;
	}

	private float getPulseProportion() {
		return (float) (0.5F * Math.sin(System.currentTimeMillis() / 350.0) + 0.5F);
	}
}
