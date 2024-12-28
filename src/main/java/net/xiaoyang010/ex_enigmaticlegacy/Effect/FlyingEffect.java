package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyingEffect extends MobEffect {
    private static final Map<UUID, Boolean> playerFlyingStates = new HashMap<>();

    public FlyingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Player player) {
            // 保存玩家的初始飞行状态
            playerFlyingStates.put(player.getUUID(), player.getAbilities().mayfly);

            // 设置飞行能力
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            // 确保飞行能力持续存在
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (entity instanceof Player player) {
            // 检查是否还有其他相同的效果
            boolean hasOtherFlyingEffect = player.getActiveEffects()
                    .stream()
                    .filter(effect -> effect.getEffect() instanceof FlyingEffect)
                    .count() > 1;

            // 只有在没有其他飞行效果时才恢复原始状态
            if (!hasOtherFlyingEffect) {
                // 恢复玩家的原始飞行状态
                boolean originalFlyingState = playerFlyingStates.getOrDefault(player.getUUID(), false);
                if (!player.isCreative() && !originalFlyingState) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                }
                player.onUpdateAbilities();
                playerFlyingStates.remove(player.getUUID());
            }
        }
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    // 在玩家登出时清理状态
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        playerFlyingStates.remove(event.getPlayer().getUUID());
    }
}