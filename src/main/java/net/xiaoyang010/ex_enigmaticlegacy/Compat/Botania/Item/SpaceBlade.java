package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.api.AdvancedBotanyAPI;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.ILensEffect;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.entity.EntityManaBurst;
import vazkii.botania.common.handler.ModSounds;

import java.util.List;
import java.util.UUID;

public class SpaceBlade extends SwordItem implements IManaItem, ILensEffect {
    private static final String NBT_MANA = "Mana";
    private static final String NBT_LEVEL = "Level";
    private static final int[] CREATIVE_MANA = new int[]{0, 10000, 1000000, 10000000, 100000000, 1000000000, 2147483646}; //各等级最小魔力值

    public SpaceBlade(Properties properties) {
        super(AdvancedBotanyAPI.MITHRIL_ITEM_TIER, 3, -2.4F, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player) {
            BlockPos pos = player.getOnPos();
            if (getManaTag(stack) >= CREATIVE_MANA[getLevel(stack) + 1]){
                setLevel(stack, getLevel(stack) + 1);
                world.playSound(player, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (world.isClientSide) player.displayClientMessage(new TextComponent("魔力达上限！等级提升为:" + getLevel(stack)), false);
            }

            for (BlockPos blockPos : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 1, 2))) {
                BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity instanceof TilePool pool){ //从魔力池补充
                    int mana = pool.getCurrentMana();
                    int manaTag = getManaTag(stack);
                    int level = getLevel(stack);
                    int addMana = 1024 * (level + 1);
                    if (mana > addMana){
                        setManaTag(stack, manaTag + addMana);
                        pool.receiveMana(-addMana);
                    }
                }
            }

        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag isadvanced) {
        int lv = getLevel(stack);
        components.add(new TextComponent("等级：" + lv));
        components.add(new TextComponent("魔力：" + getManaTag(stack)));
        TooltipHandler.addOnShift(components, () -> {
            components.add(new TranslatableComponent("ex_enigmaticlegacy.swordInfo.1")
                    .withStyle(lv >= 1 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            components.add(new TranslatableComponent("ex_enigmaticlegacy.swordInfo.2")
                    .withStyle(lv >= 3 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            components.add(new TranslatableComponent("ex_enigmaticlegacy.swordInfo.LEVEL", Math.max(lv, 3))
                    .withStyle(lv >= 5 ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        });
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (this.allowdedIn(tab)) {
            for (int level = 1; level < 7; level++) {
                ItemStack stack = new ItemStack(this);
                setLevel(stack, level);
                setManaTag(stack, CREATIVE_MANA[level]);
                list.add(stack);
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && !player.level.isClientSide) {
            if (getLevel(stack) >= 3) {
                float size = getLevel(stack) >= 4 ? (getLevel(stack) >= 5 ? 3.5F : 2.5F) : 1.5F;
                AABB aabb = target.getBoundingBox().inflate(size, 1.7F, size);

                for (LivingEntity living : player.level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                    if (living.isAlive() && living != attacker) {
                        living.hurt(DamageSource.playerAttack(player), getSwordDamage(stack));
                    }
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()){
            trySpawnBurst(player);
            setManaTag(stack, getManaTag(stack) - (int) Math.floor((getSwordDamage(stack) * 100)));
            if (getManaTag(stack) <= CREATIVE_MANA[getLevel(stack)]){
                setLevel(stack, getLevel(stack) - 1);
                level.playSound(player, player.getOnPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (level.isClientSide) player.displayClientMessage(new TranslatableComponent("魔力不足！等级掉为:" + getLevel(stack)), false);
            }

        }else {
            Vec3 scale = player.getLookAngle().scale(3.0d);
            level.playSound(player, player.getOnPos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.setDeltaMovement(scale);

            player.getCooldowns().addCooldown(stack.getItem(), 20);
        }
        return InteractionResultHolder.success(stack);
    }

    /**
     * 发射魔力光束
     */
    public static void trySpawnBurst(Player player) {
        EntityManaBurst burst = getBurst(player, player.getMainHandItem());
        player.level.addFreshEntity(burst);
        player.getMainHandItem().hurtAndBreak(1, player, (p) -> {
            p.broadcastBreakEvent(InteractionHand.MAIN_HAND);
        });
        player.level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.terraBlade, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static EntityManaBurst getBurst(Player player, ItemStack stack) {
        EntityManaBurst burst = new EntityManaBurst(player);
        float motionModifier = 7.0F;
        burst.setColor(2165484);
        burst.setMana(1024);
        burst.setStartingMana(1024);
        burst.setMinManaLoss(40);
        burst.setManaLossPerTick(4.0F);
        burst.setGravity(0.0F);
        burst.setDeltaMovement(burst.getDeltaMovement().scale((double)motionModifier));
        burst.setSourceLens(stack.copy());
        return burst;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (slot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getSwordDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.MOVEMENT_SPEED,
                    new AttributeModifier(UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"), "Weapon speed", 0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        return multimap;
    }

    /**
     * 根据等级获取范围攻击伤害
     */
    private float getSwordDamage(ItemStack stack) {
        int level = getLevel(stack);
        float baseDamage = getTier().getAttackDamageBonus(); //5.0f
        return 5.0F + baseDamage + (level * level * level * 10 + level * 10);
    }

    public int getManaTag(ItemStack stack){
        return stack.getOrCreateTag().getInt(NBT_MANA);
    }

    public void setManaTag(ItemStack stack, int mana){
        stack.getOrCreateTag().putInt(NBT_MANA, mana);
    }

    public int getLevel(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_LEVEL);
    }

    public void setLevel(ItemStack stack, int level) {
        stack.getOrCreateTag().putInt(NBT_LEVEL, level);
    }

    @Override
    public int getMana() {
        return getManaTag(new ItemStack(this));
    }

    @Override
    public int getMaxMana() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void addMana(int i) {
//        setManaTag(new ItemStack(this), i + getMana());
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canExportManaToPool(BlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean canExportManaToItem(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isNoExport() {
        return false;
    }

    @Override
    public void apply(ItemStack itemStack, BurstProperties burstProperties, Level level) {

    }

    @Override
    public boolean collideBurst(IManaBurst iManaBurst, HitResult hitResult, boolean b, boolean b1, ItemStack itemStack) {
        return b1;
    }

    @Override
    public void updateBurst(IManaBurst burst, ItemStack itemStack) {
        ThrowableProjectile entity = burst.entity();
        AABB axis = (new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.xOld, entity.yOld, entity.zOld)).inflate(1.0);
        List<LivingEntity> entities = entity.level.getEntitiesOfClass(LivingEntity.class, axis);
        Entity thrower = entity.getOwner();

        for (LivingEntity living : entities) {
            if (living != thrower) {
                if (living instanceof Player livingPlayer) {
                    if (thrower instanceof Player throwingPlayer) {
                        if (!throwingPlayer.canHarmPlayer(livingPlayer)) {
                            continue;
                        }
                    }
                }

                if (living.hurtTime == 0) {
                    int cost = 33;
                    int mana = burst.getMana();
                    if (mana >= cost) {
                        burst.setMana(mana - cost);
                        float damage = getSwordDamage(itemStack);
                        if (!burst.isFake() && !entity.level.isClientSide) {
                            DamageSource source = DamageSource.MAGIC;
                            if (thrower instanceof Player player) {
                                source = DamageSource.playerAttack(player);
                            } else if (thrower instanceof LivingEntity livingEntity) {
                                source = DamageSource.mobAttack(livingEntity);
                            }

                            living.hurt(source, damage);
                            entity.discard();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean doParticles(IManaBurst iManaBurst, ItemStack itemStack) {
        return true;
    }
}