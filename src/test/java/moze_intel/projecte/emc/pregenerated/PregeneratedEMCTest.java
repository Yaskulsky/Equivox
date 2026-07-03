package moze_intel.projecte.emc.pregenerated;

import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
@DisplayName("Test Pregenerated EMC Serialization")
class PregeneratedEMCTest {

	private static Object2LongMap<ItemInfo> parseJson(HolderLookup.Provider registryAccess, String json) {
		return CodecTestHelper.parseJson(registryAccess, PregeneratedEMC.CODEC, "pregnerated emc test", json);
	}

	@Test
	@DisplayName("Test empty pregen file")
	void testEmptyPregenFile(MinecraftServer server) {
		Object2LongMap<ItemInfo> pregenerated = parseJson(server.registryAccess(), "[]");
		Assertions.assertEquals(0, pregenerated.size());
	}

	@Test
	@DisplayName("Test a simple pregen file")
	void testSimplePregenFile(MinecraftServer server) {
		Object2LongMap<ItemInfo> pregenerated = parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"emc": 1
					}
				]""");
		Assertions.assertEquals(1, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.getLong(ItemInfo.fromItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test pregen file with duplicate entries")
	void testPregenFileWithDuplicates(MinecraftServer server) {
		Assertions.assertThrows(JsonParseException.class, () -> parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"emc": 1
					},
					{
						"item": "minecraft:dirt",
						"emc": 2
					}
				]"""));
	}

	@Test
	@DisplayName("Test pregen file with long values")
	void testPregenFileLongValues(MinecraftServer server) {
		Object2LongMap<ItemInfo> pregenerated = parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"emc": 2147483648
					}
				]""");
		Assertions.assertEquals(1, pregenerated.size());
		//Max int + 1
		Assertions.assertEquals(2_147_483_648L, pregenerated.getLong(ItemInfo.fromItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test pregen file with keys that contains data components")
	void testPregenFileWithDC(MinecraftServer server) {
		Object2LongMap<ItemInfo> pregenerated = parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"data": {
							"custom_data": {
								"my": "tag"
							}
						},
						"emc": 1
					}
				]""");
		Assertions.assertEquals(1, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.getLong(ItemInfo.fromItem(Items.DIRT, CodecTestHelper.MY_TAG_PATCH)));
	}

	@Test
	@DisplayName("Test pregen file with keys that contain an empty data component")
	void testPregenFileWithEmptyDC(MinecraftServer server) {
		//Empty data components are ignored and are treated as if they aren't there
		Object2LongMap<ItemInfo> pregenerated = parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"data": {},
						"emc": 1
					}
				]""");
		Assertions.assertEquals(1, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.getLong(ItemInfo.fromItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test pregen file with invalid value")
	void testPregenFileInvalidValues(MinecraftServer server) {
		Assertions.assertThrows(JsonParseException.class, () -> parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"emc": 0
					}
				]"""));
		Assertions.assertThrows(JsonParseException.class, () -> parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"emc": -1
					}
				]"""));
	}

	@Test
	@DisplayName("Test pregen file with invalid keys")
	void testPregenFileInvalidKeys(MinecraftServer server) {
		//Test to ensure we skip over any invalid keys rather than throwing an exception and failing to deserialize anything
		Object2LongMap<ItemInfo> pregenerated = parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"emc": 1
					},
					{
						"item": "projecte:invalid",
						"emc": 2
					},
					{
						"item": "minecraft:stone",
						"emc": 3
					},
					{
						"item": "INVALID",
						"emc": 4
					},
					{
						"invalid": "minecraft:dirt",
						"emc": 5
					},
					{
						"item": "minecraft:granite",
						"data": {
							"unknown": true
						},
						"emc": 6
					}
				]""");
		Assertions.assertEquals(2, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.getLong(ItemInfo.fromItem(Items.DIRT)));
		Assertions.assertEquals(3, pregenerated.getLong(ItemInfo.fromItem(Items.STONE)));
	}

	@Test
	@DisplayName("Test pregen file with missing final value, and a valid key")
	void testPregenFileMissingValueValidKey(MinecraftServer server) {
		Assertions.assertThrows(JsonParseException.class, () -> parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"emc": 1
					},
					{
						"item": "minecraft:stone"
					}
				]"""));
	}

	@Test
	@DisplayName("Test pregen file with missing final value, and an invalid key")
	void testPregenFileMissingValueInvalidKey(MinecraftServer server) {
		//Test to ensure we don't fail to load other data if our last entry is missing a value rather than throwing an exception and failing to deserialize anything
		Object2LongMap<ItemInfo> pregenerated = parseJson(server.registryAccess(), """
				[
					{
						"item": "minecraft:dirt",
						"emc": 1
					},
					{
						"item": "minecraft:invalid"
					}
				]""");
		Assertions.assertEquals(1, pregenerated.size());
		Assertions.assertEquals(1, pregenerated.getLong(ItemInfo.fromItem(Items.DIRT)));
	}
}