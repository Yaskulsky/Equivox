package dev.shadowsoffire.apotheosis.socket.gem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.color.GradientColor;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;

public enum Purity implements StringRepresentable, dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted {
   CRACKED("cracked", 8421504),
   CHIPPED("chipped", 3407667),
   FLAWED("flawed", 5592575),
   NORMAL("normal", 12255419),
   FLAWLESS("flawless", 15560724),
   PERFECT("perfect", GradientColor.RAINBOW);

   public static final IntFunction<Purity> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), OutOfBoundsStrategy.ZERO);
   public static final Codec<Purity> CODEC = StringRepresentable.fromValues(Purity::values);
   public static final StreamCodec<ByteBuf, Purity> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
   public static final Set<Purity> ALL_PURITIES = ApothMiscUtil.linkedSet(new Purity[]{CRACKED, CHIPPED, FLAWED, NORMAL, FLAWLESS, PERFECT});
   private final String name;
   private final TextColor color;

   private Purity(String name, TextColor color) {
      this.name = name;
      this.color = color;
   }

   private Purity(String name, int color) {
      this(name, TextColor.fromRgb(color));
   }

   public String getName() {
      return this.name;
   }

   public TextColor getColor() {
      return this.color;
   }

   public String getSerializedName() {
      return this.name;
   }

   public TieredWeights weights() {
      return (TieredWeights)PurityWeightsRegistry.getWeights().get(this);
   }

   public Purity next() {
      return this == PERFECT ? this : BY_ID.apply(this.ordinal() + 1);
   }

   public boolean isAtLeast(Purity other) {
      return this.ordinal() >= other.ordinal();
   }

   public MutableComponent toComponent() {
      return Apotheosis.lang("purity", this.getSerializedName(), new Object[0]).withStyle(Style.EMPTY.withColor(this.color));
   }

   public static Purity max(Purity p1, Purity p2) {
      return BY_ID.apply(Math.max(p1.ordinal(), p2.ordinal()));
   }

   public static Purity random(GenContext ctx) {
      return random(ctx, ALL_PURITIES);
   }

   public static Purity random(GenContext ctx, Set<Purity> pool) {
      if (pool.isEmpty()) {
         pool = ALL_PURITIES;
      }

      List<Weighted<Purity>> list = pool.stream().<Weighted<Purity>>mapMulti(TieredWeights.wrapFilter(ctx)).toList();
      return WeightedRandom.getRandomItem(ctx.rand(), list, Weighted::weight)
         .<Purity>map(Weighted::value)
         .orElse((Purity)ApothMiscUtil.getRandomElement(pool, ctx.rand()));
   }

   public static <T> MapCodec<Map<Purity, T>> mapCodec(Codec<T> elementCodec) {
      return Codec.simpleMap(CODEC, elementCodec, Keyable.forStrings(() -> Arrays.stream(values()).map(StringRepresentable::getSerializedName)));
   }
}
