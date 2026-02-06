package net.xiaoyang010.ex_enigmaticlegacy.Client.particle.ef;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class EffectManager {
    public static List<Effect> effects = new ArrayList<>();
    public static List<Effect> toAdd = new ArrayList<>();

    public static void addEffect(Effect e) {
        toAdd.add(e);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (event.phase == TickEvent.Phase.START && mc.player != null && mc.level != null) {
            for (int i = 0; i < effects.size(); ++i) {
                Effect effect = effects.get(i);
                if (!effect.dead) {
                    effect.update();
                }
            }
        }

        if (event.phase == TickEvent.Phase.END) {
            effects.addAll(toAdd);
            toAdd.clear();

            effects.removeIf(effect -> effect.dead);
        }
    }

    @SubscribeEvent
    public void onRenderLast(RenderLevelLastEvent event) {
        PoseStack poseStack = event.getPoseStack();
        float partialTicks = event.getPartialTick();

        for (Effect effect : effects) {
            if (effect != null && !effect.dead) {
                effect.renderTotal(poseStack, partialTicks);
            }
        }
    }
}
