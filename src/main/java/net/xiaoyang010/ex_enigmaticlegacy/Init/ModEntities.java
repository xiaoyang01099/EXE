
package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.entity.*;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.*;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
/*import net.xiaoyang010.ex_enigmaticlegacy.entity.biological.Sacabambaspis;*/
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.SpectriteWither;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, ExEnigmaticlegacyMod.MODID);

	public static final RegistryObject<EntityType<Xiaoyang010Entity>> XIAOYANG_010 = register("xiaoyang_010", EntityType.Builder.<Xiaoyang010Entity>of(Xiaoyang010Entity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(100).setUpdateInterval(3).setCustomClientFactory(Xiaoyang010Entity::new).fireImmune().sized(0.6f, 1.8f));

	public static final RegistryObject<EntityType<MiaoMiaoEntity>> KIND_MIAO = register("kind_miao", EntityType.Builder.<MiaoMiaoEntity>of(MiaoMiaoEntity::new, MobCategory.CREATURE).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(100).setUpdateInterval(3).setCustomClientFactory(MiaoMiaoEntity::new).fireImmune().sized(0.6f, 1.8f));

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
			MiaoMiaoEntity.init();
		});
	}
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(XIAOYANG_010.get(), Xiaoyang010Entity.createAttributes().build());
		event.put(XINGYUN2825.get(),Xingyun2825Entity.createAttributes().build());
		event.put(SPECTRITE_WITHER.get(), SpectriteWither.createAttributes().build());
		event.put(KIND_MIAO.get(), MiaoMiaoEntity.createAttributes().build());
	}

}
