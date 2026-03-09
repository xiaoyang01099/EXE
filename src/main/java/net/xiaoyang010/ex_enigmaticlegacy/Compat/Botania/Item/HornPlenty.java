package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.CatMewEntity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModSounds;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputPacket.HornChargeHudPacket;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;

import java.lang.reflect.Method;
import java.util.List;

public class HornPlenty extends Item {
    private static final short MAX_CHARGE_LOOT = 16;
    private static final int MANA_COST = 64000;
    private static final int MAX_USE_DURATION = 42000;
    private static final int USE_THRESHOLD = 48;
    private static Method dropFromLootTableMethod;

    static {
        try {
            dropFromLootTableMethod = LivingEntity.class.getDeclaredMethod(
                    "dropFromLootTable",
                    DamageSource.class,
                    boolean.class
            );
            dropFromLootTableMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HornPlenty(Properties properties) {
        super(properties.stacksTo(1));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide && entity instanceof Player player) {
            short chargeLoot = this.getChargeLoot(stack);
            short lastChargeLoot = this.getLastChargeLoot(stack);

            if (lastChargeLoot != chargeLoot) {
                this.setLastChargeLoot(stack, chargeLoot);
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHandler.sendToPlayer(new HornChargeHudPacket(chargeLoot), serverPlayer);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!this.hasChargeLoot(stack) &&
                ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int timeUsed = this.getUseDuration(stack) - remainingUseDuration;

        if (timeUsed > USE_THRESHOLD) {
            if (!level.isClientSide &&
                    ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {
                this.setChargeLoot(stack, MAX_CHARGE_LOOT);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.2F, 4.0F);
            }
            player.stopUsingItem();
        } if (level.isClientSide) {
            player.playSound(ModSounds.HORN_PLENTY, 2.4f, 2.47f);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return this.hasChargeLoot(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * (1.0F - (float)this.getChargeLoot(stack) / MAX_CHARGE_LOOT));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF00;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_DURATION;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (event == null || event.getSource() == null) {
            return;
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof Player player)) {
            return;
        }

        ItemStack horn = null;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof HornPlenty && this.hasChargeLoot(stack)) {
                horn = stack;
                break;
            }
        }

        if (!player.level.isClientSide && horn != null && this.hasChargeLoot(horn)) {
            LivingEntity victim = (LivingEntity) event.getEntity();

            if (victim != null && !isBossEntity(victim) && isValidEntity(victim)) {
                if (player.level.random.nextInt(100) < 20) {
                    try {
                        if (dropFromLootTableMethod != null) {
                            dropFromLootTableMethod.invoke(victim, event.getSource(), true);
                        }

                        this.setChargeLoot(horn, (short)(this.getChargeLoot(horn) - 1));

                        player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.9F, 0.8F);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void setLastChargeLoot(ItemStack stack, short count) {
        ItemNBTHelper.setShort(stack, "lastChargeLoot", count);
    }

    public short getLastChargeLoot(ItemStack stack) {
        return ItemNBTHelper.getShort(stack, "lastChargeLoot", (short)0);
    }

    public void setChargeLoot(ItemStack stack, short count) {
        ItemNBTHelper.setShort(stack, "chargeLoot", count);
    }

    public short getChargeLoot(ItemStack stack) {
        return ItemNBTHelper.getShort(stack, "chargeLoot", (short)0);
    }

    public boolean hasChargeLoot(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, "chargeLoot", 0) > 0;
    }

    private static boolean isBossEntity(LivingEntity entity) {
        return entity instanceof WitherBoss
                || entity instanceof EnderDragon
                || entity instanceof CatMewEntity
                || !entity.canChangeDimensions();
    }

    public static boolean isValidEntity(LivingEntity entity) {
        List<Object> lockList = getEntityLockList();

        String entityClassName = entity.getClass().getSimpleName();
        for (Object lockedName : lockList) {
            if (entityClassName.equals(lockedName)) {
                return false;
            }
        }
        return true;
    }

    private static List<Object> getEntityLockList() {
        return ConfigHandler.lockEntityListToHorn;
    }
}
