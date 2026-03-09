package net.xiaoyang010.ex_enigmaticlegacy.api.test;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class VampireWings extends Item implements ICurioItem {
    public static final int MAX_BLOOD   = 200;
    public static final int MAX_REVIVES = 9;
    public static final float VAMPIRIC_HEAL = 10f;
    public static final int MAX_BATS    = 10;
    public static final int MAX_WOLVES  = 10;
    public static final int MIND_CONTROL_DURATION = 30 * 20;
    public static final int DARK_VEIL_DURATION = 60 * 20;

    public enum AbilityMode {
        SPIDER_CLIMB("§8爬墙模式", "§7贴墙即可爬行"),
        SUMMON_BATS("§5召唤蝙蝠", "§7召唤蝙蝠军团（上限10只）"),
        SUMMON_WOLVES("§6召唤狼群", "§7召唤狼群部队（上限10只）"),
        DARK_VEIL("§0黑夜天幕", "§7为周围玩家制造超自然黑暗"),
        MIND_CONTROL("§d心灵控制", "§7控制视线中的生物30秒");

        public final String displayName;
        public final String description;

        AbilityMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public AbilityMode next() {
            AbilityMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    private static final UUID HP_UUID     = UUID.fromString("a1b2c3d4-0001-0000-0000-000000000001");
    private static final UUID SPD_UUID    = UUID.fromString("a1b2c3d4-0001-0000-0000-000000000002");
    private static final UUID ATK_UUID    = UUID.fromString("a1b2c3d4-0001-0000-0000-000000000003");
    private static final UUID ARM_UUID    = UUID.fromString("a1b2c3d4-0001-0000-0000-000000000004");
    private static final UUID ARMTGH_UUID = UUID.fromString("a1b2c3d4-0001-0000-0000-000000000005");

    public VampireWings(Properties rarity) {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .fireResistant());
    }

    public static int getBlood(ItemStack stack) {
        return stack.getOrCreateTag().getInt("Blood");
    }

    public static void setBlood(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt("Blood", Math.max(0, Math.min(MAX_BLOOD, value)));
    }

    public static int getRevives(ItemStack stack) {
        if (!stack.getOrCreateTag().contains("Revives")) {
            stack.getOrCreateTag().putInt("Revives", MAX_REVIVES);
        }
        return stack.getOrCreateTag().getInt("Revives");
    }

    public static void setRevives(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt("Revives", Math.max(0, Math.min(MAX_REVIVES, value)));
    }

    public static int getWitherKills(ItemStack stack) {
        return stack.getOrCreateTag().getInt("WitherKills");
    }

    public static void addWitherKill(ItemStack stack) {
        int kills = getWitherKills(stack) + 1;
        if (kills >= 10) {
            kills = 0;
            int revives = getRevives(stack);
            if (revives < MAX_REVIVES) setRevives(stack, revives + 1);
        }
        stack.getOrCreateTag().putInt("WitherKills", kills);
    }

    public static AbilityMode getMode(ItemStack stack) {
        int idx = stack.getOrCreateTag().getInt("AbilityMode");
        AbilityMode[] values = AbilityMode.values();
        if (idx < 0 || idx >= values.length) idx = 0;
        return values[idx];
    }

    public static void setMode(ItemStack stack, AbilityMode mode) {
        stack.getOrCreateTag().putInt("AbilityMode", mode.ordinal());
    }

    public static boolean isClimbing(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("Climbing");
    }

    public static void setClimbing(ItemStack stack, boolean value) {
        stack.getOrCreateTag().putBoolean("Climbing", value);
    }

    public static long getCooldown(ItemStack stack) {
        return stack.getOrCreateTag().getLong("AbilityCooldown");
    }

    public static void setCooldown(ItemStack stack, long gameTime) {
        stack.getOrCreateTag().putLong("AbilityCooldown", gameTime);
    }

    public static boolean isCoolingDown(ItemStack stack, Level level, int cooldownTicks) {
        return level.getGameTime() - getCooldown(stack) < cooldownTicks;
    }

    public static void setControlTarget(ItemStack stack, UUID uuid) {
        stack.getOrCreateTag().putUUID("ControlTarget", uuid);
    }

    public static UUID getControlTarget(ItemStack stack) {
        if (stack.getOrCreateTag().hasUUID("ControlTarget")) {
            return stack.getOrCreateTag().getUUID("ControlTarget");
        }
        return null;
    }

    public static void clearControlTarget(ItemStack stack) {
        stack.getOrCreateTag().remove("ControlTarget");
        stack.getOrCreateTag().remove("ControlEnd");
    }

    public static void setControlEnd(ItemStack stack, long endTime) {
        stack.getOrCreateTag().putLong("ControlEnd", endTime);
    }

    public static long getControlEnd(ItemStack stack) {
        return stack.getOrCreateTag().getLong("ControlEnd");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent("§4吸血翅膀"));
        tooltip.add(new TextComponent("§c吸血值: " + getBlood(stack) + " / " + MAX_BLOOD));
        tooltip.add(new TextComponent("§6复活次数: " + getRevives(stack) + " / " + MAX_REVIVES));
        int witherKills = getWitherKills(stack);
        if (witherKills > 0) {
            tooltip.add(new TextComponent("§8凋零击杀进度: " + witherKills + " / 10"));
        }
        tooltip.add(new TextComponent("§7§o夜晚可以飞行，免疫摔落伤害"));
        tooltip.add(new TextComponent("§5当前模式: " + getMode(stack).displayName));
        tooltip.add(new TextComponent("§7" + getMode(stack).description));
        tooltip.add(new TextComponent("§8[蹲下+右键] 切换模式  [右键] 激活能力"));
    }

    @Override
    public void onEquip(SlotContext ctx, ItemStack prevStack, ItemStack stack) {
        if (!(ctx.entity() instanceof Player player)) return;
        applyAttributes(player);
        if (player.level.isClientSide) {
            player.displayClientMessage(
                    new TextComponent("§4你感到一股黑暗力量涌入体内..."), true);
        }
    }

    @Override
    public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
        if (!(ctx.entity() instanceof Player player)) return;
        removeAttributes(player);
        clearControlTarget(stack);
        if (!player.level.isClientSide) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            dismissAllMinions(player);
        }
    }

    @Override
    public void curioTick(SlotContext ctx, ItemStack stack) {
        if (!(ctx.entity() instanceof Player player)) return;
        Level level = player.level;

        if (!level.isClientSide && level.isDay()) {
            float brightness = player.getBrightness();
            BlockPos eyePos = new BlockPos(player.getX(), player.getEyeY(), player.getZ());
            boolean wet = player.isInWaterRainOrBubble() || player.isInPowderSnow || player.wasInPowderSnow;
            if (brightness > 0.5F && !wet && level.canSeeSky(eyePos)
                    && player.random.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F) {
                player.setSecondsOnFire(2);
            }
        }

        if (!level.isClientSide) {
            boolean isCreativeOrSpectator = player.isCreative() || player.isSpectator();
            if (level.isNight()) {
                if (!player.getAbilities().mayfly) {
                    player.getAbilities().mayfly = true;
                    player.onUpdateAbilities();
                }
            } else {
                if (player.getAbilities().mayfly && !isCreativeOrSpectator) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
            }
        }

        if (!level.isClientSide && !player.isOnGround() && !player.getAbilities().flying) {
            if (player.getDeltaMovement().y < -0.5 && !player.isInWater()) {
                player.setDeltaMovement(
                        player.getDeltaMovement().x * 1.1,
                        Math.max(player.getDeltaMovement().y * 0.6, -0.5),
                        player.getDeltaMovement().z * 1.1
                );
                player.fallDistance = 0;
            }
        }

        if (!level.isClientSide && level.isNight()) {
            player.fallDistance = 0;
        }

        if (!level.isClientSide) {
            player.getFoodData().setFoodLevel(20);
        }

        tickRegen(player, level);

        tickSpiderClimb(player, stack, level);

        tickMindControl(player, stack, level);

        if (!level.isClientSide) {
            int blood = getBlood(stack);
            if (level.getGameTime() % 100 == 0) {
                if (blood <= 0) {
                    player.hurt(DamageSource.MAGIC, 2.0f);
                } else {
                    setBlood(stack, blood - 1);
                }
            }
        }

        if (level.isClientSide && level.getGameTime() % 200 == 0) {
            if (getBlood(stack) < 40) {
                player.displayClientMessage(new TextComponent("§4你感到饥渴...需要吸血！"), true);
            }
        }
    }

    private void tickSpiderClimb(Player player, ItemStack stack, Level level) {
        if (level.isClientSide) return;

        boolean climbMode = getMode(stack) == AbilityMode.SPIDER_CLIMB;

        if (climbMode && player.horizontalCollision && !player.isOnGround()) {
            double currentY = player.getDeltaMovement().y;
            if (currentY < 0) {
                player.setDeltaMovement(
                        player.getDeltaMovement().x,
                        0,
                        player.getDeltaMovement().z
                );
            }
            player.fallDistance = 0;
            setClimbing(stack, true);
        } else {
            setClimbing(stack, false);
        }
    }

    private void tickRegen(Player player, Level level) {
        if (level.isClientSide) return;
        if (level.getGameTime() % 40 != 0) return;

        float maxHp = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        float currentHp = player.getHealth();
        float missing = maxHp - currentHp;

        if (missing <= 0) return;

        float healAmount;
        float ratio = currentHp / maxHp;
        if (ratio < 0.25f) {
            healAmount = 3.0f;
        } else if (ratio < 0.5f) {
            healAmount = 2.0f;
        } else {
            healAmount = 1.0f;
        }

        player.heal(healAmount);
    }


    private void tickMindControl(Player player, ItemStack stack, Level level) {
        if (level.isClientSide) return;
        UUID targetUUID = getControlTarget(stack);
        if (targetUUID == null) return;

        long now = level.getGameTime();
        if (now > getControlEnd(stack)) {
            clearControlTarget(stack);
            player.displayClientMessage(new TextComponent("§d心灵控制已解除"), true);
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) return;

        Entity target = serverLevel.getEntity(targetUUID);
        if (target == null || !target.isAlive()) {
            clearControlTarget(stack);
            return;
        }
        if (target instanceof Mob mob) {
            mob.getNavigation().moveTo(player, 1.0);
            mob.getLookControl().setLookAt(player);
            mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 1, false, false));
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false));
        } else if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 1, false, true));
            targetPlayer.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,  60, 0, false, true));
            if (now % 40 == 0) {
                targetPlayer.displayClientMessage(
                        new TextComponent("§d你的思维被某种黑暗力量控制..."), true);
            }
        }
    }

    public static void switchMode(Player player, ItemStack stack) {
        AbilityMode next = getMode(stack).next();
        setMode(stack, next);
        player.displayClientMessage(
                new TextComponent("§5切换到: " + next.displayName + "  §7" + next.description), true);
    }

    public static void activateAbility(Player player, ItemStack stack) {
        if (player.level.isClientSide) return;
        Level level = player.level;
        AbilityMode mode = getMode(stack);

        switch (mode) {
            case SPIDER_CLIMB -> {
                player.displayClientMessage(
                        new TextComponent("§8爬墙已就绪，贴近墙壁即可爬行"), true);
            }
            case SUMMON_BATS -> activateSummonBats(player, stack, level);
            case SUMMON_WOLVES -> activateSummonWolves(player, stack, level);
            case DARK_VEIL -> activateDarkVeil(player, stack, level);
            case MIND_CONTROL -> activateMindControl(player, stack, level);
        }
    }

    private static void activateSummonBats(Player player, ItemStack stack, Level level) {
        if (isCoolingDown(stack, level, 200)) {
            long remain = 200 - (level.getGameTime() - getCooldown(stack));
            player.displayClientMessage(
                    new TextComponent("§c冷却中... §7还需 " + (remain / 20) + " 秒"), true);
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) return;

        AABB searchBox = player.getBoundingBox().inflate(50);
        long currentBats = level.getEntitiesOfClass(VampireBat.class, searchBox,
                bat -> player.getUUID().equals(bat.getOwnerUUID())).size();

        if (currentBats >= MAX_BATS) {
            player.displayClientMessage(
                    new TextComponent("§c蝙蝠数量已达上限 (" + MAX_BATS + ")"), true);
            return;
        }

        int toSummon = (int) Math.min(3, MAX_BATS - currentBats);
        for (int i = 0; i < toSummon; i++) {
            VampireBat bat = ModEntities.VAMPIRE_BAT.get().create(serverLevel);
            if (bat == null) continue;
            double angle = (2 * Math.PI / toSummon) * i;
            bat.moveTo(
                    player.getX() + Math.cos(angle) * 2,
                    player.getY() + 1,
                    player.getZ() + Math.sin(angle) * 2,
                    0, 0
            );
            bat.setOwnerUUID(player.getUUID());
            serverLevel.addFreshEntity(bat);
        }

        setCooldown(stack, level.getGameTime());
        player.displayClientMessage(
                new TextComponent("§5召唤了 §f" + toSummon + " §5只蝙蝠！"), true);
    }

    private static void activateSummonWolves(Player player, ItemStack stack, Level level) {
        if (isCoolingDown(stack, level, 400)) {
            long remain = 400 - (level.getGameTime() - getCooldown(stack));
            player.displayClientMessage(
                    new TextComponent("§c冷却中... §7还需 " + (remain / 20) + " 秒"), true);
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) return;
        AABB searchBox = player.getBoundingBox().inflate(50);
        long currentWolves = level.getEntitiesOfClass(VampireWolf.class, searchBox,
                wolf -> player.getUUID().equals(wolf.getVampireOwnerUUID())).size();
        if (currentWolves >= MAX_WOLVES) {
            player.displayClientMessage(
                    new TextComponent("§c狼群数量已达上限 (" + MAX_WOLVES + ")"), true);
            return;
        }
        int toSummon = (int) Math.min(2, MAX_WOLVES - currentWolves);
        for (int i = 0; i < toSummon; i++) {
            VampireWolf wolf = ModEntities.VAMPIRE_WOLF.get().create(serverLevel);
            if (wolf == null) continue;
            double angle = (2 * Math.PI / Math.max(toSummon, 1)) * i;
            wolf.moveTo(
                    player.getX() + Math.cos(angle) * 3,
                    player.getY(),
                    player.getZ() + Math.sin(angle) * 3,
                    player.getYRot(), 0
            );
            wolf.initAsVampireMinion(player);
            serverLevel.addFreshEntity(wolf);
        }
        setCooldown(stack, level.getGameTime());
        player.displayClientMessage(
                new TextComponent("§6召唤了 §f" + toSummon + " §6只狼！"), true);
    }

    private static void activateDarkVeil(Player player, ItemStack stack, Level level) {
        if (isCoolingDown(stack, level, 600)) { // 30秒冷却
            long remain = 600 - (level.getGameTime() - getCooldown(stack));
            player.displayClientMessage(
                    new TextComponent("§c冷却中... §7还需 " + (remain / 20) + " 秒"), true);
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) return;

        AABB range = player.getBoundingBox().inflate(64);
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(
                ServerPlayer.class, range, p -> true);

        int duration = DARK_VEIL_DURATION;
        MobEffectInstance blindness = new MobEffectInstance(MobEffects.BLINDNESS, duration / 2, 0, false, true);

        for (ServerPlayer target : nearbyPlayers) {
            if (target.getUUID().equals(player.getUUID())) continue;
            target.addEffect(blindness);
            target.displayClientMessage(
                    new TextComponent("§0天空被黑暗笼罩，你陷入了无尽的黑暗..."), true);
        }
        player.displayClientMessage(
                new TextComponent("§0你召唤了黑夜天幕，周围 §f" + (nearbyPlayers.size() - 1) +
                        " §0名玩家陷入黑暗！"), true);

        setCooldown(stack, level.getGameTime());
    }

    private static void activateMindControl(Player player, ItemStack stack, Level level) {
        if (isCoolingDown(stack, level, 1200)) {
            long remain = 1200 - (level.getGameTime() - getCooldown(stack));
            player.displayClientMessage(
                    new TextComponent("§c冷却中... §7还需 " + (remain / 20) + " 秒"), true);
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) return;

        var hitResult = player.pick(10.0, 1.0f, false);
        var eyePos  = player.getEyePosition(1.0f);
        var lookVec = player.getViewVector(1.0f);
        var endPos  = eyePos.add(lookVec.scale(10.0));
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(10.0)).inflate(1.0);

        Entity target = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : serverLevel.getEntities(player, searchBox)) {
            if (entity == player) continue;
            if (!entity.isAlive()) continue;
            if (!(entity instanceof net.minecraft.world.entity.LivingEntity)) continue;

            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            var intersection = entityBox.clip(eyePos, endPos);
            if (intersection.isPresent()) {
                double dist = eyePos.distanceTo(intersection.get());
                if (dist < minDist) {
                    minDist = dist;
                    target = entity;
                }
            }
        }

        if (target == null) {
            player.displayClientMessage(new TextComponent("§d没有找到可控制的目标"), true);
            return;
        }

        setControlTarget(stack, target.getUUID());
        setControlEnd(stack, level.getGameTime() + MIND_CONTROL_DURATION);
        setCooldown(stack, level.getGameTime());

        String targetName = target.getName().getString();
        player.displayClientMessage(
                new TextComponent("§d你控制了 §f" + targetName + " §d，持续 §f30 §d秒！"), true);

        if (target instanceof net.minecraft.world.entity.LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, MIND_CONTROL_DURATION, 0, false, false));
        }
    }

    private static void dismissAllMinions(Player player) {
        Level level = player.level;
        AABB searchBox = player.getBoundingBox().inflate(100);
        level.getEntitiesOfClass(VampireBat.class, searchBox,
                        bat -> player.getUUID().equals(bat.getOwnerUUID()))
                .forEach(Entity::discard);
        level.getEntitiesOfClass(VampireWolf.class, searchBox,
                        wolf -> player.getUUID().equals(wolf.getVampireOwnerUUID()))
                .forEach(Entity::discard);
    }

    @Override
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    private void applyAttributes(Player player) {
        addMod(player, Attributes.MAX_HEALTH,      HP_UUID,     "vampire_hp",  40,   AttributeModifier.Operation.ADDITION);
        addMod(player, Attributes.MOVEMENT_SPEED,  SPD_UUID,    "vampire_spd", 0.05, AttributeModifier.Operation.ADDITION);
        addMod(player, Attributes.ATTACK_DAMAGE,   ATK_UUID,    "vampire_atk", 10,   AttributeModifier.Operation.ADDITION);
        addMod(player, Attributes.ARMOR,           ARM_UUID,    "vampire_arm", 10,   AttributeModifier.Operation.ADDITION);
        addMod(player, Attributes.ARMOR_TOUGHNESS, ARMTGH_UUID, "vampire_ath", 4,    AttributeModifier.Operation.ADDITION);
    }

    private void removeAttributes(Player player) {
        removeMod(player, Attributes.MAX_HEALTH,      HP_UUID);
        removeMod(player, Attributes.MOVEMENT_SPEED,  SPD_UUID);
        removeMod(player, Attributes.ATTACK_DAMAGE,   ATK_UUID);
        removeMod(player, Attributes.ARMOR,           ARM_UUID);
        removeMod(player, Attributes.ARMOR_TOUGHNESS, ARMTGH_UUID);
    }

    private void addMod(Player player, Attribute attr,
                        UUID uuid, String name, double value, AttributeModifier.Operation op) {
        var instance = player.getAttribute(attr);
        if (instance != null && instance.getModifier(uuid) == null) {
            instance.addPermanentModifier(new AttributeModifier(uuid, name, value, op));
        }
    }

    private void removeMod(Player player, Attribute attr, UUID uuid) {
        var instance = player.getAttribute(attr);
        if (instance != null) instance.removeModifier(uuid);
    }
}