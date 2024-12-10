package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.EntityRainBowLightningBlot;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ColorText;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public class ManaitaSwordGod extends SwordItem {

    public ManaitaSwordGod() {
        super(Tiers.NETHERITE, 10, -2.4F, new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR));
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
        return Component.nullToEmpty(ColorText.GetColor1("砧板之刃[神]"));
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> tooltip, TooltipFlag p_41424_) {
        tooltip.add(Component.nullToEmpty(""));
        tooltip.add(Component.nullToEmpty(ColorText.getGray("在主手时:")));
        tooltip.add(Component.nullToEmpty(ColorText.GetColor1("无限 ") + ColorText.GetGreen("攻击伤害")));
        tooltip.add(Component.nullToEmpty(ColorText.GetGreen("1.6 攻击速度")));
        super.appendHoverText(p_41421_, p_41422_, tooltip, p_41424_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
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
                dropItem(entity, 10, true);
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
        // 自定义攻击距离，模拟无限攻击距离
        double maxDistance = 180000.0; // 设置一个极大的攻击距离
        Vec3 playerPos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();
        Vec3 reachVec = playerPos.add(lookVec.scale(maxDistance));

        // 定义一个 AABB 边界框，使用扩展的 reachVec
        AABB boundingBox = new AABB(playerPos, reachVec);
        List<LivingEntity> entities = player.level.getEntitiesOfClass(LivingEntity.class, boundingBox,
                e -> e != player && e.distanceTo(player) <= maxDistance);

        for (LivingEntity entity1 : entities) {
            dropItem(entity1, 1, true);
            dropItem(entity1, 1, true);
            dropItem(entity1, 1, true);
            entity1.die(DamageSource.mobAttack(player));
            entity1.setHealth(0.0F);
            entity1.setRemoved(Entity.RemovalReason.KILLED);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

//选中点到的实体（原来的）
    //@Override
    //public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
    //if (entity instanceof  LivingEntity entity1){
    //dropItem(entity1,1,true);
    //dropItem(entity1,1,true);
    //dropItem(entity1,1,true);
    //entity1.die(DamageSource.mobAttack(player));
    //entity1.setHealth(0);//直接设置血量
    //entity1.setRemoved(Entity.RemovalReason.KILLED);//这样会掉落物品,移除实体
    //先看看能不能运行
    //移到前面试试
    //}
    //return super.onLeftClickEntity(stack, player, entity);
    //}

    private static void dropItem(LivingEntity mob, int i, boolean b) {
        Class c = mob.getClass();
        try {
            //这里有一点需要主要
            //m_7472_(Lnet/minecraft/world/damagesource/DamageSource;IZ)V dropCustomDeathLoot
            //是方法混淆名，在作为一个模组加入游戏时用的就是这个
            //现在是测试，方法经过反混淆，所以直接复制就行
            //导出时记得改成混淆名:m_7472_
            Method method = c.getDeclaredMethod("m_7472_");
            method.invoke(mob, i, b);
        } catch (Exception e) {
            e.printStackTrace();
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