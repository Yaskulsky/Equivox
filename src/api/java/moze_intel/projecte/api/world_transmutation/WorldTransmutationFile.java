package moze_intel.projecte.api.world_transmutation;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import moze_intel.projecte.api.codec.IPECodecHelper;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.jetbrains.annotations.Nullable;

/**
 * @param comment        Optional comment describing the file.
 * @param transmutations List of world transmutations.
 */
public record WorldTransmutationFile(@Nullable String comment, List<IWorldTransmutation> transmutations) {

	private static final Codec<IWorldTransmutation> TRANSMUTATION_CODEC = Codec.either(
			SimpleWorldTransmutation.CODEC,
			WorldTransmutation.CODEC
	).flatComapMap(Either::unwrap, transmutation -> DataResult.success(switch (transmutation) {
		case SimpleWorldTransmutation simple -> Either.left(simple);
		case WorldTransmutation worldTransmutation -> Either.right(worldTransmutation);
	}));

	private static final Codec<List<IWorldTransmutation>> LIST_CODEC = TRANSMUTATION_CODEC.listOf();
	/**
	 * Codec for serializing and deserializing World Transmutation Files.
	 */
	public static final Codec<WorldTransmutationFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.NON_EMPTY_STRING.optionalFieldOf("comment").forGetter(file -> Optional.ofNullable(file.comment())),
			LIST_CODEC.optionalFieldOf("transmutations").forGetter(file -> IPECodecHelper.INSTANCE.ifNotEmpty(file.transmutations()))
	).apply(instance, (comment, transmutations) ->
			new WorldTransmutationFile(comment.orElse(null), transmutations.orElseGet(Collections::emptyList))));
	/**
	 * Codec for serializing and deserializing World Transmutation Files that contain conditions that are checked before loading.
	 */
	public static final Codec<Optional<WithConditions<WorldTransmutationFile>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);
}