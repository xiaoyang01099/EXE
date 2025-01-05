package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.NebulaArmor;

public class FlyingEffect extends MobEffect {
    public static final String NBT_FLYING = "exe:flying";

    public FlyingEffect(MobEffectCategory category, int color) {
        super(category, color);
        MinecraftForge.EVENT_BUS.register(this); // 注册事件
    }

    @SubscribeEvent
    public void playerFall(LivingFallEvent event){
        LivingEntity living = event.getEntityLiving();
        if (living instanceof Player player) {
            boolean flying = player.getPersistentData().getBoolean(NBT_FLYING);
            boolean nebulaArmor = player.getPersistentData().getBoolean(NebulaArmor.NBT_FALL);
            if ((flying || nebulaArmor) && !player.level.isClientSide){ //取消玩家摔落
                player.getPersistentData().remove(NBT_FLYING); //重置标记
                player.getPersistentData().remove(NebulaArmor.NBT_FALL);
                event.setDamageMultiplier(0);
            }
        }
    }
}