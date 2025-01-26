
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.*;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.SpectriteWither;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, ExEnigmaticlegacyMod.MODID);

//	public static final RegistryObject<EntityType<RainbowWitherSkull>> RAINBOW_WITHER_SKULL =
//			REGISTRY.register("rainbow_wither_skull",
//					() -> EntityType.Builder.<RainbowWitherSkull>of(RainbowWitherSkull::new, MobCategory.MISC)
//							.sized(0.3125F, 0.3125F)
//							.clientTrackingRange(4)
//							.updateInterval(10)
//							.fireImmune()
//							.build(new ResourceLocation("ex_enigmaticlegacy", "rainbow_wither_skull").toString()));

	public static final RegistryObject<EntityType<CloneEntity>> CLONE_ENTITY = REGISTRY.register("clone_entity",
			() -> EntityType.Builder.of(CloneEntity::new, MobCategory.MONSTER)
					.sized(0.6f, 1.8f) // 设置碰撞箱大小
					.build(new ResourceLocation("ex_enigmaticlegacy", "clone_entity").toString()));

	public static final RegistryObject<EntityType<CapybaraEntity>> CAPYBARA = REGISTRY.register("capybara",
			() -> EntityType.Builder.of(CapybaraEntity::new, MobCategory.CREATURE)
					.sized(0.9F, 1.4F)
					.clientTrackingRange(10)
					.build(ExEnigmaticlegacyMod.MODID + ":capybara"));

	public static final RegistryObject<EntityType<SpottedGardenEelEntity>> SPOTTED_GARDEN_EEL = REGISTRY.register("spotted_garden_eel",
			() -> EntityType.Builder.of(SpottedGardenEelEntity::new, MobCategory.CREATURE)
					.sized(0.9F, 1.4F)
					.clientTrackingRange(10)
					.build(ExEnigmaticlegacyMod.MODID + ":spotted_garden_eel"));

	public static final RegistryObject<EntityType<Xiaoyang010Entity>> XIAOYANG_010 = register("xiaoyang_010", EntityType.Builder.<Xiaoyang010Entity>of(Xiaoyang010Entity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(100).setUpdateInterval(3).setCustomClientFactory(Xiaoyang010Entity::new).fireImmune().sized(0.6f, 1.8f));

	public static final RegistryObject<EntityType<CatMewEntity>> KIND_MIAO = register("kind_miao", EntityType.Builder.<CatMewEntity>of(CatMewEntity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(100).setUpdateInterval(3).setCustomClientFactory(CatMewEntity::new).fireImmune().sized(0.6f, 1.8f));

	public static final RegistryObject<EntityType<Xingyun2825Entity>> XINGYUN2825 = register("xingyun_2825", EntityType.Builder.<Xingyun2825Entity>of(Xingyun2825Entity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(100).setUpdateInterval(3).setCustomClientFactory(Xingyun2825Entity::new).fireImmune().sized(0.6f, 1.8f));

	public static final RegistryObject<EntityType<EntityRainBowLightningBlot>> LIGHTNING_BLOT = register("lightning_blot", EntityType.Builder.<EntityRainBowLightningBlot>of(EntityRainBowLightningBlot::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(100).setUpdateInterval(3).fireImmune().sized(0.6f, 1.8f));

	public static final RegistryObject<EntityType<ManaitaArrow>> MANAITA_ARROW = register("manaita_arrow",
			EntityType.Builder.<ManaitaArrow>of(ManaitaArrow::new, MobCategory.MISC)
					.setTrackingRange(64)
					.setUpdateInterval(20)
					.sized(0.5f, 0.5f));

	public static final RegistryObject<EntityType<SpectriteCrystalEntity>> SPECTRITE_CRYSTAL = register("spectrite_crystal",
            EntityType.Builder.<SpectriteCrystalEntity>of(SpectriteCrystalEntity::new, MobCategory.MISC)
                    .sized(2.0F, 2.0F));

	/*public static final EntityType<EntitySlash> SLASH = EntityType.Builder
			.<EntitySlash>of(EntitySlash::new, MobCategory.MISC)
			.sized(0.1F, 0.1F)
			.build("ex_enigmaticlegacy:slash");*/

	public static final RegistryObject<EntityType<SpectriteWither>> SPECTRITE_WITHER = register(
			"spectrite_wither",
            EntityType.Builder.<SpectriteWither>of(SpectriteWither::new, MobCategory.MONSTER)
                    .sized(0.9f, 3.5f)
	);

	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			Xiaoyang010Entity.init();
			Xingyun2825Entity.init();
//			CatMewEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(XIAOYANG_010.get(), Xiaoyang010Entity.createAttributes().build());
		event.put(XINGYUN2825.get(),Xingyun2825Entity.createAttributes().build());
		event.put(SPECTRITE_WITHER.get(), SpectriteWither.createAttributes().build());
		event.put(KIND_MIAO.get(), CatMewEntity.createAttributes().build());
		event.put(CAPYBARA.get(), CapybaraEntity.createAttributes().build());
		event.put(SPOTTED_GARDEN_EEL.get(), SpottedGardenEelEntity.createAttributes().build());
		event.put(CLONE_ENTITY.get(), CloneEntity.createAttributes().build());
	}

}
