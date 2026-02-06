package net.xiaoyang010.ex_enigmaticlegacy.Init;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.xiaoyang010.ex_enigmaticlegacy.Effect.*;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "ex_enigmaticlegacy");

    public static final RegistryObject<MobEffect> FLYING = EFFECTS.register("flying", () -> new FlyingEffect(MobEffectCategory.BENEFICIAL, 0x112233));
    public static final RegistryObject<MobEffect> EMESIS = EFFECTS.register("emesis", Emesis::new);
    public static final RegistryObject<MobEffect> DAMAGE_REDUCTION = EFFECTS.register("damage_reduction", DamageReduction::new);
    public static final RegistryObject<MobEffect> CREEPER_FRIENDLY = EFFECTS.register("creeper_friendly", CreeperFriendly::new);
    public static final RegistryObject<MobEffect> DROWNING = EFFECTS.register("drowning", Drowning::new);
    public static final RegistryObject<MobEffect> RANDOM_TELEPORT = EFFECTS.register("random_teleport", RandomTeleport::new);
    public static final RegistryObject<MobEffect> FREEZE_WORST = EFFECTS.register("freeze_worst", FreezeWorst::new);
    public static final RegistryObject<MobEffect> ROOTED = EFFECTS.register("rooted", Rooted::new);

    public static void registerEffects() {
        EFFECTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
