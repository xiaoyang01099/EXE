package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.helper.ExperienceHelper;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class XPTome extends Item {
    public static final String TAG_ABSORPTION = "AbsorptionMode";
    public static final String TAG_ACTIVE = "IsActive";
    public static final String TAG_XP_STORED = "XPStored";
    public static final int XP_PORTION = 5;

    public XPTome(Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        RelicImpl.addDefaultTooltip(stack, tooltipComponents);

        Component modeText;
        if (!ItemNBTHelper.getBoolean(stack, TAG_ACTIVE, false)) {
            modeText = new TranslatableComponent("item.ItemXPTomeDeactivated.lore").withStyle(ChatFormatting.RED);
        } else if (ItemNBTHelper.getBoolean(stack, TAG_ABSORPTION, true)) {
            modeText = new TranslatableComponent("item.ItemXPTomeAbsorption.lore").withStyle(ChatFormatting.GREEN);
        } else {
            modeText = new TranslatableComponent("item.ItemXPTomeExtraction.lore").withStyle(ChatFormatting.BLUE);
        }

        if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome1.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.nullToEmpty(""));
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome2.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome3.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome4.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.nullToEmpty(""));
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome5.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome6.lore").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.nullToEmpty(""));
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome7.lore").withStyle(ChatFormatting.YELLOW));
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome8.lore").withStyle(ChatFormatting.YELLOW));
            tooltipComponents.add(new TranslatableComponent("item.ItemXPTome9.lore").withStyle(ChatFormatting.YELLOW));
        } else {
            tooltipComponents.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.GRAY));
        }

        tooltipComponents.add(Component.nullToEmpty(""));
        tooltipComponents.add(new TranslatableComponent("item.ItemXPTomeMode.lore")
                .append(" ").append(modeText));
        tooltipComponents.add(Component.nullToEmpty(""));
        tooltipComponents.add(new TranslatableComponent("item.ItemXPTomeExp.lore").withStyle(ChatFormatting.AQUA));

        int storedXP = ItemNBTHelper.getInt(stack, TAG_XP_STORED, 0);
        int level = ExperienceHelper.getLevelForExperience(storedXP);
        tooltipComponents.add(new TranslatableComponent("item.FRCode6.lore")
                .append(String.valueOf(storedXP))
                .append(" ")
                .append(new TranslatableComponent("item.ItemXPTomeUnits.lore"))
                .append(" ")
                .append(String.valueOf(level))
                .append(" ")
                .append(new TranslatableComponent("item.ItemXPTomeLevels.lore"))
                .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }

            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relicInstance = relicCap.orElse(null);
                if (relicInstance != null && !relicInstance.isRightPlayer(player)) {
                    return;
                }
            }

            if (ItemNBTHelper.getBoolean(stack, TAG_ACTIVE, false)) {
                boolean action = false;

                if (ItemNBTHelper.getBoolean(stack, TAG_ABSORPTION, true)) {
                    int playerXP = ExperienceHelper.getPlayerXP(player);
                    if (playerXP >= XP_PORTION) {
                        ExperienceHelper.drainPlayerXP(player, XP_PORTION);
                        int currentStored = ItemNBTHelper.getInt(stack, TAG_XP_STORED, 0);
                        ItemNBTHelper.setInt(stack, TAG_XP_STORED, currentStored + XP_PORTION);
                        action = true;
                    } else if (playerXP > 0) {
                        ExperienceHelper.drainPlayerXP(player, playerXP);
                        int currentStored = ItemNBTHelper.getInt(stack, TAG_XP_STORED, 0);
                        ItemNBTHelper.setInt(stack, TAG_XP_STORED, currentStored + playerXP);
                        action = true;
                    }
                } else {
                    int storedXP = ItemNBTHelper.getInt(stack, TAG_XP_STORED, 0);
                    if (storedXP >= XP_PORTION) {
                        ItemNBTHelper.setInt(stack, TAG_XP_STORED, storedXP - XP_PORTION);
                        ExperienceHelper.addPlayerXP(player, XP_PORTION);
                        action = true;
                    } else if (storedXP > 0) {
                        ItemNBTHelper.setInt(stack, TAG_XP_STORED, 0);
                        ExperienceHelper.addPlayerXP(player, storedXP);
                        action = true;
                    }
                }

                if (action) {
                    player.containerMenu.broadcastChanges();
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        if (!player.isShiftKeyDown()) {
            boolean currentMode = ItemNBTHelper.getBoolean(stack, TAG_ABSORPTION, true);
            ItemNBTHelper.setBoolean(stack, TAG_ABSORPTION, !currentMode);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                    1.0F, (float)(0.4F + Math.random() * 0.1F));

        } else {
            boolean isActive = ItemNBTHelper.getBoolean(stack, TAG_ACTIVE, false);
            ItemNBTHelper.setBoolean(stack, TAG_ACTIVE, !isActive);

            if (!isActive) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS,
                        1.0F, (float)(0.8F + Math.random() * 0.2F));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS,
                        1.0F, (float)(0.8F + Math.random() * 0.2F));
            }
        }

        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ItemNBTHelper.getBoolean(stack, TAG_ACTIVE, false);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int storedXP = ItemNBTHelper.getInt(stack, TAG_XP_STORED, 0);
        int maxDisplay = 1395;
        return Math.min(13, (int)(13.0F * storedXP / maxDisplay));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        boolean isActive = ItemNBTHelper.getBoolean(stack, TAG_ACTIVE, false);
        boolean isAbsorption = ItemNBTHelper.getBoolean(stack, TAG_ABSORPTION, true);

        if (!isActive) {
            return 0x555555;
        } else if (isAbsorption) {
            return 0x00AA00;
        } else {
            return 0x0077FF;
        }
    }
}