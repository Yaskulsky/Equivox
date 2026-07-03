package moze_intel.projecte.config;

import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.config.CustomEMCParser.CustomEMCFile;
import moze_intel.projecte.impl.codec.CodecTestHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
@DisplayName("Test parsing Custom EMC files")
class CustomEMCParserTest {

	private static CustomEMCFile parseJson(HolderLookup.Provider registryAccess, String json) {
		return CodecTestHelper.parseJson(registryAccess, CustomEMCParser.CustomEMCFile.CODEC, "custom emc test", json);
	}

	@Test
	@DisplayName("Test custom emc file that is empty")
	void testEmpty(MinecraftServer server) {
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": [
					]
				}""");
		Assertions.assertNull(customEMCFile.comment());
		Assertions.assertEquals(0, customEMCFile.entries().size());
	}

	@Test
	@DisplayName("Test custom emc file that only contains a comment")
	void testCommentOnly(MinecraftServer server) {
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"comment": "A very simple Example",
					"entries": [
					]
				}""");
		Assertions.assertEquals("A very simple Example", customEMCFile.comment());
		Assertions.assertEquals(0, customEMCFile.entries().size());
	}

	@Test
	@DisplayName("Test custom emc file with a few entries")
	void testSimple(MinecraftServer server) {
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"id": "minecraft:dirt",
							"emc": 1
						},
						{
							"id": "minecraft:stone",
							"emc": 2
						},
						{
							"tag": "c:ingots/iron",
							"emc": 3
						}
					]
				}""");
		Object2LongMap<NSSItem> entries = customEMCFile.entries();
		Assertions.assertEquals(3, entries.size());
		Assertions.assertEquals(1, entries.getLong(NSSItem.createItem(Items.DIRT)));
		Assertions.assertEquals(2, entries.getLong(NSSItem.createItem(Items.STONE)));
		Assertions.assertEquals(3, entries.getLong(NSSItem.createTag(Tags.Items.INGOTS_IRON)));
	}

	@Test
	@DisplayName("Test custom emc file with an entry that is a long")
	void testCustomEmcFileWithLongValue(MinecraftServer server) {
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"id": "minecraft:dirt",
							"emc": 2147483648
						}
					]
				}""");
		Object2LongMap<NSSItem> entries = customEMCFile.entries();
		Assertions.assertEquals(1, entries.size());
		//Max int + 1
		Assertions.assertEquals(2_147_483_648L, entries.getLong(NSSItem.createItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test custom emc file with an invalid value")
	void testCustomEmcFileWithInvalidValue(MinecraftServer server) {
		Assertions.assertThrows(JsonParseException.class, () -> parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"id": "minecraft:dirt",
							"emc": -1
						}
					]
				}"""));
	}

	@Test
	@DisplayName("Test custom emc file with an invalid value")
	void testInvalidKeyAndValue(MinecraftServer server) {
		//Note: We validate this doesn't throw as invalid keys are just entirely ignored and their values are not checked
		Assertions.assertDoesNotThrow(() -> parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"invalid": "minecraft:dirt",
							"emc": -1
						}
					]
				}"""));
	}

	@Test
	@DisplayName("Test ignoring invalid keys in a custom emc file")
	void testIgnoreInvalidKeys(MinecraftServer server) {
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"item": "INVALID|minecraft:dirt",
							"emc": 1
						},
						{
							"id": "INVALID|minecraft:dirt",
							"emc": 1
						},
						{
							"id": "minecraft:stone",
							"emc": 2
						},
						{
							"fluid": "minecraft:stone",
							"emc": 4
						}
					]
				}""");
		Object2LongMap<NSSItem> entries = customEMCFile.entries();
		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(2, entries.getLong(NSSItem.createItem(Items.STONE)));
	}

	@Test
	@DisplayName("Test custom emc file with values of zero")
	void testCustomEmcFileWithZero(MinecraftServer server) {
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"id": "minecraft:dirt",
							"emc": 0
						}
					]
				}""");
		Object2LongMap<NSSItem> entries = customEMCFile.entries();
		Assertions.assertEquals(1, entries.size());
		Assertions.assertEquals(0, entries.getLong(NSSItem.createItem(Items.DIRT)));
	}

	@Test
	@DisplayName("Test custom emc file with items dependent on data components")
	void testCustomEmcFileWithDC(MinecraftServer server) {
		CustomEMCFile customEMCFile = parseJson(server.registryAccess(), """
				{
					"entries": [
						{
							"id": "minecraft:dirt",
							"data": {
								"custom_data": {
									"my": "tag"
								}
							},
							"emc": 1
						},
						{
							"id": "minecraft:stone",
							"data": {
								"custom_data": "{my: \\"tag\\"}"
							},
							"emc": 2
						}
					]
				}""");
		Object2LongMap<NSSItem> entries = customEMCFile.entries();
		Assertions.assertEquals(2, entries.size());
		Assertions.assertEquals(1, entries.getLong(NSSItem.createItem(Items.DIRT, CodecTestHelper.MY_TAG_PATCH)));
		Assertions.assertEquals(2, entries.getLong(NSSItem.createItem(Items.STONE, CodecTestHelper.MY_TAG_PATCH)));
	}
}