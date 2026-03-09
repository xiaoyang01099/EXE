package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModDamageSources;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.PlayerMotionUpdateMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.TelekinesisAttackMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.TelekinesisParticleMessage;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.TelekinesisUseMessage;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TelekinesisTome extends Item implements INoEMCItem {
    private static final float TELEKINESIS_TOME_DAMAGE_MIN = 16.0F;
    private static final float TELEKINESIS_TOME_DAMAGE_MAX = 40.0F;
    private static final boolean ALT_TELEKINESIS_ALGORITHM = true;

    private static final float RANGE = 3.0F;
    private static final int COST = 2;
    private static HashMap<Player, HashMap<String, Object>> globalTomeMap = new HashMap<>();
    private static final String TAG_TICKS_TILL_EXPIRE = "ticksTillExpire";
    private static final String TAG_TICKS_COOLDOWN = "ticksCooldown";
    private static final String TAG_TARGET = "target";
    private static final String TAG_DIST = "dist";
    private static final String TAG_RE_DIST = "reDist";
    private boolean verificationVariable = false;

    public TelekinesisTome(Properties tab) {
        super(tab);
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

    public static HashMap<String, Object> getPlayerTomeData(Player player) {
        if (globalTomeMap.containsKey(player)) {
            return globalTomeMap.get(player);
        } else {
            HashMap<String, Object> stats = new HashMap<>();
            stats.put(TAG_TICKS_TILL_EXPIRE, 0);
            stats.put(TAG_TICKS_COOLDOWN, 0);
            stats.put(TAG_TARGET, -1);
            stats.put(TAG_DIST, -1.0);
            stats.put(TAG_RE_DIST, -1.0);
            globalTomeMap.put(player, stats);
            return stats;
        }
    }

    public static Object getTomeTag(Player player, String tag, Object expectedValue) {
        HashMap<String, Object> playerData = getPlayerTomeData(player);
        return playerData.getOrDefault(tag, expectedValue);
    }

    public static void setTomeTag(Player player, String tag, Object value) {
        HashMap<String, Object> playerData = getPlayerTomeData(player);
        playerData.put(tag, value);
        globalTomeMap.put(player, playerData);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasPressedAttackKey() {
        if (Minecraft.getInstance().options.keyAttack.isDown() && !this.verificationVariable) {
            this.verificationVariable = true;
            return true;
        } else if (!Minecraft.getInstance().options.keyAttack.isDown() && this.verificationVariable) {
            this.verificationVariable = false;
            return false;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome1.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome2.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome3.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome4.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome5.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome6.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome7.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome8.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome9.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("item.ItemTelekinesisTome10.lore"));
            tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
            tooltip.add(new TranslatableComponent("Damage: " + (int)TELEKINESIS_TOME_DAMAGE_MIN + "~" + (int)TELEKINESIS_TOME_DAMAGE_MAX));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore"));
        }
        tooltip.add(new TranslatableComponent("item.FREmpty.lore"));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player player) {

            if (!level.isClientSide) {
                var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
                if (relic != null) {
                    relic.tickBinding(player);
                }
            }

            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relicInstance = relicCap.orElse(null);
                if (relicInstance != null && !relicInstance.isRightPlayer(player)) {
                    return;
                }
            }

            int ticksTillExpire = (Integer) getTomeTag(player, TAG_TICKS_TILL_EXPIRE, 0);
            int ticksCooldown = (Integer) getTomeTag(player, TAG_TICKS_COOLDOWN, 0);

            if (ticksTillExpire == 0) {
                setTomeTag(player, TAG_TARGET, -1);
                setTomeTag(player, TAG_DIST, -1.0);
                setTomeTag(player, TAG_RE_DIST, -1.0);
            }

            --ticksTillExpire;
            if (ticksCooldown > 0) {
                --ticksCooldown;
            }

            setTomeTag(player, TAG_TICKS_TILL_EXPIRE, ticksTillExpire);
            setTomeTag(player, TAG_TICKS_COOLDOWN, ticksCooldown);

            if (level.isClientSide) {
                if (!ALT_TELEKINESIS_ALGORITHM) {
                    boolean attack = false;
                    if (Minecraft.getInstance().screen == null &&
                            Minecraft.getInstance().options.keyUse.isDown() &&
                            player.getMainHandItem() == stack) {
                        NetworkHandler.CHANNEL.sendToServer(new TelekinesisUseMessage());
                        onUsingTickAlt(stack, player, 0);
                        attack = true;
                    }

                    if (attack && player.getMainHandItem() == stack && hasPressedAttackKey()) {
                        NetworkHandler.CHANNEL.sendToServer(new TelekinesisAttackMessage(true));
                    }
                } else if (player.getUseItem() == stack && hasPressedAttackKey()) {
                    NetworkHandler.CHANNEL.sendToServer(new TelekinesisAttackMessage(true));
                }
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (livingEntity instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    player.stopUsingItem();
                    return;
                }
            }

            onUsingTickAlt(stack, player, getUseDuration(stack) - remainingUseDuration);

            if (level.isClientSide && hasPressedAttackKey()) {
                NetworkHandler.CHANNEL.sendToServer(new TelekinesisAttackMessage(true));
            }
        }
    }

    public static void onUsingTickAlt(ItemStack stack, Player player, int count) {

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return;
            }
        }

        Level world = player.level;
        int targetID = (Integer) getTomeTag(player, TAG_TARGET, -1);
        int ticksCooldown = (Integer) getTomeTag(player, TAG_TICKS_COOLDOWN, 0);
        double length = (Double) getTomeTag(player, TAG_DIST, -1.0);
        double re_dist = (Double) getTomeTag(player, TAG_RE_DIST, -1.0);

        if (ticksCooldown == 0) {
            LivingEntity item = null;
            if (targetID != -1 && world.getEntity(targetID) != null) {
                item = getExistingTarget(player, world, targetID, 6.0F);
            }

            if (item == null) {
                item = searchForTarget(player, world, 3.0F);
            }

            length = 7.5;
            if (item != null && re_dist == -1.0) {
                re_dist = player.distanceTo(item);
            }

            if (item != null) {
                item.fallDistance = 0.0F;
                if (item.getEffect(MobEffects.LEVITATION) == null) {
                    item.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 2, 3));
                }

                Vector3 target3 = Vector3.fromEntityCenter(player);
                if (player.isShiftKeyDown()) {
                    target3.add(new Vector3(player.getViewVector(1.0F)).multiply(re_dist));
                } else {
                    target3.add(new Vector3(player.getViewVector(1.0F)).multiply(length));
                    re_dist = player.distanceTo(item);
                }

                target3.y += 0.5;
                Vector3 entityCenter = Vector3.fromEntityCenter(item);
                if (!world.isClientSide) {
                    LivingEntity finalItem = item;
                    NetworkHandler.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                                    finalItem.getX(), finalItem.getY(), finalItem.getZ(), 64.0, finalItem.level.dimension())),
                            new TelekinesisParticleMessage(entityCenter.x, entityCenter.y, entityCenter.z, 1.0F));
                }

                double multiplier = item.distanceToSqr(target3.x, target3.y, target3.z);
                float vectorPower = 0.66666F;
                if (multiplier < 1.5) {
                    vectorPower = 0.333333F;
                } else if (multiplier >= 8.0) {
                    vectorPower *= (float)(multiplier / 8.0);
                }

                if (RelicsEventHandler.isEntityBlacklistedFromTelekinesis(item)) {
                    return;
                }

                setEntityMotionFromVector(item, target3, vectorPower);
                setTomeTag(player, TAG_TARGET, item.getId());
                setTomeTag(player, TAG_DIST, length);
                setTomeTag(player, TAG_RE_DIST, re_dist);
            }

            if (item != null) {
                setTomeTag(player, TAG_TICKS_TILL_EXPIRE, 5);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            setTomeTag(player, TAG_TARGET, -1);
            setTomeTag(player, TAG_DIST, -1.0);
            setTomeTag(player, TAG_RE_DIST, -1.0);
            setTomeTag(player, TAG_TICKS_TILL_EXPIRE, 0);
        }
    }

    public static void leftClick(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof TelekinesisTome)  {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }

            int targetID = (Integer) getTomeTag(player, TAG_TARGET, -1);
            LivingEntity item = null;
            if (targetID != -1 && player.level.getEntity(targetID) != null) {
                item = getExistingTarget(player, player.level, targetID, 6.0F);
                if (item != null) {
                    lightningAttack(player, item, stack, player.level);
                }
            }
        }
    }

    public static void setEntityMotionFromVector(Entity entity, Vector3 originalPosVector, float modifier) {
        Vector3 entityVector = Vector3.fromEntityCenter(entity);
        Vector3 finalVector = originalPosVector.copy().subtract(entityVector);
        if (finalVector.mag() > 1.0) {
            finalVector.normalize();
        }

        entity.setDeltaMovement(finalVector.x * modifier, finalVector.y * modifier, finalVector.z * modifier);
        if (entity instanceof Player && !entity.level.isClientSide) {
            NetworkHandler.sendToPlayer((ServerPlayer) entity,
                    new PlayerMotionUpdateMessage(finalVector.x * modifier, finalVector.y * modifier, finalVector.z * modifier));
        }
    }

    public static LivingEntity searchForTarget(Player player, Level world, float range) {
        LivingEntity newTarget = null;
        Vector3 target = Vector3.fromEntityCenter(player);
        List<LivingEntity> entities = new ArrayList<>();

        for (int distance = 1; entities.isEmpty() && distance < 32; ++distance) {
            target.add(new Vector3(player.getViewVector(1.0F)).multiply(distance));
            target.y += 0.5;
            entities = world.getEntitiesOfClass(LivingEntity.class, new AABB(
                    target.x - range, target.y - range, target.z - range,
                    target.x + range, target.y + range, target.z + range));
            entities.remove(player);
        }

        if (!entities.isEmpty()) {
            newTarget = entities.get(0);
        }

        return newTarget;
    }

    public static LivingEntity getExistingTarget(Player player, Level world, int targetID, float range) {
        Entity entity = world.getEntity(targetID);
        if (!(entity instanceof LivingEntity taritem)) {
            return null;
        }

        Vector3 target = Vector3.fromEntityCenter(player);
        for (int distance = 1; distance < 32; ++distance) {
            target.add(new Vector3(player.getViewVector(1.0F)).multiply(distance));
            target.y += 0.5;
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, new AABB(
                    target.x - range, target.y - range, target.z - range,
                    target.x + range, target.y + range, target.z + range));
            entities.remove(player);

            if (entities.contains(taritem)) {
                return taritem;
            }
        }

        return null;
    }

    public static void lightningAttack(Player player, LivingEntity target, ItemStack stack, Level world) {
        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return;
            }
        }

        if (!world.isClientSide && !RelicsEventHandler.isOnCoodown(player)) {
            Vector3 TVec = Vector3.fromEntityCenter(target);
            Vec3 moveVector = player.getViewVector(1.0F);

            if (player.distanceTo(target) <= 16.0F) {
                for (int counter = 0; counter <= 3; ++counter) {
                    RelicsEventHandler.imposeLightning(player.level,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            TVec.x, TVec.y, TVec.z, 20,
                            (float)(1.0F / player.distanceTo(target) * (2.0F + Math.random() * 4.0F)),
                            (int)(player.distanceTo(target) * 1.2F), 0,
                            (float)(0.225F + player.distanceToSqr(target) / 2000.0F));
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, player.getSoundSource(), 1.0F, 0.8F);
                target.hurt(new ModDamageSources.DamageSourceTLightning(player),
                        (float)(TELEKINESIS_TOME_DAMAGE_MIN + (TELEKINESIS_TOME_DAMAGE_MAX - TELEKINESIS_TOME_DAMAGE_MIN) * Math.random()));
            }

            if (player.isShiftKeyDown()) {
                target.setDeltaMovement(moveVector.x * 3.0, moveVector.y * 1.5, moveVector.z * 3.0);
                setTomeTag(player, TAG_TARGET, -1);
                setTomeTag(player, TAG_DIST, -1.0);
                setTomeTag(player, TAG_RE_DIST, -1.0);
                setTomeTag(player, TAG_TICKS_TILL_EXPIRE, 0);
                setTomeTag(player, TAG_TICKS_COOLDOWN, 40);
            }

            RelicsEventHandler.setCasted(player, 10, true);
        }
    }
}