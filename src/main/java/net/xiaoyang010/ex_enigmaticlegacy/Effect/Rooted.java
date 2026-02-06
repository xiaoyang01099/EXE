package net.xiaoyang010.ex_enigmaticlegacy.Effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class Rooted extends MobEffect {
    private static final String KNOCKBACK_RESISTANCE_UUID = "F412C29C-0DB3-11E6-B4DD-7CEA70D5A8C7";
    private static final String MOVEMENT_SPEED_UUID = "F412C29C-0DB3-11E6-B4DD-7CEA70D5A8C8";
    private static final WeakHashMap<LivingEntity, Vec3> ROOTED_POSITIONS = new WeakHashMap<>();

    public Rooted() {
        super(MobEffectCategory.HARMFUL, 0x634D05);
        MinecraftForge.EVENT_BUS.register(this);

        addAttributeModifier(
                Attributes.KNOCKBACK_RESISTANCE,
                KNOCKBACK_RESISTANCE_UUID,
                1.0,
                AttributeModifier.Operation.ADDITION
        );

        addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_UUID,
                -1.0,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);

        ROOTED_POSITIONS.put(entity, entity.position());

        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }

        entity.setDeltaMovement(Vec3.ZERO);
        entity.hasImpulse = false;
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        ROOTED_POSITIONS.remove(entity);

        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
        }
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        rootEntity(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private void rootEntity(LivingEntity entity) {
        Vec3 rootedPos = ROOTED_POSITIONS.get(entity);
        if (rootedPos == null) {
            rootedPos = entity.position();
            ROOTED_POSITIONS.put(entity, rootedPos);
        }

        entity.setPos(rootedPos.x, rootedPos.y, rootedPos.z);

        entity.setDeltaMovement(Vec3.ZERO);
        entity.hasImpulse = false;
        entity.fallDistance = 0;

        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
            mob.getNavigation().stop();
            mob.setTarget(null);
        }

        if (entity instanceof Player player) {
            if (player.getAbilities().flying && !player.isCreative() && !player.isSpectator()) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (!entity.hasEffect(this)) {
            return;
        }

        rootEntity(entity);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntityLiving();

        if (!entity.hasEffect(this)) {
            return;
        }

        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0, 1));

        Vec3 rootedPos = ROOTED_POSITIONS.get(entity);
        if (rootedPos != null) {
            entity.setPos(rootedPos.x, rootedPos.y, rootedPos.z);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (!player.hasEffect(this)) {
            return;
        }

        rootEntity(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingTeleport(net.minecraftforge.event.entity.EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!entity.hasEffect(this)) {
            return;
        }

        event.setCanceled(true);
    }

    @Override
    public @NotNull List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    @Override
    public boolean isBeneficial() {
        return false;
    }
}
