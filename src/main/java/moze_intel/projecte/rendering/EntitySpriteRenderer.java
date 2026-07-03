package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * TODO 26.1: Port sprite entity rendering to extractRenderState/submit API.
 */
public class EntitySpriteRenderer<ENTITY extends Entity> extends EntityRenderer<ENTITY, EntityRenderState> {

	private final Identifier texture;

	public EntitySpriteRenderer(EntityRendererProvider.Context context, Identifier texture) {
		super(context);
		this.texture = texture;
	}

	@Override
	public @NotNull EntityRenderState createRenderState() {
		return new EntityRenderState();
	}

	@Override
	public void submit(@NotNull EntityRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState camera) {
	}

	@NotNull
	public Identifier getTextureLocation(@NotNull ENTITY entity) {
		return texture;
	}
}
