package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import com.github.alexthe666.iceandfire.entity.EntityStoneStatue;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.IBlacklistedFromStatues;
import com.github.alexthe666.iceandfire.misc.IafDamageRegistry;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import com.google.common.base.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.common.helper.ItemNBTHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class PetrifyingWand extends Item {

    private static final int MANA_PER_USE = 1000;
    private static final int MAX_MANA = 10000;
    private static final int RANGE = 32;
    private static final float DAMAGE = Float.MAX_VALUE;
    private static final int USE_DURATION = 30;
    private static final String TAG_MANA = "mana";

    public PetrifyingWand() {
        super(new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_BOTANIA)
                .durability(0)
                .setNoRepair()
                .stacksTo(1));
    }

    public static class ManaItem implements IManaItem {
        private final ItemStack stack;

        public ManaItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
        }

        @Override
        public int getMaxMana() {
            return MAX_MANA;
        }

        @Override
        public void addMana(int mana) {
                setMana(stack, Math.min(getMana() + mana, getMaxMana()));
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            return true;
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
            return false;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean isNoExport() {
            return false;
        }
    }

    protected static void setMana(ItemStack stack, int mana) {
        if (mana > 0) {
            ItemNBTHelper.setInt(stack, TAG_MANA, mana);
        } else {
            ItemNBTHelper.removeEntry(stack, TAG_MANA);
        }
    }

    public int getManaFromStack(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        return nbt.getInt(TAG_MANA);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (getMana(stack) >= MANA_PER_USE) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else if (!world.isClientSide) {
            sendLowManaWarning(player);
        }
        return InteractionResultHolder.fail(stack);
    }

    private int getMana(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || world.isClientSide) return;

        if (getUseDuration(stack) - timeLeft < 2) return;

        if (getManaFromStack(stack) < MANA_PER_USE) {
            sendLowManaWarning(player);
            return;
        }

        addMana(stack, -MANA_PER_USE);
        aaa(player, world);
        player.getCooldowns().addCooldown(stack.getItem(), 40);
    }

    private void addMana(ItemStack stack, int i) {
        int mana = getMana(stack);
        ItemNBTHelper.setInt(stack, TAG_MANA, mana + i);
    }

    private void aaa(Entity entity, Level worldIn){
        double dist = 32.0;
        Vec3 Vector3d = entity.getEyePosition(1.0F);
        Vec3 Vector3d1 = entity.getViewVector(1.0F);
        Vec3 Vector3d2 = Vector3d.add(Vector3d1.x * dist, Vector3d1.y * dist, Vector3d1.z * dist);
        double d1 = dist;
        Entity pointedEntity = null;
        List<Entity> list = worldIn.getEntities(entity, entity.getBoundingBox().expandTowards(Vector3d1.x * dist, Vector3d1.y * dist, Vector3d1.z * dist).inflate(1.0, 1.0, 1.0), new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity) {
                boolean blindness = entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(MobEffects.BLINDNESS) || entity instanceof IBlacklistedFromStatues && !((IBlacklistedFromStatues)entity).canBeTurnedToStone();
                return entity != null && entity.isPickable() && !blindness && (entity instanceof Player || entity instanceof LivingEntity && DragonUtils.isAlive((LivingEntity)entity));
            }
        });
        double d2 = d1;

        for(int j = 0; j < list.size(); ++j) {
            Entity entity1 = (Entity)list.get(j);
            AABB axisalignedbb = entity1.getBoundingBox().inflate((double)entity1.getPickRadius());
            Optional<Vec3> optional = axisalignedbb.clip(Vector3d, Vector3d2);
            if (axisalignedbb.contains(Vector3d)) {
                if (d2 >= 0.0) {
                    d2 = 0.0;
                }
            } else if (optional.isPresent()) {
                double d3 = Vector3d.distanceTo((Vec3)optional.get());
                if (d3 < d2 || d2 == 0.0) {
                    if (entity1.getRootVehicle() == entity.getRootVehicle() && !entity.canRiderInteract()) {
                        if (d2 == 0.0) {
                            pointedEntity = entity1;
                        }
                    } else {
                        pointedEntity = entity1;
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && pointedEntity instanceof LivingEntity) {
            pointedEntity.playSound(IafSoundRegistry.TURN_STONE, 1.0F, 1.0F);
            EntityStoneStatue statue = EntityStoneStatue.buildStatueEntity((LivingEntity)pointedEntity);
            if (pointedEntity instanceof Player) {
                pointedEntity.hurt(IafDamageRegistry.causeGorgonDamage(pointedEntity), 2.14748365E9F);
            } else if (!worldIn.isClientSide) {
                pointedEntity.remove(RemovalReason.KILLED);
            }

            statue.absMoveTo(pointedEntity.getX(), pointedEntity.getY(), pointedEntity.getZ(), pointedEntity.getYRot(), pointedEntity.getXRot());
            statue.yBodyRot = pointedEntity.getYRot();
            if (!worldIn.isClientSide) {
                worldIn.addFreshEntity(statue);
            }

        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world,
                                List<Component> tooltip, TooltipFlag flag) {

        tooltip.add(new TranslatableComponent("item.iceandfire.petrify_wand.desc")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(new TranslatableComponent("item.iceandfire.petrify_wand.mana",
                ItemNBTHelper.getInt(stack, TAG_MANA, 0), MAX_MANA)
                .withStyle(ChatFormatting.DARK_PURPLE));

        if (flag.isAdvanced()) {
            tooltip.add(new TranslatableComponent("", RANGE)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /**
     * 提供物品能力
     * 这个方法用于向Forge能力系统注册IManaItem能力
     */
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IManaItem> manaHandler = LazyOptional.of(() -> new ManaItem(stack));

            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
                if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                    return manaHandler.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).ifPresent(manaItem -> {
                BlockPos pos = entity.blockPosition();
                for (BlockPos checkPos : BlockPos.betweenClosed(
                        pos.offset(-2, -2, -2),
                        pos.offset(2, 2, 2))) {
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be instanceof IManaPool pool) {
                        int space = manaItem.getMaxMana() - manaItem.getMana();
                        if (space > 0 && pool.getCurrentMana() > 0) {
                            int manaToTransfer = Math.min(1000, Math.min(space, pool.getCurrentMana()));
                            pool.receiveMana(-manaToTransfer);
                            manaItem.addMana(manaToTransfer);
                            break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getManaFromStack(stack) < MAX_MANA;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getManaFromStack(stack) / (float)MAX_MANA);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x6600FF;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_DURATION;
    }

    private void sendLowManaWarning(Player player) {
        player.displayClientMessage(
                new TranslatableComponent("tooltip.iceandfire.petrify_wand.no_mana")
                        .withStyle(ChatFormatting.RED),
                true
        );
        player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5F, 0.5F);
    }

    private boolean processPetrificationAdvanced(LivingEntity entity, Level world) {
        double dist = RANGE;
        Vec3 eyePos = entity.getEyePosition(1.0F);
        Vec3 lookVec = entity.getViewVector(1.0F);
        Vec3 targetVec = eyePos.add(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist);

        Entity pointedEntity = null;
        List<Entity> list = world.getEntities(entity,
                entity.getBoundingBox().expandTowards(lookVec.x * dist, lookVec.y * dist, lookVec.z * dist)
                        .inflate(1.0F, 1.0F, 1.0F),
                (Predicate<Entity>) entity1 -> {
                    boolean isBlacklisted = entity1 instanceof IBlacklistedFromStatues
                            && !((IBlacklistedFromStatues) entity1).canBeTurnedToStone();
                    boolean hasBlindness = entity1 instanceof LivingEntity
                            && ((LivingEntity) entity1).hasEffect(MobEffects.BLINDNESS);

                    return entity1 != null
                            && entity1.isPickable()
                            && !hasBlindness
                            && !isBlacklisted
                            && (entity1 instanceof Player
                            || (entity1 instanceof LivingEntity living && living.isAlive()));
                });

        double closestDistance = dist;

        for (Entity potentialTarget : list) {
            AABB boundingBox = potentialTarget.getBoundingBox().inflate(potentialTarget.getPickRadius());
            Optional<Vec3> rayTraceResult = boundingBox.clip(eyePos, targetVec);

            if (boundingBox.contains(eyePos)) {
                if (closestDistance >= 0.0F) {
                    closestDistance = 0.0F;
                }
            } else if (rayTraceResult.isPresent()) {
                double hitDistance = eyePos.distanceTo(rayTraceResult.get());
                if (hitDistance < closestDistance || closestDistance == 0.0F) {
                    if (potentialTarget.getRootVehicle() == entity.getRootVehicle() && !entity.canRiderInteract()) {
                        if (closestDistance == 0.0F) {
                            pointedEntity = potentialTarget;
                        }
                    } else {
                        pointedEntity = potentialTarget;
                        closestDistance = hitDistance;
                    }
                }
            }
        }

        if (pointedEntity != null) {
            petrifyEntity(pointedEntity, world, entity instanceof Player ? (Player) entity : null);
            world.playSound(null, pointedEntity.blockPosition(),
                    IafSoundRegistry.TURN_STONE, SoundSource.PLAYERS,
                    1.5F, 1.0F);
            return true;
        }

        return false;
    }

    private void petrifyEntity(Entity target, Level world, @Nullable Player player) {
        if (target instanceof Player) {
            target.hurt(IafDamageRegistry.causeGorgonDamage(player), DAMAGE);
        } else if (target instanceof LivingEntity living) {
            if (!world.isClientSide) {
                EntityStoneStatue statue = EntityStoneStatue.buildStatueEntity(living);
                statue.absMoveTo(target.getX(), target.getY(), target.getZ(),
                        target.getYRot(), target.getXRot());
                statue.setCustomName(target.getCustomName());
                world.addFreshEntity(statue);
                target.discard();
            }
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return itemStack.copy();
    }
}