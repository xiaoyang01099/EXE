package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityShinyEnergy;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Projecte.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;

import javax.annotation.Nullable;
import java.util.List;

public class ShinyStone extends Item implements ICurioItem, INoEMCItem {
    private static final int SHINY_STONE_CHECKRATE = 4;

    public ShinyStone(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new ShinyStone.RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemShinyStone1.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemShinyStone2.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.nullToEmpty(""));

            boolean isPermanentDay = ItemNBTHelper.getBoolean(stack, "PermanentDay", false);
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public void spawnEnergyParticle(LivingEntity entity) {
        EntityShinyEnergy energy = new EntityShinyEnergy(entity.level, entity, entity,
                entity.getX(), entity.getY(), entity.getZ());

        Vector3 position = Vector3.fromEntityCenter(entity);
        Vector3 motVec = new Vector3(
                (Math.random() - 0.5D) * 3.0D,
                (Math.random() - 0.5D) * 3.0D,
                (Math.random() - 0.5D) * 3.0D);
        position.add(motVec);
        motVec.normalize().negate().multiply(0.1D);

        energy.setPos(position.x, position.y, position.z);
        energy.setDeltaMovement(motVec.x, motVec.y, motVec.z);
        entity.level.addFreshEntity(energy);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();

        if (entity instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }
        }

        if (!entity.level.isClientSide && entity.tickCount % SHINY_STONE_CHECKRATE == 0) {
            double posX = ItemNBTHelper.getDouble(stack, "LastX", 0.0D);
            double posY = ItemNBTHelper.getDouble(stack, "LastY", 0.0D);
            double posZ = ItemNBTHelper.getDouble(stack, "LastZ", 0.0D);

            int Static = ItemNBTHelper.getInt(stack, "Static", 0);

            if (entity.getX() == posX && entity.getY() == posY && entity.getZ() == posZ) {
                int particleNumber = 3;
                ItemNBTHelper.setInt(stack, "HealRate", 1);

                if (Static >= 40) {
                    ItemNBTHelper.setInt(stack, "HealRate", 2);
                    particleNumber = 2;
                }
                if (Static >= 80) {
                    ItemNBTHelper.setInt(stack, "HealRate", 3);
                    particleNumber = 1;
                }
                if (Static >= 200) {
                    ItemNBTHelper.setInt(stack, "HealRate", 4);
                    particleNumber = 0;
                }

                for (int counter = particleNumber; counter <= 3; counter++) {
                    this.spawnEnergyParticle(entity);
                }

                ItemNBTHelper.setInt(stack, "Static", Static + 4);
            } else {
                ItemNBTHelper.setInt(stack, "Static", 0);
                ItemNBTHelper.setInt(stack, "HealRate", 0);
            }

            ItemNBTHelper.setDouble(stack, "LastX", entity.getX());
            ItemNBTHelper.setDouble(stack, "LastY", entity.getY());
            ItemNBTHelper.setDouble(stack, "LastZ", entity.getZ());
        }

        if (!entity.level.isClientSide) {
            int healRate = ItemNBTHelper.getInt(stack, "HealRate", 0);
            int healCheckrate = Math.max(1, SHINY_STONE_CHECKRATE / 4); // 防止除零

            if (healRate == 1 && entity.tickCount % (10 * healCheckrate) == 0) {
                entity.heal(1.0F);
            } else if (healRate == 2 && entity.tickCount % (5 * healCheckrate) == 0) {
                entity.heal(1.0F);
            } else if (healRate == 3 && entity.tickCount % (2 * healCheckrate) == 0) {
                entity.heal(1.0F);
            } else if (healRate == 4 && entity.tickCount % healCheckrate == 0) {
                entity.heal(1.0F);
            }
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && !relic.isRightPlayer(player)) {
                    return;
                }
            }
        }

        ItemNBTHelper.setInt(stack, "Static", 0);
        ItemNBTHelper.setInt(stack, "HealRate", 0);
        ItemNBTHelper.setDouble(stack, "LastX", 0.0D);
        ItemNBTHelper.setDouble(stack, "LastY", 0.0D);
        ItemNBTHelper.setDouble(stack, "LastZ", 0.0D);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        ItemNBTHelper.removeEntry(stack, "Static");
        ItemNBTHelper.removeEntry(stack, "HealRate");
        ItemNBTHelper.removeEntry(stack, "LastX");
        ItemNBTHelper.removeEntry(stack, "LastY");
        ItemNBTHelper.removeEntry(stack, "LastZ");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        if (player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                boolean currentState = ItemNBTHelper.getBoolean(stack, "PermanentDay", false);
                boolean newState = !currentState;

                ItemNBTHelper.setBoolean(stack, "PermanentDay", newState);

                if (newState) {
                    world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, world.getServer());
                    player.displayClientMessage(
                            new TranslatableComponent("item.daynight.controller1")
                                    .withStyle(ChatFormatting.GOLD),
                            true
                    );
                } else {
                    world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, world.getServer());
                    player.displayClientMessage(
                            new TranslatableComponent("item.daynight.controller2")
                                    .withStyle(ChatFormatting.AQUA),
                            true
                    );
                }
            }

            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }
}