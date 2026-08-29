package org.xiaoyang.ex_enigmaticlegacy.Item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.xiaoyang.ex_enigmaticlegacy.Util.PlayerUtil;
import org.xiaoyang.ex_enigmaticlegacy.Config.ConfigFile;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.FlySwordEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.DimensionSlashDomainEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.SwordAuraEntity;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// FlySwordItem 负责飞剑召唤、剑气和次元斩公共逻辑。
public class FlySwordItem extends SwordItem {
    private static final Component TOOLTIP_SUMMON = Component.translatable("item.akatzumatool.fly_sword.tooltip.1"); // 召唤提示。
    private static final Component TOOLTIP_AURA = Component.translatable("item.akatzumatool.fly_sword.tooltip.4"); // 剑气提示。

    public int flySwordMoveState = 0; // 兼容旧逻辑保留的移动状态字段。

    // 构造飞剑物品。
    public FlySwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    // 客户端使用自定义飞剑手持渲染器，把第一/第三人称手持模型提交给后处理 bloom 队列。
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final FlySwordHeldItemRenderer renderer = new FlySwordHeldItemRenderer(); // 飞剑手持后处理提交渲染器。

            @Override
            public FlySwordHeldItemRenderer getCustomRenderer() {
                return this.renderer;
            }
        });
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            trySpawnSwordAura(player);
        } /*else if (attacker instanceof EntityMaid maid) {
            MaidMeleeAttackTask.trySpawnSwordAura(maid);
        }*/
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(TOOLTIP_SUMMON);
        tooltip.add(TOOLTIP_AURA);
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return super.use(level, player, usedHand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return super.onItemUseFirst(stack, context);
    }

    // 按指定数量召唤飞剑实体。
    public static void spawnFlySword(Level level, Player player, int count) {
        for (int i = 1; i <= count; i++) {
            FlySwordEntity entity = new FlySwordEntity(ModEntities.FLY_SWORD_ENTITY.get(), level);
            entity.setOwner(player, i);
            entity.setPos(player.position());
            level.addFreshEntity(entity);
        }
    }

    // 收集当前玩家已经召唤的飞剑实体。
    public static List<FlySwordEntity> getOwnedFlySwords(ServerLevel serverLevel, Player player) {
        List<FlySwordEntity> ownedFlySwords = new ArrayList<>();
        if (serverLevel == null || player == null) return ownedFlySwords;

        // 只收集当前服务端世界中归属该玩家的飞剑。
        Iterable<Entity> entities = serverLevel.getEntities().getAll();
        for (Entity entity : entities) {
            if (entity instanceof FlySwordEntity flySword && flySword.getMaster() == player) {
                ownedFlySwords.add(flySword);
            }
        }
        return ownedFlySwords;
    }

    // 关闭当前玩家已经召唤的全部飞剑。
    public static boolean closeOwnedFlySwords(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide()) return false;

        // 先收集再移除，避免遍历实体集合时直接修改集合状态。
        List<FlySwordEntity> ownedFlySwords = getOwnedFlySwords((ServerLevel) player.level(), player);
        for (FlySwordEntity flySword : ownedFlySwords) {
            flySword.remove(Entity.RemovalReason.DISCARDED);
        }
        return !ownedFlySwords.isEmpty();
    }

    // 切换当前玩家飞剑状态：没有飞剑时召唤，已有飞剑时关闭。
    public static boolean toggleFlySwords(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide()) return false;
        if (!isHoldingAnyFlySword(player)) return false;

        ServerLevel serverLevel = (ServerLevel) player.level();
        List<FlySwordEntity> ownedFlySwords = getOwnedFlySwords(serverLevel, player);
        if (!ownedFlySwords.isEmpty()) {
            // 已有飞剑时 B 键只关闭，不再立即重召。
            for (FlySwordEntity flySword : ownedFlySwords) {
                flySword.remove(Entity.RemovalReason.DISCARDED);
            }
            return true;
        }

        // 没有飞剑时按当前手持飞剑类型召唤对应数量。
        int count = isHoldingFlySwordPlus(player) ? 5 : 2;
        spawnFlySword(serverLevel, player, count);
        return true;
    }

    // 召唤或关闭当前玩家的飞剑。
    public static boolean trySummonFlySwords(Player player) {
        return toggleFlySwords(player);
    }

    // 释放飞剑左键剑气。
    public static void trySpawnSwordAura(Player player) {
        if (player == null) return;
        if (player.level().isClientSide()) return;
        if (!(player.getMainHandItem().getItem() instanceof FlySwordItem)) return;

        SwordAuraEntity aura = new SwordAuraEntity(ModEntities.SWORD_AURA_ENTITY.get(), player.level());
        Vec3 origin = player.getEyePosition().add(player.getViewVector(1.0F).scale(0.35D));
        aura.setAuraData(player, origin, player.getViewVector(1.0F), ConfigFile.flySwordAuraDamage());
        player.level().addFreshEntity(aura);
    }

    // 释放次元斩并扣除 2 点饱食度。
    public static boolean trySpawnDimensionSlash(Player player) {
        if (player == null) return false;
        if (player.level().isClientSide()) return false;
        if (!isHoldingFlySwordPlus(player)) return false;

        DimensionSlashDomainEntity domain = DimensionSlashDomainEntity.create(player);
        player.level().addFreshEntity(domain);
        PlayerUtil.deductFood(player, 2);
        return true;
    }

    // 判断是否手持任意飞剑。
    public static boolean isHoldingAnyFlySword(Player player) {
        if (player == null) return false;
        return player.getMainHandItem().getItem() instanceof FlySwordItem
                || player.getOffhandItem().getItem() instanceof FlySwordItem;
    }

    // 判断是否手持真·飞剑。
    public static boolean isHoldingFlySwordPlus(Player player) {
        if (player == null) return false;
        return player.getMainHandItem().getItem() instanceof FlySwordPlusItem
                || player.getOffhandItem().getItem() instanceof FlySwordPlusItem;
    }

    // 取得玩家手持真·飞剑的手，优先主手。
    public static InteractionHand getHeldFlySwordPlusHand(Player player) {
        if (player == null) return null;
        if (player.getMainHandItem().getItem() instanceof FlySwordPlusItem) return InteractionHand.MAIN_HAND;
        if (player.getOffhandItem().getItem() instanceof FlySwordPlusItem) return InteractionHand.OFF_HAND;
        return null;
    }

    // 兼容旧调用入口。
    public static boolean isHoldingFlySword(Player player) {
        return isHoldingAnyFlySword(player);
    }
}
