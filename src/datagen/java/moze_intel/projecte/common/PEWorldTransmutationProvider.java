package moze_intel.projecte.common;

import com.google.common.collect.BiMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.data.WorldTransmutationBuilder;
import moze_intel.projecte.api.data.WorldTransmutationProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import org.jetbrains.annotations.NotNull;

public class PEWorldTransmutationProvider extends WorldTransmutationProvider {

	public PEWorldTransmutationProvider(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, PECore.MODID);
	}

	@Override
	protected void addWorldTransmutations(@NotNull HolderLookup.Provider registries) {
		createTransmutationBuilder(PECore.rl("defaults"))
				.comment("Default world transmutations for simple vanilla blocks.")
				.register(Blocks.STONE, Blocks.COBBLESTONE, Blocks.GRASS_BLOCK)
				.register(Blocks.COBBLESTONE, Blocks.STONE, Blocks.GRASS_BLOCK)
				.register(Blocks.GRASS_BLOCK, Blocks.SAND, Blocks.COBBLESTONE)
				.register(Blocks.SAND, Blocks.GRASS_BLOCK, Blocks.COBBLESTONE)
				.register(Blocks.DIRT, Blocks.SAND, Blocks.COBBLESTONE)
				.registerConsecutivePairs(Blocks.GRAVEL, Blocks.SANDSTONE)
				.registerConsecutivePairs(Blocks.ICE.defaultBlockState(), IceBlock.meltsInto())
				.registerConsecutivePairs(Blocks.OBSIDIAN.defaultBlockState(), Blocks.LAVA.defaultBlockState())
				.registerConsecutivePairs(Blocks.MELON, Blocks.PUMPKIN)
				.register(Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE)
				.register(Blocks.DIORITE, Blocks.ANDESITE, Blocks.GRANITE)
				.register(Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE)

				.registerConsecutivePairs(Blocks.SOUL_SAND, Blocks.SOUL_SOIL)
				.registerConsecutivePairs(Blocks.NETHERRACK, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM)
				.registerConsecutivePairs(Blocks.NETHER_WART_BLOCK, Blocks.WARPED_WART_BLOCK)
				.registerConsecutivePairs(Blocks.CRIMSON_FUNGUS, Blocks.WARPED_FUNGUS)
				.registerConsecutivePairs(Blocks.CRIMSON_ROOTS, Blocks.WARPED_ROOTS)
		;

		createTransmutationBuilder(PECore.rl("wood"))
				.comment("Default world transmutations for wooden vanilla blocks.")
				.registerConsecutivePairs(Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.SPRUCE_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
						Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG)
				.registerConsecutivePairs(Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_JUNGLE_LOG,
						Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_CHERRY_LOG)
				.registerConsecutivePairs(Blocks.OAK_WOOD, Blocks.BIRCH_WOOD, Blocks.SPRUCE_WOOD, Blocks.JUNGLE_WOOD, Blocks.ACACIA_WOOD, Blocks.DARK_OAK_WOOD,
						Blocks.MANGROVE_WOOD, Blocks.CHERRY_WOOD)
				.registerConsecutivePairs(Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD,
						Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_CHERRY_WOOD)
				.registerConsecutivePairs(Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES,
						Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES, Blocks.CHERRY_LEAVES)
				.registerConsecutivePairs(Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING,
						Blocks.DARK_OAK_SAPLING, Blocks.MANGROVE_PROPAGULE, Blocks.CHERRY_SAPLING)
				.registerConsecutivePairs(Blocks.OAK_PLANKS, Blocks.BIRCH_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.ACACIA_PLANKS,
						Blocks.DARK_OAK_PLANKS, Blocks.MANGROVE_PLANKS, Blocks.CHERRY_PLANKS, Blocks.BAMBOO_PLANKS)
				.registerConsecutivePairs(Blocks.OAK_SLAB, Blocks.BIRCH_SLAB, Blocks.SPRUCE_SLAB, Blocks.JUNGLE_SLAB, Blocks.ACACIA_SLAB, Blocks.DARK_OAK_SLAB,
						Blocks.MANGROVE_SLAB, Blocks.CHERRY_SLAB, Blocks.BAMBOO_SLAB)
				.registerConsecutivePairs(Blocks.OAK_STAIRS, Blocks.BIRCH_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.JUNGLE_STAIRS, Blocks.ACACIA_STAIRS,
						Blocks.DARK_OAK_STAIRS, Blocks.MANGROVE_STAIRS, Blocks.CHERRY_STAIRS, Blocks.BAMBOO_STAIRS)
				.registerConsecutivePairs(Blocks.OAK_FENCE, Blocks.BIRCH_FENCE, Blocks.SPRUCE_FENCE, Blocks.JUNGLE_FENCE, Blocks.ACACIA_FENCE,
						Blocks.DARK_OAK_FENCE, Blocks.MANGROVE_FENCE, Blocks.CHERRY_FENCE, Blocks.BAMBOO_FENCE)
				.registerConsecutivePairs(Blocks.OAK_PRESSURE_PLATE, Blocks.BIRCH_PRESSURE_PLATE, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.JUNGLE_PRESSURE_PLATE,
						Blocks.ACACIA_PRESSURE_PLATE, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.MANGROVE_PRESSURE_PLATE, Blocks.CHERRY_PRESSURE_PLATE,
						Blocks.BAMBOO_PRESSURE_PLATE)
				.registerConsecutivePairs(Blocks.OAK_BUTTON, Blocks.BIRCH_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.ACACIA_BUTTON, Blocks.DARK_OAK_BUTTON,
						Blocks.MANGROVE_BUTTON, Blocks.CHERRY_BUTTON, Blocks.BAMBOO_BUTTON)
				.registerConsecutivePairs(Blocks.OAK_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.ACACIA_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR,
						Blocks.MANGROVE_TRAPDOOR, Blocks.CHERRY_TRAPDOOR, Blocks.BAMBOO_TRAPDOOR)
				.registerConsecutivePairs(Blocks.OAK_DOOR, Blocks.BIRCH_DOOR, Blocks.SPRUCE_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR,
						Blocks.MANGROVE_DOOR, Blocks.CHERRY_DOOR, Blocks.BAMBOO_DOOR)
				.registerConsecutivePairs(Blocks.OAK_SIGN, Blocks.BIRCH_SIGN, Blocks.SPRUCE_SIGN, Blocks.JUNGLE_SIGN, Blocks.ACACIA_SIGN, Blocks.DARK_OAK_SIGN,
						Blocks.MANGROVE_SIGN, Blocks.CHERRY_SIGN, Blocks.BAMBOO_SIGN)
				.registerConsecutivePairs(Blocks.OAK_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN,
						Blocks.MANGROVE_WALL_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.BAMBOO_WALL_SIGN)
				.registerConsecutivePairs(Blocks.OAK_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN,
						Blocks.MANGROVE_HANGING_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN)
				.registerConsecutivePairs(Blocks.OAK_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN,
						Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN)
				//Nether wood blocks
				.registerConsecutivePairs(Blocks.CRIMSON_STEM, Blocks.WARPED_STEM)
				.registerConsecutivePairs(Blocks.STRIPPED_CRIMSON_STEM, Blocks.STRIPPED_WARPED_STEM)
				.registerConsecutivePairs(Blocks.CRIMSON_HYPHAE, Blocks.WARPED_HYPHAE)
				.registerConsecutivePairs(Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE)
				.registerConsecutivePairs(Blocks.CRIMSON_PLANKS, Blocks.WARPED_PLANKS)
				.registerConsecutivePairs(Blocks.CRIMSON_SLAB, Blocks.WARPED_SLAB)
				.registerConsecutivePairs(Blocks.CRIMSON_STAIRS, Blocks.WARPED_STAIRS)
				.registerConsecutivePairs(Blocks.CRIMSON_FENCE, Blocks.WARPED_FENCE)
				.registerConsecutivePairs(Blocks.CRIMSON_PRESSURE_PLATE, Blocks.WARPED_PRESSURE_PLATE)
				.registerConsecutivePairs(Blocks.CRIMSON_BUTTON, Blocks.WARPED_BUTTON)
				.registerConsecutivePairs(Blocks.CRIMSON_TRAPDOOR, Blocks.WARPED_TRAPDOOR)
				.registerConsecutivePairs(Blocks.CRIMSON_DOOR, Blocks.WARPED_DOOR)
				.registerConsecutivePairs(Blocks.CRIMSON_SIGN, Blocks.WARPED_SIGN)
				.registerConsecutivePairs(Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_WALL_SIGN)
				.registerConsecutivePairs(Blocks.CRIMSON_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN)
				.registerConsecutivePairs(Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN)
		;

		createTransmutationBuilder(PECore.rl("colors"))
				.comment("Default world transmutations for various colored vanilla blocks.")
				.registerConsecutivePairs(Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE, Blocks.YELLOW_CONCRETE,
						Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE,
						Blocks.BLUE_CONCRETE, Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE)
				.registerConsecutivePairs(Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER,
						Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER,
						Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER,
						Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER)
				.registerConsecutivePairs(Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET, Blocks.LIGHT_BLUE_CARPET, Blocks.YELLOW_CARPET,
						Blocks.LIME_CARPET, Blocks.PINK_CARPET, Blocks.GRAY_CARPET, Blocks.LIGHT_GRAY_CARPET, Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET,
						Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET)
				.registerConsecutivePairs(Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL, Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL,
						Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL, Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL, Blocks.BROWN_WOOL, Blocks.GREEN_WOOL,
						Blocks.RED_WOOL, Blocks.BLACK_WOOL)
				.registerConsecutivePairs(Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA,
						Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA,
						Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA)
				.registerConsecutivePairs(Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS,
						Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS,
						Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS,
						Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS)
				.registerConsecutivePairs(Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS_PANE,
						Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS_PANE,
						Blocks.GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS_PANE,
						Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS_PANE,
						Blocks.BLACK_STAINED_GLASS_PANE)
		;

		WorldTransmutationBuilder oxidizationBuilder = createTransmutationBuilder(PECore.rl("oxidization"))
				.comment("Default world transmutations for oxidized copper.");
		//Note: We can't use the data map as that isn't populated yet
		//TODO - 1.21.4: Do we want to try and somehow have a special case variant that loads some world transmutations from the data map?
		// Maybe check for the existence of the file or some key? Wait until after 1.21.4 or whenever there are ordered reload listeners
		// so that we can ensure we are after datamaps
		BiMap<Block, Block> nextByBlock = WeatheringCopper.NEXT_BY_BLOCK.get();
		BiMap<Block, Block> previousByBlock = WeatheringCopper.PREVIOUS_BY_BLOCK.get();
		for (Block block : nextByBlock.keySet()) {
			if (!previousByBlock.containsKey(block)) {
				//We don't have a previous block for this one, it is the start of a chain
				List<Block> blocks = new ArrayList<>();
				blocks.add(block);
				Block nextBlock;
				while ((nextBlock = nextByBlock.get(block)) != null) {
					blocks.add(nextBlock);
					block = nextBlock;
				}
				if (blocks.size() > 1) {
					oxidizationBuilder.registerConsecutivePairs(blocks.toArray(Block[]::new));
				}
			}
		}
	}
}