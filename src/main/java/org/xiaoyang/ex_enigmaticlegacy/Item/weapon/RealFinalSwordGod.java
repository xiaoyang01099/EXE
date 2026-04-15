package org.xiaoyang.ex_enigmaticlegacy.Item.weapon;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.EntityRainBowLightningBlot;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;
import org.xiaoyang.ex_enigmaticlegacy.Util.ColorText;
import org.xiaoyang.ex_enigmaticlegacy.api.EXEAPI;

import java.util.List;

public class RealFinalSwordGod extends SwordItem {

    public RealFinalSwordGod() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 10, -2.4F, new Properties());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            target.hurt(target.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
            target.setHealth(0.0F);

            if (target instanceof Player player) {
                player.getFoodData().setFoodLevel(0);
                player.experienceLevel = 0;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.nullToEmpty(ColorText.GetColor1(I18n.get("item.ex_enigmaticlegacy.real_final_sword")));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.nullToEmpty(""));
        tooltip.add(Component.nullToEmpty(ColorText.getGray(I18n.get("text.ex_enigmaticlegacy.manaita_sword.hand"))));
        tooltip.add(Component.nullToEmpty(ColorText.GetColor1(I18n.get("text.ex_enigmaticlegacy.manaita_sword.infinity"))
                + ColorText.GetGreen(I18n.get("text.ex_enigmaticlegacy.manaita_sword.damage"))));
        tooltip.add(Component.nullToEmpty(ColorText.GetGreen(I18n.get("text.ex_enigmaticlegacy.manaita_sword.speed"))));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 playerPos = player.position();
            double radius = 180000.0;

            AABB boundingBox = new AABB(playerPos.subtract(radius, radius, radius), playerPos.add(radius, radius, radius));
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox,
                    entity -> !(entity instanceof Player));

            for (LivingEntity entity : entities) {
                createRainLightning_blot(entity.getX(), entity.getY(), entity.getZ(), level);
                if (entity instanceof Player playerHurt)
                    dropItem(playerHurt);
                entity.hurt(entity.damageSources().lightningBolt(), Float.MAX_VALUE);
                entity.setHealth(0.0F);
                entity.discard();
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
            entityToSpawn.moveTo(Vec3.atBottomCenterOf(new BlockPos((int) x, (int) y, (int) z)));
            entityToSpawn.setVisualOnly(true);
            _level.addFreshEntity(entityToSpawn);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide()) {
            if (entity instanceof LivingEntity living) {
                if (entity instanceof Player playerHurt) {
                    playerHurt.hurt(player.damageSources().generic(), Integer.MAX_VALUE);
                    dropItem(playerHurt);
                } else {
                    living.hurt(player.damageSources().generic(), Float.POSITIVE_INFINITY);
                }
                living.setHealth(-1.0f);
                if (living.isAlive()) {
                    living.die(player.damageSources().generic());
                    living.kill();
                    living.deathTime = 0;
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    private void dropItem(Player playerHurt) {
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