package com.yaskulsky.equivox.gameObjs.registries;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.api.capabilities.PECapabilities;
import com.yaskulsky.equivox.gameObjs.block_entities.AlchBlockEntityChest;
import com.yaskulsky.equivox.gameObjs.block_entities.CollectorMK1BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.CollectorMK2BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.CollectorMK3BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.CondenserBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.CondenserMK2BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.DMFurnaceBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.DMPedestalBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.EmcChestBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.EntropySinkBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.EntropySinkDarkBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.EntropySinkRedBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.InterdictionBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.RMFurnaceBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.RelayMK1BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.RelayMK2BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.RelayMK3BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.StellarCondenserBlockEntity;
import com.yaskulsky.equivox.gameObjs.registration.impl.BlockEntityTypeDeferredRegister;
import com.yaskulsky.equivox.gameObjs.registration.impl.BlockEntityTypeRegistryObject;
import net.neoforged.neoforge.capabilities.Capabilities;

public class PEBlockEntityTypes {

	public static final BlockEntityTypeDeferredRegister BLOCK_ENTITY_TYPES = new BlockEntityTypeDeferredRegister(PECore.MODID);

	public static final BlockEntityTypeRegistryObject<AlchBlockEntityChest> ALCHEMICAL_CHEST = BLOCK_ENTITY_TYPES.builder(PEBlocks.ALCHEMICAL_CHEST, AlchBlockEntityChest::new)
			.clientTicker(AlchBlockEntityChest::tickClient)
			.serverTicker(AlchBlockEntityChest::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, AlchBlockEntityChest.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<CollectorMK1BlockEntity> COLLECTOR = BLOCK_ENTITY_TYPES.builder(PEBlocks.COLLECTOR, CollectorMK1BlockEntity::new)
			.serverTicker(CollectorMK1BlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, CollectorMK1BlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<CollectorMK2BlockEntity> COLLECTOR_MK2 = BLOCK_ENTITY_TYPES.builder(PEBlocks.COLLECTOR_MK2, CollectorMK2BlockEntity::new)
			.serverTicker(CollectorMK1BlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, CollectorMK1BlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<CollectorMK3BlockEntity> COLLECTOR_MK3 = BLOCK_ENTITY_TYPES.builder(PEBlocks.COLLECTOR_MK3, CollectorMK3BlockEntity::new)
			.serverTicker(CollectorMK1BlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, CollectorMK1BlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<CondenserBlockEntity> CONDENSER = BLOCK_ENTITY_TYPES.builder(PEBlocks.CONDENSER, CondenserBlockEntity::new)
			.clientTicker(EmcChestBlockEntity::lidAnimateTick)
			.serverTicker(CondenserBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, CondenserBlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<CondenserMK2BlockEntity> CONDENSER_MK2 = BLOCK_ENTITY_TYPES.builder(PEBlocks.CONDENSER_MK2, CondenserMK2BlockEntity::new)
			.clientTicker(EmcChestBlockEntity::lidAnimateTick)
			.serverTicker(CondenserBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, CondenserBlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<RelayMK1BlockEntity> RELAY = BLOCK_ENTITY_TYPES.builder(PEBlocks.RELAY, RelayMK1BlockEntity::new)
			.serverTicker(RelayMK1BlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, RelayMK1BlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<RelayMK2BlockEntity> RELAY_MK2 = BLOCK_ENTITY_TYPES.builder(PEBlocks.RELAY_MK2, RelayMK2BlockEntity::new)
			.serverTicker(RelayMK1BlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, RelayMK1BlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<RelayMK3BlockEntity> RELAY_MK3 = BLOCK_ENTITY_TYPES.builder(PEBlocks.RELAY_MK3, RelayMK3BlockEntity::new)
			.serverTicker(RelayMK1BlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, RelayMK1BlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<DMFurnaceBlockEntity> DARK_MATTER_FURNACE = BLOCK_ENTITY_TYPES.builder(PEBlocks.DARK_MATTER_FURNACE, DMFurnaceBlockEntity::new)
			.serverTicker(DMFurnaceBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, DMFurnaceBlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<RMFurnaceBlockEntity> RED_MATTER_FURNACE = BLOCK_ENTITY_TYPES.builder(PEBlocks.RED_MATTER_FURNACE, RMFurnaceBlockEntity::new)
			.serverTicker(DMFurnaceBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, DMFurnaceBlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<InterdictionBlockEntity> INTERDICTION_TORCH = BLOCK_ENTITY_TYPES.builder(PEBlocks.INTERDICTION_TORCH, InterdictionBlockEntity::new)
			.commonTicker(InterdictionBlockEntity::tick)
			.build();
	public static final BlockEntityTypeRegistryObject<DMPedestalBlockEntity> DARK_MATTER_PEDESTAL = BLOCK_ENTITY_TYPES.builder(PEBlocks.DARK_MATTER_PEDESTAL, DMPedestalBlockEntity::new)
			.clientTicker(DMPedestalBlockEntity::tickClient)
			.serverTicker(DMPedestalBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, DMPedestalBlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<EntropySinkBlockEntity> ENTROPY_SINK = BLOCK_ENTITY_TYPES.builder(PEBlocks.ENTROPY_SINK, EntropySinkBlockEntity::new)
			.serverTicker(EntropySinkBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, EntropySinkBlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<EntropySinkDarkBlockEntity> ENTROPY_SINK_DARK = BLOCK_ENTITY_TYPES.builder(PEBlocks.ENTROPY_SINK_DARK, EntropySinkDarkBlockEntity::new)
			.serverTicker(EntropySinkBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, EntropySinkBlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<EntropySinkRedBlockEntity> ENTROPY_SINK_RED = BLOCK_ENTITY_TYPES.builder(PEBlocks.ENTROPY_SINK_RED, EntropySinkRedBlockEntity::new)
			.serverTicker(EntropySinkBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.with(Capabilities.Item.BLOCK, EntropySinkBlockEntity.INVENTORY_PROVIDER)
			.build();
	public static final BlockEntityTypeRegistryObject<StellarCondenserBlockEntity> STELLAR_CONDENSER = BLOCK_ENTITY_TYPES.builder(PEBlocks.STELLAR_CONDENSER, StellarCondenserBlockEntity::new)
			.serverTicker(StellarCondenserBlockEntity::tickServer)
			.with(PECapabilities.EMC_STORAGE_CAPABILITY, (be, side) -> be)
			.build();
}