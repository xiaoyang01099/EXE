package net.xiaoyang010.ex_enigmaticlegacy.Item.armor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModArmors;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

import java.awt.Color;

public class NebulaArmorHelper {
    private static final String TAG_LAST_HEAL_TIME = "lastHealTime";
    private static final int HEAL_COOLDOWN = 80; // 4秒 (80 ticks)
    private static final float FLIGHT_SPEED = 0.25F;
    private static final float DEFAULT_FLIGHT_SPEED = 0.05F;



    public static boolean shouldPlayerHaveStepup(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);

        return !boots.isEmpty() && boots.getItem() == ModArmors.NEBULA_BOOTS.get() &&
                !legs.isEmpty() && legs.getItem() == ModArmors.NEBULA_LEGGINGS.get();
    }

    public static boolean hasNebulaArmor(Player player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

        boolean hasHead = !head.isEmpty() &&
                (head.getItem() == ModArmors.NEBULA_HELMET.get() ||
                        head.getItem() == ModArmors.NEBULA_HELMET_REVEAL.get());

        boolean hasChest = !chest.isEmpty() && chest.getItem() == ModArmors.NEBULA_CHESTPLATE.get();
        boolean hasLegs = !legs.isEmpty() && legs.getItem() == ModArmors.NEBULA_LEGGINGS.get();
        boolean hasFeet = !feet.isEmpty() && feet.getItem() == ModArmors.NEBULA_BOOTS.get();

        return hasHead && hasChest && hasLegs && hasFeet;
    }

    public static boolean isNebulaArmor(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof NebulaArmor;
    }

    public static boolean hasNebulaArmorPiece(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) return false;

        return switch (slot) {
            case HEAD -> stack.getItem() == ModArmors.NEBULA_HELMET.get() ||
                    stack.getItem() == ModArmors.NEBULA_HELMET_REVEAL.get();
            case CHEST -> stack.getItem() == ModArmors.NEBULA_CHESTPLATE.get();
            case LEGS -> stack.getItem() == ModArmors.NEBULA_LEGGINGS.get();
            case FEET -> stack.getItem() == ModArmors.NEBULA_BOOTS.get();
            default -> false;
        };
    }

    public static int getNebulaArmorCount(Player player) {
        int count = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR && hasNebulaArmorPiece(player, slot)) {
                count++;
            }
        }
        return count;
    }

    public static void foodToHeal(Player player) {
        if (player.level.isClientSide()) return;

        FoodData foodData = player.getFoodData();
        int food = foodData.getFoodLevel();
        float saturation = foodData.getSaturationLevel();

        long currentTime = player.level.getGameTime();
        long lastHealTime = player.getPersistentData().getLong(TAG_LAST_HEAL_TIME);

        if (currentTime - lastHealTime < HEAL_COOLDOWN) {
            return;
        }

        if (food >= 16 && saturation > 2.0F && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0F);
            foodData.setSaturation(saturation - 2.0F);
            player.getPersistentData().putLong(TAG_LAST_HEAL_TIME, currentTime);
        }
        else if (food >= 10 && food < 16 && saturation > 1.0F &&
                player.getHealth() < player.getMaxHealth() &&
                currentTime - lastHealTime >= HEAL_COOLDOWN * 2) {
            player.heal(0.5F);
            foodData.setSaturation(saturation - 1.0F);
            player.getPersistentData().putLong(TAG_LAST_HEAL_TIME, currentTime);
        }
    }

    public static boolean dispatchManaExact(ItemStack source, Player player, int manaToSend, boolean transfer) {
        if (source.isEmpty() || manaToSend <= 0) {
            return false;
        }

        if (source.getItem() instanceof IManaItem sourceManaItem) {
            if (sourceManaItem.getMana() < manaToSend) {
                return false;
            }
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stackInSlot = inventory.getItem(i);
            if (tryTransferMana(source, stackInSlot, manaToSend, transfer)) {
                return true;
            }
        }

        try {
            return CuriosApi.getCuriosHelper().findCurios(player, itemStack ->
                            itemStack.getItem() instanceof IManaItem &&
                                    ((IManaItem) itemStack.getItem()).canReceiveManaFromItem(itemStack))
                    .stream()
                    .map(SlotResult::stack)
                    .anyMatch(curioStack -> tryTransferMana(source, curioStack, manaToSend, transfer));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean tryTransferMana(ItemStack source, ItemStack target, int manaToSend, boolean transfer) {
        if (target.isEmpty() || target.is(source.getItem())) {
            return false;
        }

        if (!(target.getItem() instanceof IManaItem targetManaItem)) {
            return false;
        }

        if (!targetManaItem.canReceiveManaFromItem(target)) {
            return false;
        }

        int currentMana = targetManaItem.getMana();
        int maxMana = targetManaItem.getMaxMana();
        int availableSpace = maxMana - currentMana;

        if (availableSpace <= 0) {
            return false;
        }

        int actualTransfer = Math.min(manaToSend, availableSpace);

        if (transfer && actualTransfer > 0) {
            if (source.getItem() instanceof IManaItem sourceManaItem) {
                sourceManaItem.addMana(-actualTransfer);
            }

            targetManaItem.addMana(actualTransfer);
        }

        return actualTransfer > 0;
    }

    public static boolean requestManaFromNetwork(ItemStack stack, Player player, int manaRequested, boolean consume) {
        return ManaItemHandler.instance().requestManaExactForTool(stack, player, manaRequested, consume);
    }

    public static float getArmorSetEffectiveness(Player player) {
        return getNebulaArmorCount(player) / 4.0F;
    }

    public static void applyArmorSetEffects(Player player) {
        if (player.level.isClientSide()) return;

        int armorCount = getNebulaArmorCount(player);

        switch (armorCount) {
            case 4: // 全套护甲
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 220, 0, true, false));
                // 继续执行3件套效果
            case 3: // 3件套
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 220, 0, true, false));
                // 继续执行2件套效果
            case 2: // 2件套
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 220, 0, true, false));
                break;
            default:
                break;
        }
    }

    public static Color getArmorColor(float time) {
        float hue = (time / 20.0F) % 360.0F / 360.0F;
        return Color.getHSBColor(hue, 0.8F, 1.0F);
    }

    public static float getArmorEfficiency(ItemStack stack) {
        if (!(stack.getItem() instanceof NebulaArmor armor)) {
            return 0.0F;
        }

        int damage = armor.getDamage(stack);
        return 1.0F - (float) damage / 1000.0F;
    }

    public static boolean canFly(Player player) {
        return hasNebulaArmorPiece(player, EquipmentSlot.FEET) &&
                getNebulaArmorCount(player) >= 2; // 至少需要2件护甲
    }

    public static void updateFlightAbility(Player player) {
        if (player.level.isClientSide()) return;

        boolean canFly = canFly(player);
        boolean isFlying = player.getAbilities().flying;

        if (canFly) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }

            // 调整飞行速度
            float targetSpeed = hasNebulaArmorPiece(player, EquipmentSlot.FEET) ? FLIGHT_SPEED : DEFAULT_FLIGHT_SPEED;
            if (Math.abs(player.getAbilities().getFlyingSpeed() - targetSpeed) > 0.001F) {
                player.getAbilities().setFlyingSpeed(targetSpeed);
                player.onUpdateAbilities();
            }
        } else {
            if (player.getAbilities().mayfly && !player.isCreative()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.getAbilities().setFlyingSpeed(DEFAULT_FLIGHT_SPEED);
                player.onUpdateAbilities();
            }
        }
    }

    public static ItemStack findManaItem(Player player, boolean needsSpace) {
        // 优先检查主手和副手
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (isSuitableManaItem(mainHand, needsSpace)) return mainHand;
        if (isSuitableManaItem(offHand, needsSpace)) return offHand;

        // 检查物品栏
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (isSuitableManaItem(stack, needsSpace)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isSuitableManaItem(ItemStack stack, boolean needsSpace) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IManaItem manaItem)) {
            return false;
        }

        if (needsSpace) {
            return manaItem.canReceiveManaFromItem(stack) &&
                    manaItem.getMana() < manaItem.getMaxMana();
        } else {
            return manaItem.getMana() > 0;
        }
    }
}