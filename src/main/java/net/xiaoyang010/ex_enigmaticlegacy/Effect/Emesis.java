package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy", value = Dist.CLIENT)
public class Emesis extends MobEffect {
    private final Random random = new Random();
    private static final ResourceLocation EMESIS = new ResourceLocation("ex_enigmaticlegacy:textures/mob_effect/emesis.png");

    public Emesis() {
        super(MobEffectCategory.NEUTRAL, 0x98D982); // 绿色效果，HARMFUL类效果
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player) {
            Player player = (Player) entity;

            // 屏幕晃动效果：让视角摇晃（客户端实现）
            if (player.level.isClientSide) {
                player.setYRot(player.getYRot() + random.nextFloat() * 10 - 5); // 随机左右晃动视角
                player.setXRot((float) (player.getXRot() + random.nextFloat() * 5 - 2.5)); // 随机上下晃动视角
            }

            // 阻止玩家移动 (服务端实现)
            if (!player.level.isClientSide) {
                freezeMovement(player); // 调用阻止玩家移动方法
            }

            // 中毒效果：每 tick 造成轻微伤害
            player.hurt(DamageSource.MAGIC, 0.5f + amplifier); // 每秒造成伤害，放大器增加伤害
        }
    }

    /**
     * 阻止玩家移动。
     * @param player 玩家实体
     */
    private void freezeMovement(Player player) {
        // 将玩家的移动向量设置为零，防止物理移动
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true; // 立即停止所有运动
        player.fallDistance = 0;  // 重置坠落距离，防止坠落伤害
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0; // 每秒执行一次效果
    }

    /**
     * 客户端：每一帧冻结玩家的输入
     * 这里确保玩家在客户端无法响应移动输入。
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        // 仅在客户端处理玩家输入禁用
        if (player.level.isClientSide && player.hasEffect(ModEffects.EMESIS.get())) {
            // 禁止所有玩家的输入控制，彻底禁用移动
            player.setDeltaMovement(Vec3.ZERO); // 强制将玩家的运动设为 0
        }
    }
}
