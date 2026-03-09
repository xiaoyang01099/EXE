package net.xiaoyang010.ex_enigmaticlegacy.Event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.LichShieldRing;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModIntegrationItems;
import top.theillusivec4.curios.api.CuriosApi;
import twilightforest.TFSounds;
import twilightforest.capabilities.CapabilityList;
import twilightforest.capabilities.shield.IShieldCapability;
import twilightforest.item.TFItems;

@Mod.EventBusSubscriber(modid = ExEnigmaticlegacyMod.MODID)
public class ShieldHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {

        if (!ModList.get().isLoaded("twilightforest")) {
            return;
        }

        LivingEntity entity = event.getEntityLiving();
        DamageSource damageSource = event.getSource();

        if (!(entity instanceof Player player)) {
            return;
        }

        var curioResult = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, ModIntegrationItems.LICH_RING.get());

        if (!curioResult.isPresent()) {
            return;
        }

        ItemStack ringStack = curioResult.get().stack();

        if (LichShieldRing.isMarkedAsBroken(ringStack)) {
            return;
        }

        entity.getCapability(CapabilityList.SHIELDS).ifPresent(cap -> {
            if (handleShieldDamage(entity, damageSource, event.getAmount(), cap, ringStack)) {
                event.setCanceled(true);
            }
        });
    }

    private static boolean handleShieldDamage(LivingEntity entity, DamageSource damageSource,
                                              float damage, IShieldCapability shieldCap, ItemStack ringStack) {

        if (shieldCap.shieldsLeft() <= 0) {
            return false;
        }

        if (damageSource == DamageSource.OUT_OF_WORLD) {
            return false;
        }

        if (damageSource.isMagic() && damage > 2.0F) {

            int currentShields = shieldCap.shieldsLeft();
            shieldCap.setShields(currentShields - 1, false);

            entity.level.playSound(null, entity.blockPosition(),
                    TFSounds.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F,
                    entity.getVoicePitch() * 2.0F);

            if (shieldCap.shieldsLeft() <= 0) {
                shieldCap.breakShield();
                LichShieldRing.markAsBroken(ringStack, true);
                playShieldDestroyedEffect(entity);
            }
        } else {
            entity.level.playSound(null, entity.blockPosition(),
                    TFSounds.SHIELD_BREAK, SoundSource.PLAYERS, 1.0F,
                    entity.getVoicePitch() * 2.0F);
        }

        return true;
    }

    private static void playShieldDestroyedEffect(LivingEntity entity) {
        entity.level.playSound(null, entity.blockPosition(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 0.5F);
        entity.level.playSound(null, entity.blockPosition(),
                SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.5F, 0.8F);

        if (!entity.level.isClientSide()) {
            for (int i = 0; i < 30; i++) {
                double d0 = entity.getRandom().nextGaussian() * 0.02D;
                double d1 = entity.getRandom().nextGaussian() * 0.02D;
                double d2 = entity.getRandom().nextGaussian() * 0.02D;
                entity.level.addParticle(ParticleTypes.CRIT,
                        entity.getX() + entity.getRandom().nextDouble() * entity.getBbWidth() * 2.0D - entity.getBbWidth(),
                        entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight(),
                        entity.getZ() + entity.getRandom().nextDouble() * entity.getBbWidth() * 2.0D - entity.getBbWidth(),
                        d0, d1, d2);
            }

            for (int i = 0; i < 15; i++) {
                entity.level.addParticle(ParticleTypes.LAVA,
                        entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 2.0D,
                        entity.getY() + entity.getRandom().nextDouble() * 2.0D,
                        entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 2.0D,
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!ModList.get().isLoaded("twilightforest")) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack heldItem = event.getItemStack();
        InteractionHand hand = event.getHand();

        if (!heldItem.is(TFItems.LIVEROOT.get())) {
            return;
        }

        var curioResult = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, ModIntegrationItems.LICH_RING.get());

        if (!curioResult.isPresent()) {
            return;
        }

        ItemStack ringStack = curioResult.get().stack();

        player.getCapability(CapabilityList.SHIELDS).ifPresent(cap -> {
            if (LichShieldRing.isMarkedAsBroken(ringStack)) {
                repairShield(player, heldItem, ringStack, cap);
                event.setCanceled(true);
            } else if (cap.shieldsLeft() > 0) {
                player.displayClientMessage(
                        new TranslatableComponent("message.ex_enigmaticlegacy.shield_already_intact")
                                .withStyle(ChatFormatting.GREEN),
                        true
                );
            } else {
                player.displayClientMessage(
                        new TranslatableComponent("message.ex_enigmaticlegacy.shield_not_active")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
            }
        });
    }

    private static void repairShield(Player player, ItemStack heldItem, ItemStack ringStack,
                                     IShieldCapability shieldCap) {

        if (!player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }

        shieldCap.replenishShields();
        shieldCap.setShields(LichShieldRing.SHIELD_STRENGTH, false);

        LichShieldRing.markAsBroken(ringStack, false);

        player.level.playSound(null, player.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.level.playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.5F);
        player.level.playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5F, 2.0F);

        player.displayClientMessage(
                new TranslatableComponent("message.ex_enigmaticlegacy.shield_repaired")
                        .withStyle(ChatFormatting.AQUA),
                true
        );

        createRepairEffect(player);
    }

    private static void createRepairEffect(Player player) {
        if (!player.level.isClientSide()) {
            for (int i = 0; i < 40; i++) {
                double d0 = player.getRandom().nextGaussian() * 0.02D;
                double d1 = player.getRandom().nextGaussian() * 0.02D;
                double d2 = player.getRandom().nextGaussian() * 0.02D;

                player.level.addParticle(
                        ParticleTypes.ENCHANT,
                        player.getX() + player.getRandom().nextDouble() * player.getBbWidth() * 2.0D - player.getBbWidth(),
                        player.getY() + player.getRandom().nextDouble() * player.getBbHeight(),
                        player.getZ() + player.getRandom().nextDouble() * player.getBbWidth() * 2.0D - player.getBbWidth(),
                        d0, d1, d2
                );
            }

            for (int i = 0; i < 20; i++) {
                player.level.addParticle(
                        ParticleTypes.HEART,
                        player.getX() + (player.getRandom().nextDouble() - 0.5D) * 2.0D,
                        player.getY() + player.getRandom().nextDouble() * 2.0D,
                        player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 2.0D,
                        0.0D, 0.1D, 0.0D
                );
            }

            for (int i = 0; i < 25; i++) {
                float sparkle = 0.8F + player.getRandom().nextFloat() * 0.2F;
                float red = 0.37F * sparkle;
                float grn = 0.99F * sparkle;
                float blu = 0.89F * sparkle;

                player.level.addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        player.getX() + (player.getRandom().nextDouble() - 0.5D) * 3.0D,
                        player.getY() + player.getRandom().nextDouble() * 2.5D,
                        player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 3.0D,
                        red, grn, blu
                );
            }
        }
    }
}