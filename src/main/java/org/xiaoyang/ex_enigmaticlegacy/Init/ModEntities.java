
package org.xiaoyang.ex_enigmaticlegacy.Init;

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

import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.AlphirinePortal;
import org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Block.tile.EntityAdvancedSpark;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.ef.EntitySlash;
import org.xiaoyang.ex_enigmaticlegacy.Entity.biological.*;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.*;
import org.xiaoyang.ex_enigmaticlegacy.Exe;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Exe.MODID);

	public static final RegistryObject<EntityType<EntityInfinityArrowLevel>> INFINITY_ARROW_LEVEL_ENTITY =
			ENTITIES.register("infinity_arrow_level", () ->
					EntityType.Builder.<EntityInfinityArrowLevel>of(
							EntityInfinityArrowLevel::new, MobCategory.MISC)
							.sized(0.5F, 0.5F)
							.clientTrackingRange(4)
							.updateInterval(20)
							.fireImmune()
							.noSummon()
							.build("infinity_arrow_level")
			);

	public static final RegistryObject<EntityType<EntitySlimeCannonBall>> SLIME_CANNON_BALL =
			ENTITIES.register("slime_cannon_ball", () ->
					EntityType.Builder.<EntitySlimeCannonBall>of(
							EntitySlimeCannonBall::new, MobCategory.MISC)
							.sized(0.51F, 0.51F)
							.clientTrackingRange(64)
							.updateInterval(1)
							.build( "slime_cannon_ball")
			);

	public static final RegistryObject<EntityType<EntityManaVine>> MANA_VINE_BALL =
			ENTITIES.register("mana_vine_ball",
					() -> EntityType.Builder.<EntityManaVine>of(EntityManaVine::new, MobCategory.MISC)
							.sized(0.25F, 0.25F)
							.clientTrackingRange(64)
							.updateInterval(10)
							.setShouldReceiveVelocityUpdates(true)
							.build("mana_vine_ball")
			);

	public static final RegistryObject<EntityType<EntitySeed>> ENTITY_SEED =
			ENTITIES.register("entity_seed",
			() -> EntityType.Builder.<EntitySeed>of(EntitySeed::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build( "entity_seed")
	);

	public static final RegistryObject<EntityType<EntityContinuumBomb>> CONTINUUM_BOMB =
			ENTITIES.register("continuum_bomb",
			() -> EntityType.Builder.<EntityContinuumBomb>of(EntityContinuumBomb::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build("continuum_bomb")
			);

	public static final RegistryObject<EntityType<EntitySlingBullet>> SLING_BULLET =
			ENTITIES.register("sling_bullet", () ->
					EntityType.Builder.<EntitySlingBullet>of(
									EntitySlingBullet::new, MobCategory.MISC)
							.sized(0.25F, 0.25F)
							.clientTrackingRange(250)
							.updateInterval(1)
							.build("sling_bullet")
			);

	public static final RegistryObject<EntityType<BlackHoleEntity>> BLACK_HOLE =
			ENTITIES.register("black_hole",
			() -> EntityType.Builder.<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
					.sized(1.0F, 1.0F)
					.clientTrackingRange(64)
					.updateInterval(1)
					.fireImmune()
					.build("black_hole"));

	public static final RegistryObject<EntityType<EntityDarkMatterOrb>> DARK_MATTER_ORB =
			ENTITIES.register("dark_matter_orb",
			() -> EntityType.Builder.<EntityDarkMatterOrb>of(EntityDarkMatterOrb::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(64)
					.updateInterval(20)
					.setShouldReceiveVelocityUpdates(true)
					.build("dark_matter_orb"));

	public static final RegistryObject<EntityType<EntityCrimsonOrb>> CRIMSON_ORB =
			ENTITIES.register("crimson_orb",
			() -> EntityType.Builder.<EntityCrimsonOrb>of(EntityCrimsonOrb::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(64)
					.updateInterval(1)
					.setShouldReceiveVelocityUpdates(true)
					.build("crimson_orb"));

	public static final RegistryObject<EntityType<EntityShinyEnergy>> SHINY_ENERGY =
			ENTITIES.register("shiny_energy",
			() -> EntityType.Builder.<EntityShinyEnergy>of(EntityShinyEnergy::new, MobCategory.MISC)
					.sized(0.0F, 0.0F)
					.clientTrackingRange(64)
					.updateInterval(20)
					.setShouldReceiveVelocityUpdates(true)
					.build("shiny_energy"));

	public static final RegistryObject<EntityType<EntityChaoticOrb>> CHAOTIC_ORB =
			ENTITIES.register("chaotic_orb", () -> EntityType.Builder
					.<EntityChaoticOrb>of(EntityChaoticOrb::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(64)
					.updateInterval(20)
					.fireImmune()
					.noSummon()
					.build("chaotic_orb"));

	public static final RegistryObject<EntityType<EntityRageousMissile>> RAGEOUS_MISSILE =
			ENTITIES.register("rageous_missile", () -> EntityType.Builder
					.<EntityRageousMissile>of(EntityRageousMissile::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(64)
					.updateInterval(20)
					.setShouldReceiveVelocityUpdates(true)
					.build("rageous_missile"));

	public static final RegistryObject<EntityType<EntityBabylonWeaponSS>> BABYLON_WEAPON_SS =
			ENTITIES.register("babylon_weapon_ss", () -> EntityType.Builder
					.<EntityBabylonWeaponSS>of(EntityBabylonWeaponSS::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(64)
					.updateInterval(20)
					.setShouldReceiveVelocityUpdates(true)
					.build("babylon_weapon_ss"));

	public static final RegistryObject<EntityType<EntityLunarFlare>> LUNAR_FLARE =
			ENTITIES.register("lunar_flare",
					() -> EntityType.Builder.<EntityLunarFlare>of(EntityLunarFlare::new, MobCategory.MISC)
							.sized(0.25F, 0.25F)
							.clientTrackingRange(196)
							.updateInterval(20)
							.setShouldReceiveVelocityUpdates(true)
							.build("lunar_flare"));

	public static final RegistryObject<EntityType<EntityThunderpealOrb>> THUNDERPEAL_ORB =
			ENTITIES.register("thunderpeal_orb", () ->
					EntityType.Builder.<EntityThunderpealOrb>of(EntityThunderpealOrb::new, MobCategory.MISC)
							.sized(0.25F, 0.25F)
							.clientTrackingRange(64)
							.updateInterval(20)
							.setShouldReceiveVelocityUpdates(true)
							.build("thunderpeal_orb")
			);

	public static final RegistryObject<EntityType<EntitySoulEnergy>> SOUL_ENERGY =
			ENTITIES.register(
			"soul_energy",
			() -> EntityType.Builder.<EntitySoulEnergy>of(EntitySoulEnergy::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(64)
					.updateInterval(20)
					.setShouldReceiveVelocityUpdates(true)
					.build("soul_energy")
	);

	public static final RegistryObject<EntityType<EntitySword>> ENTITY_SWORD =
			ENTITIES.register("entity_sword",
			() -> EntityType.Builder.<EntitySword>of(EntitySword::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build("entity_sword")
	);

	public static final RegistryObject<EntityType<EntityAdvancedSpark>> ADVANCED_SPARK =
			ENTITIES.register("advanced_spark",
			() -> EntityType.Builder.<EntityAdvancedSpark>of
							(EntityAdvancedSpark::new, MobCategory.MISC)
					.sized(0.2F, 0.5F)
					.fireImmune()
					.clientTrackingRange(4)
					.updateInterval(10)
					.build("advanced_spark")
	);

	public static final RegistryObject<EntityType<AlphirinePortal>> ALPHIRINE_PORTAL =
			ENTITIES.register("alphirine_portal",
					() -> EntityType.Builder.<AlphirinePortal>of
									(AlphirinePortal::new, MobCategory.MISC)
							.sized(0.25F, 0.25F)
							.clientTrackingRange(4)
							.updateInterval(20)
							.build("alphirine_portal")
			);

	public static final RegistryObject<EntityType<RideablePearlEntity>> RIDEABLE_PEARL_ENTITY =
			ENTITIES.register("rideable_pearl_entity",
					() -> EntityType.Builder.<RideablePearlEntity>of(RideablePearlEntity::new, MobCategory.MISC)
							.sized(0.25F, 0.25F)
							.clientTrackingRange(4)
							.updateInterval(10)
							.build("rideable_pearl_entity")
			);

	public static final RegistryObject<EntityType<CloneEntity>> CLONE_ENTITY =
			ENTITIES.register("clone_entity",
			() -> EntityType.Builder.of(CloneEntity::new, MobCategory.MONSTER)
					.sized(0.6f, 1.8f)
					.build("clone_entity")
			);

	public static final RegistryObject<EntityType<NatureBoltEntity>> NATURE_BOLT =
			ENTITIES.register("nature_bolt",
			() -> EntityType.Builder.<NatureBoltEntity>of(NatureBoltEntity::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(10)
					.build("nature_bolt")
	);

	public static final RegistryObject<EntityType<SacabambaspisEntity>> SACABAMBASPIS =
			ENTITIES.register("sacabambaspis",
					() -> EntityType.Builder.of(SacabambaspisEntity::new, MobCategory.WATER_AMBIENT)
							.sized(0.6F, 0.5F)
							.build("sacabambaspis")
			);

	public static final RegistryObject<EntityType<EntitySlash>> ENTITY_SLASH = register("slash",
			EntityType.Builder.of(EntitySlash::new, MobCategory.MISC)
					.sized(0.5F, 0.5F)
					.clientTrackingRange(64)
					.updateInterval(1)
					.setShouldReceiveVelocityUpdates(false)
	);

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


	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return ENTITIES.register(registryname, () -> entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			Xiaoyang010Entity.init();
			Xingyun2825Entity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(XIAOYANG_010.get(), Xiaoyang010Entity.createAttributes().build());
		event.put(XINGYUN2825.get(),Xingyun2825Entity.createAttributes().build());
		event.put(KIND_MIAO.get(), CatMewEntity.createAttributes().build());
		event.put(CLONE_ENTITY.get(), CloneEntity.createAttributes().build());
		event.put(SACABAMBASPIS.get(), SacabambaspisEntity.createAttributes().build());
	}

}
