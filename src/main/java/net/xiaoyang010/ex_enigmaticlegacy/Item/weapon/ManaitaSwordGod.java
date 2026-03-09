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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityRainBowLightningBlot;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.Xingyun2825Entity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ColorText;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ManaitaSwordGod extends SwordItem {

    public ManaitaSwordGod() {
        super(EXEAPI.MIRACLE_ITEM_TIER, 200, -2.4F, new Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level.isClientSide) {
            target.hurt(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
            target.setHealth(0.0F);

            if (target instanceof Player player) {
                player.getFoodData().setFoodLevel(0);
                player.experienceLevel = 0;
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        return Component.nullToEmpty(ColorText.GetColor1(I18n.get("item.ex_enigmaticlegacy.manaita_sword")));
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
            double radius = 180000.0;

            AABB boundingBox = new AABB(playerPos.subtract(radius, radius, radius), playerPos.add(radius, radius, radius));
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox,
                    entity -> !(entity instanceof Player));

            // 对每个非玩家生物召唤彩色雷电并杀死它们
            for (LivingEntity entity : entities) {
                if (entity instanceof Monster) {
                    createRainLightning_blot(entity.getX(), entity.getY(), entity.getZ(), level);
                    // 使用雷电伤害源杀死生物
                    entity.die(DamageSource.mobAttack(player));
                    if (entity instanceof Player playerHurt)
                        dropItem(playerHurt);
                    entity.hurt(DamageSource.LIGHTNING_BOLT, Float.MAX_VALUE);
                    entity.setHealth(0.0F);
                }
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
                } else if (entity instanceof Xingyun2825Entity xingyun2825Entity) {
                    xingyun2825Entity.costomDie();
                } else living.hurt(new InfinityDamageSource(player), Float.POSITIVE_INFINITY);
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