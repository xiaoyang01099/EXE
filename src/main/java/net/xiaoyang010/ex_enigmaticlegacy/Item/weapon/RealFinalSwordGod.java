package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;


import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import morph.avaritia.util.InfinityDamageSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityRainBowLightningBlot;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ColorText;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RealFinalSwordGod extends SwordItem {

    public RealFinalSwordGod() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 10, -2.4F, new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level.isClientSide) { // 确保代码在服务器端执行
            // 使用自定义的 DamageSource 直接杀死目标，忽略所有防护和模式
            target.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);

            // 强制将目标的生命值设为0，确保目标死亡
            target.setHealth(0.0F);

            // 如果目标是玩家，清空其食物值和经验值
            if (target instanceof Player) {
                Player player = (Player) target;
                player.getFoodData().setFoodLevel(0);
                player.experienceLevel = 0;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        return Component.nullToEmpty(ColorText.GetColor1(I18n.get("item.ex_enigmaticlegacy.real_final_sword")));
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> tooltip, TooltipFlag p_41424_) {
        tooltip.add(Component.nullToEmpty(""));
        tooltip.add(Component.nullToEmpty(ColorText.getGray(I18n.get("text.ex_enigmaticlegacy.manaita_sword.hand"))));
        tooltip.add(Component.nullToEmpty(ColorText.GetColor1(I18n.get("text.ex_enigmaticlegacy.manaita_sword.infinity"))
                + ColorText.GetGreen(I18n.get("text.ex_enigmaticlegacy.manaita_sword.damage"))));
        tooltip.add(Component.nullToEmpty(ColorText.GetGreen(I18n.get("text.ex_enigmaticlegacy.manaita_sword.speed"))));
        super.appendHoverText(p_41421_, p_41422_, tooltip, p_41424_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            Vec3 playerPos = player.position();
            double radius = 180000.0; // 设置雷电召唤的半径

            // 获取半径内的所有非玩家生物
            AABB boundingBox = new AABB(playerPos.subtract(radius, radius, radius), playerPos.add(radius, radius, radius));
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox,
                    entity -> !(entity instanceof Player)); // 排除玩家

            // 对每个非玩家生物召唤彩色雷电并杀死它们
            for (LivingEntity entity : entities) {
                createRainLightning_blot(entity.getX(), entity.getY(), entity.getZ(), level);
                // 使用雷电伤害源杀死生物
                entity.die(DamageSource.mobAttack(player));
                if (entity instanceof Player playerHurt)
                    dropItem(playerHurt);
                entity.hurt(DamageSource.LIGHTNING_BOLT, Float.MAX_VALUE);
                entity.setHealth(0.0F); // 确保死亡
                //这里确保死亡建议用setRemove()
            }
            List<ItemEntity> itemEntities = serverLevel.getEntitiesOfClass(ItemEntity.class, boundingBox);
            for (ItemEntity itemEntity : itemEntities) {
                player.getInventory().add(itemEntity.getItem());
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    private void createRainLightning_blot(double x, double y, double z, Level level) {
        if (level instanceof ServerLevel _level) {
            EntityRainBowLightningBlot entityToSpawn = ModEntities.LIGHTNING_BLOT.get().create(_level);
            entityToSpawn.moveTo(Vec3.atBottomCenterOf(new BlockPos(x, y, z)));
            entityToSpawn.setVisualOnly(true);
            _level.addFreshEntity(entityToSpawn);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level.isClientSide) {
            if (entity instanceof LivingEntity living) {
                if (entity instanceof Player playerHurt) {
                    playerHurt.hurt(new InfinityDamageSource(player), Integer.MAX_VALUE);
                    dropItem(playerHurt);
                }else living.hurt(new InfinityDamageSource(player), Float.POSITIVE_INFINITY);
                living.setHealth(-1.0f);
                if (living.isAlive()){
                    living.die(new InfinityDamageSource(player));
                    living.kill();
                    living.deathTime = 0;
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    /**
     * 被攻击玩家掉落背包物品
     * @param playerHurt
     */
    private void dropItem(Player playerHurt){
        InventoryMenu inventoryMenu = playerHurt.inventoryMenu;
        for (Slot slot : inventoryMenu.slots) {
            ItemStack item = slot.getItem();
            if (!item.isEmpty()) {
                playerHurt.drop(item, false);
            }
        }

        for (ItemStack slot : playerHurt.getArmorSlots()) {
            if (!slot.isEmpty()) {
                playerHurt.drop(slot, false);
            }
        }
        ItemStack offhandItem = playerHurt.getOffhandItem();
        if (!offhandItem.isEmpty()) {
            playerHurt.drop(offhandItem, false);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        //干掉原来的物品介绍，就是属性
        return ImmutableMultimap.of();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }
}

