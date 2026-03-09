package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class ShinyStone extends Item implements ICurioItem, INoEMCItem {
    private static final int SHINY_STONE_CHECKRATE = 4;

    public ShinyStone(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
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
            if (isPermanentDay) {
                tooltip.add(new TranslatableComponent("item.shinystone.mode.day").withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.add(new TranslatableComponent("item.shinystone.mode.normal").withStyle(ChatFormatting.AQUA));
            }

            tooltip.add(new TranslatableComponent("item.shinystone.key_hint").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public void spawnEnergyParticle(LivingEntity entity) {
        if (entity.level.isClientSide) return;

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

    private boolean isValidPlayer(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return true;
        }

        var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
        if (relic == null) {
            return true;
        }

        return relic.isRightPlayer(player);
    }

    public void togglePermanentDay(ItemStack stack, Player player) {
        Level level = player.level;

        if (level.isClientSide) {
            return;
        }

        if (!isValidPlayer(stack, player)) {
            return;
        }

        boolean currentState = ItemNBTHelper.getBoolean(stack, "PermanentDay", false);
        boolean newState = !currentState;

        ItemNBTHelper.setBoolean(stack, "PermanentDay", newState);

        if (level.getServer() != null) {
            if (newState) {
                level.getServer().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, level.getServer());
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.setDayTime(6000);
                }
                player.displayClientMessage(
                        new TranslatableComponent("item.daynight.controller1")
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
            } else {
                level.getServer().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, level.getServer());
                player.displayClientMessage(
                        new TranslatableComponent("item.daynight.controller2")
                                .withStyle(ChatFormatting.AQUA),
                        true
                );
            }
        }
    }

    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Level level = entity.level;

        if (level.isClientSide) {
            return;
        }

        if (entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);

                if (!relic.isRightPlayer(player)) {
                    return;
                }
            }
        }

        if (entity.tickCount % SHINY_STONE_CHECKRATE == 0) {
            double lastX = ItemNBTHelper.getDouble(stack, "LastX", entity.getX());
            double lastY = ItemNBTHelper.getDouble(stack, "LastY", entity.getY());
            double lastZ = ItemNBTHelper.getDouble(stack, "LastZ", entity.getZ());

            double currentX = entity.getX();
            double currentY = entity.getY();
            double currentZ = entity.getZ();

            int staticTicks = ItemNBTHelper.getInt(stack, "Static", 0);

            boolean isStatic = Math.abs(currentX - lastX) < 0.1D &&
                    Math.abs(currentY - lastY) < 0.1D &&
                    Math.abs(currentZ - lastZ) < 0.1D;

            if (isStatic) {
                int healRate;
                int particleNumber;

                if (staticTicks >= 100) {
                    healRate = 5;
                    particleNumber = 0;
                } else if (staticTicks >= 60) {
                    healRate = 4;
                    particleNumber = 1;
                } else if (staticTicks >= 30) {
                    healRate = 3;
                    particleNumber = 2;
                } else if (staticTicks >= 15) {
                    healRate = 2;
                    particleNumber = 3;
                } else {
                    healRate = 1;
                    particleNumber = 3;
                }

                ItemNBTHelper.setInt(stack, "HealRate", healRate);

                for (int counter = particleNumber; counter <= 3; counter++) {
                    this.spawnEnergyParticle(entity);
                }

                ItemNBTHelper.setInt(stack, "Static", staticTicks + SHINY_STONE_CHECKRATE);
            } else {
                ItemNBTHelper.setInt(stack, "Static", 0);
                ItemNBTHelper.setInt(stack, "HealRate", 0);
            }

            ItemNBTHelper.setDouble(stack, "LastX", currentX);
            ItemNBTHelper.setDouble(stack, "LastY", currentY);
            ItemNBTHelper.setDouble(stack, "LastZ", currentZ);
        }

        int healRate = ItemNBTHelper.getInt(stack, "HealRate", 0);
        if (healRate > 0 && entity.getHealth() < entity.getMaxHealth()) {
            int healInterval = switch (healRate) {
                case 1 -> 30;
                case 2 -> 15;
                case 3 -> 10;
                case 4 -> 6;
                case 5 -> 3;
                default -> 30;
            };

            if (entity.tickCount % healInterval == 0) {
                entity.heal(1.0F);
            }
        }
    }
    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return isValidPlayer(stack, slotContext.entity());
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return isValidPlayer(stack, slotContext.entity());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!isValidPlayer(stack, slotContext.entity())) {
            return;
        }

        ItemNBTHelper.setInt(stack, "Static", 0);
        ItemNBTHelper.setInt(stack, "HealRate", 0);
        ItemNBTHelper.removeEntry(stack, "LastX");
        ItemNBTHelper.removeEntry(stack, "LastY");
        ItemNBTHelper.removeEntry(stack, "LastZ");
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        ItemNBTHelper.removeEntry(stack, "Static");
        ItemNBTHelper.removeEntry(stack, "HealRate");
        ItemNBTHelper.removeEntry(stack, "LastX");
        ItemNBTHelper.removeEntry(stack, "LastY");
        ItemNBTHelper.removeEntry(stack, "LastZ");
    }
}
