package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.integral.enigmaticlegacy.api.items.ICursed;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEffects;
import net.xiaoyang010.ex_enigmaticlegacy.Item.armor.ManaitaArmor;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = "ex_enigmaticlegacy")
public class TerrorCrown extends ArmorItem implements INoEMCItem, ICursed {

    public TerrorCrown(ArmorMaterial material, EquipmentSlot slot) {
        super(material, slot, ManaitaArmor.MANAITA_ARMOR);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }
    }

    public static boolean isWearingTerrorCrown(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty() || !(helmet.getItem() instanceof TerrorCrown)) {
            return false;
        }

        var relicCap = helmet.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return false;
            }
        }

        return true;
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewTarget();
        if (newTarget instanceof Player player) {
            if (isWearingTerrorCrown(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isWearingTerrorCrown(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isWearingTerrorCrown(player)) {
                if (event.getSource().getDirectEntity() instanceof Projectile projectile) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Override
    public void onArmorTick(ItemStack stack, Level world, Player player) {

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return;
            }
        }

        super.onArmorTick(stack, world, player);

        RelicsEventHandler.cryHavoc(world, player, 24);

        if (!world.isClientSide) {
            Entity scannedEntity = RelicsEventHandler.getPointedEntity(world, player, 0.0, 32.0, 3.0F);

            if (scannedEntity instanceof LivingEntity targetEntity) {
                try {
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, true, true));

                    if (!targetEntity.hasEffect(MobEffects.WITHER)) {
                        targetEntity.addEffect(new MobEffectInstance(ModEffects.DROWNING.get(), 100, 6, true, true));
                        targetEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, false));
                    }

                    targetEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, true, true));
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 1, true, true));
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 2, true, true));
                } catch (Exception var7) {
                }
            }

            if (!world.isClientSide && isWearingTerrorCrown(player)) {
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(10))) {
                    if (entity instanceof Mob mob) {
                        if (mob.getTarget() == player) {
                            mob.setTarget(null);
                        }
                        mob.setLastHurtByMob(null);
                    } else if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
                        if (entity.getLastHurtByMob() == player) {
                            entity.setLastHurtByMob(null);
                        }
                    }
                }
            }
        }
    }
}