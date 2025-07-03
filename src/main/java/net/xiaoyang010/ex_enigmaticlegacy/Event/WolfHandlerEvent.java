package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class WolfHandlerEvent {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof Wolf)) {
            return;
        }

        Wolf wolf = (Wolf) event.getEntityLiving();

        if (wolf.getPersistentData().contains("TemporaryWolf")) {
            int timer = wolf.getPersistentData().getInt("DespawnTimer");

            if (timer <= 0) {
                for (int i = 0; i < 20; i++) {
                    wolf.level.addParticle(ParticleTypes.PORTAL,
                            wolf.getX() + (wolf.getRandom().nextDouble() - 0.5D) * 2.0D,
                            wolf.getY() + wolf.getRandom().nextDouble() * 2.0D,
                            wolf.getZ() + (wolf.getRandom().nextDouble() - 0.5D) * 2.0D,
                            0, 0, 0);
                }

                wolf.remove(Wolf.RemovalReason.DISCARDED);
            } else {
                wolf.getPersistentData().putInt("DespawnTimer", timer - 1);

                if (timer <= 200) {
                    wolf.level.addParticle(ParticleTypes.SMOKE,
                            wolf.getX(),
                            wolf.getY() + 0.5D,
                            wolf.getZ(),
                            0.0D, 0.0D, 0.0D);
                }
            }
        }
    }
}