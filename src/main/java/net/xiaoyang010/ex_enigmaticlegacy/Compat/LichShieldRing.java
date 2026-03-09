package net.xiaoyang010.ex_enigmaticlegacy.Compat;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModIntegrationItems;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import twilightforest.capabilities.CapabilityList;
import javax.annotation.Nullable;
import java.util.List;

public class LichShieldRing extends Item implements ICurioItem {
    public static final int SHIELD_STRENGTH = 6;

    public LichShieldRing() {
        super((new Properties().stacksTo(1).tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM)));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(LichShieldRing.class);
        }
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level.isClientSide()) {
            entity.getCapability(CapabilityList.SHIELDS).ifPresent(cap -> {
                if (cap.shieldsLeft() <= 0 && !isMarkedAsBroken(stack)) {
                    cap.setShields(SHIELD_STRENGTH, false);
                }
            });
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level.isClientSide()) {
            entity.getCapability(CapabilityList.SHIELDS).ifPresent(cap -> {
                if (!isMarkedAsBroken(stack)) {
                    cap.setShields(SHIELD_STRENGTH, false);
                }
            });
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level.isClientSide()) {
            entity.getCapability(CapabilityList.SHIELDS).ifPresent(cap -> {
                cap.setShields(0, false);
            });
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (isMarkedAsBroken(stack)) {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.lich_shield_ring.tooltip.broken").withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.lich_shield_ring.tooltip.repair").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.lich_shield_ring.tooltip.1").withStyle(ChatFormatting.GOLD));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.lich_shield_ring.tooltip.2").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.lich_shield_ring.tooltip.3").withStyle(ChatFormatting.RED));
        }
    }

    public static boolean isMarkedAsBroken(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("ShieldBroken");
    }

    public static void markAsBroken(ItemStack stack, boolean broken) {
        stack.getOrCreateTag().putBoolean("ShieldBroken", broken);
    }

    public static int getShieldStrength(ItemStack stack) {
        if (stack.isEmpty() || isMarkedAsBroken(stack)) {
            return 0;
        }
        return SHIELD_STRENGTH;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            ItemStack shieldStack = CuriosApi.getCuriosHelper()
                    .findEquippedCurio(ModIntegrationItems.LICH_RING.get(), player)
                    .map(ImmutableTriple::getRight)
                    .orElse(ItemStack.EMPTY);

            if (shieldStack.isEmpty()) return;

            int shieldStrength = LichShieldRing.getShieldStrength(shieldStack);
            if (shieldStrength <= 0) return;

            renderLichShield(player, shieldStrength, event.getPoseStack(), event.getPartialTick());
        }
    }

    private static void renderLichShield(Player player, int shieldStrength, PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Level level = player.level;

        double playerX = player.xOld + (player.getX() - player.xOld) * partialTick;
        double playerY = player.yOld + (player.getY() - player.yOld) * partialTick;
        double playerZ = player.zOld + (player.getZ() - player.zOld) * partialTick;

        var camera = mc.gameRenderer.getMainCamera();
        double cameraX = camera.getPosition().x;
        double cameraY = camera.getPosition().y;
        double cameraZ = camera.getPosition().z;

        poseStack.pushPose();
        poseStack.translate(playerX - cameraX, playerY - cameraY + player.getBbHeight() * 0.5, playerZ - cameraZ);

        renderShieldParticles(player, playerX, playerY, playerZ, shieldStrength);

        poseStack.popPose();
    }

    private static void renderShieldParticles(Player player, double x, double y, double z, int shieldStrength) {

        Level level = player.level;
        for (int i = 0; i < shieldStrength; i++) {
            if (player.getRandom().nextInt(8) == 0) {
                float time = player.tickCount * 0.1F + i * 2.0F;
                float radius = 1.2F + i * 0.15F;
                double particleX = x + Math.cos(time) * radius;
                double particleY = y + player.getBbHeight() * 0.5F + Math.sin(time * 1.5F) * 0.3F;
                double particleZ = z + Math.sin(time) * radius;

                level.addParticle(ParticleTypes.ENCHANT,
                        particleX, particleY, particleZ,
                        0.0, 0.05, 0.0);
            }
        }

        if (player.getRandom().nextInt(4) == 0) {
            double radius = 1.5 + player.getRandom().nextDouble() * 0.5;
            double angle2 = player.getRandom().nextDouble() * 2 * Math.PI;
            double particleX = x + Math.cos(angle2) * radius;
            double particleY = y - 1.3 + player.getRandom().nextDouble() * 0.2;
            double particleZ = z + Math.sin(angle2) * radius;

            level.addParticle(ParticleTypes.ENCHANT,
                    particleX, particleY, particleZ,
                    0.0, 0.02, 0.0);
        }
    }
}