package com.yaskulsky.equivox.gameObjs.registries;

import com.yaskulsky.equivox.PECore;
import com.yaskulsky.equivox.gameObjs.block_entities.AlchBlockEntityChest;
import com.yaskulsky.equivox.gameObjs.block_entities.CollectorMK1BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.CollectorMK2BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.CollectorMK3BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.CondenserMK2BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.CondenserBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.DMFurnaceBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.RMFurnaceBlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.RelayMK1BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.RelayMK2BlockEntity;
import com.yaskulsky.equivox.gameObjs.block_entities.RelayMK3BlockEntity;
import com.yaskulsky.equivox.gameObjs.container.AlchBagContainer;
import com.yaskulsky.equivox.gameObjs.container.AlchChestContainer;
import com.yaskulsky.equivox.gameObjs.container.CollectorMK1Container;
import com.yaskulsky.equivox.gameObjs.container.CollectorMK2Container;
import com.yaskulsky.equivox.gameObjs.container.CollectorMK3Container;
import com.yaskulsky.equivox.gameObjs.container.CondenserContainer;
import com.yaskulsky.equivox.gameObjs.container.CondenserMK2Container;
import com.yaskulsky.equivox.gameObjs.container.DMFurnaceContainer;
import com.yaskulsky.equivox.gameObjs.container.EternalDensityContainer;
import com.yaskulsky.equivox.gameObjs.container.MercurialEyeContainer;
import com.yaskulsky.equivox.gameObjs.container.RMFurnaceContainer;
import com.yaskulsky.equivox.gameObjs.container.RelayMK1Container;
import com.yaskulsky.equivox.gameObjs.container.RelayMK2Container;
import com.yaskulsky.equivox.gameObjs.container.RelayMK3Container;
import com.yaskulsky.equivox.gameObjs.container.ArcaneTabletContainer;
import com.yaskulsky.equivox.gameObjs.container.TransmutationContainer;
import com.yaskulsky.equivox.gameObjs.registration.impl.ContainerTypeDeferredRegister;
import com.yaskulsky.equivox.gameObjs.registration.impl.ContainerTypeRegistryObject;

public class PEContainerTypes {

	public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(PECore.MODID);

	public static final ContainerTypeRegistryObject<RMFurnaceContainer> RM_FURNACE_CONTAINER = CONTAINER_TYPES.register(PEBlocks.RED_MATTER_FURNACE, RMFurnaceBlockEntity.class, RMFurnaceContainer::new);
	public static final ContainerTypeRegistryObject<DMFurnaceContainer> DM_FURNACE_CONTAINER = CONTAINER_TYPES.register(PEBlocks.DARK_MATTER_FURNACE, DMFurnaceBlockEntity.class, DMFurnaceContainer::new);
	public static final ContainerTypeRegistryObject<CondenserContainer> CONDENSER_CONTAINER = CONTAINER_TYPES.register(PEBlocks.CONDENSER, CondenserBlockEntity.class, CondenserContainer::new);
	public static final ContainerTypeRegistryObject<CondenserMK2Container> CONDENSER_MK2_CONTAINER = CONTAINER_TYPES.register(PEBlocks.CONDENSER_MK2, CondenserMK2BlockEntity.class, CondenserMK2Container::new);
	public static final ContainerTypeRegistryObject<AlchChestContainer> ALCH_CHEST_CONTAINER = CONTAINER_TYPES.register(PEBlocks.ALCHEMICAL_CHEST, AlchBlockEntityChest.class, AlchChestContainer::new);
	public static final ContainerTypeRegistryObject<AlchBagContainer> ALCH_BAG_CONTAINER = CONTAINER_TYPES.register("alchemical_bag", AlchBagContainer::fromNetwork);
	public static final ContainerTypeRegistryObject<EternalDensityContainer> ETERNAL_DENSITY_CONTAINER = CONTAINER_TYPES.register(PEItems.GEM_OF_ETERNAL_DENSITY, EternalDensityContainer::fromNetwork);
	public static final ContainerTypeRegistryObject<TransmutationContainer> TRANSMUTATION_CONTAINER = CONTAINER_TYPES.register(PEBlocks.TRANSMUTATION_TABLE, TransmutationContainer::fromNetwork);
	public static final ContainerTypeRegistryObject<ArcaneTabletContainer> ARCANE_TABLET_CONTAINER = CONTAINER_TYPES.register("arcane_tablet", ArcaneTabletContainer::fromNetwork);
	public static final ContainerTypeRegistryObject<RelayMK1Container> RELAY_MK1_CONTAINER = CONTAINER_TYPES.register(PEBlocks.RELAY, RelayMK1BlockEntity.class, RelayMK1Container::new);
	public static final ContainerTypeRegistryObject<RelayMK2Container> RELAY_MK2_CONTAINER = CONTAINER_TYPES.register(PEBlocks.RELAY_MK2, RelayMK2BlockEntity.class, RelayMK2Container::new);
	public static final ContainerTypeRegistryObject<RelayMK3Container> RELAY_MK3_CONTAINER = CONTAINER_TYPES.register(PEBlocks.RELAY_MK3, RelayMK3BlockEntity.class, RelayMK3Container::new);
	public static final ContainerTypeRegistryObject<CollectorMK1Container> COLLECTOR_MK1_CONTAINER = CONTAINER_TYPES.register(PEBlocks.COLLECTOR, CollectorMK1BlockEntity.class, CollectorMK1Container::new);
	public static final ContainerTypeRegistryObject<CollectorMK2Container> COLLECTOR_MK2_CONTAINER = CONTAINER_TYPES.register(PEBlocks.COLLECTOR_MK2, CollectorMK2BlockEntity.class, CollectorMK2Container::new);
	public static final ContainerTypeRegistryObject<CollectorMK3Container> COLLECTOR_MK3_CONTAINER = CONTAINER_TYPES.register(PEBlocks.COLLECTOR_MK3, CollectorMK3BlockEntity.class, CollectorMK3Container::new);
	public static final ContainerTypeRegistryObject<MercurialEyeContainer> MERCURIAL_EYE_CONTAINER = CONTAINER_TYPES.register(PEItems.MERCURIAL_EYE, MercurialEyeContainer::fromNetwork);
}