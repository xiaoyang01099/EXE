package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.integral.enigmaticlegacy.api.items.ICursed;
import com.integral.enigmaticlegacy.helpers.ItemLoreHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityDarkMatterOrb;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import java.util.List;

public class EldritchSpell extends Item implements ICursed {
    public static final float ELDRITCH_SPELL_DAMAGE = 32.5f;
    public static final float ELDRITCH_SPELL_DAMAGE_EX = 100.0f;
    public static final int COOLDOWN_TICKS = 20;
    public static final int EXPERIENCE_COST = 20;

    public EldritchSpell(Properties properties) {
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemLoreHelper.indicateCursedOnesOnly(tooltip);
        RelicImpl.addDefaultTooltip(stack, tooltip);

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("item.ItemEldritchSpell1.lore").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.ItemEldritchSpell2.lore").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent("item.ItemEldritchSpell3.lore").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(new TranslatableComponent(""));
            tooltip.add(new TranslatableComponent("item.ItemEldritchSpell4.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ItemEldritchSpell5.lore",
                    ELDRITCH_SPELL_DAMAGE).withStyle(ChatFormatting.RED));
            tooltip.add(new TranslatableComponent("item.ItemEldritchSpell6.lore").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent(""));
        } else if (Screen.hasControlDown()) {
            tooltip.add(new TranslatableComponent("consumed_per_cast:").withStyle(ChatFormatting.AQUA));
            tooltip.add(new TranslatableComponent(" • " + EXPERIENCE_COST + " XP").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore").withStyle(ChatFormatting.YELLOW));
            tooltip.add(new TranslatableComponent("item.FRCtrlTooltip.lore").withStyle(ChatFormatting.AQUA));
        }
    }

    public boolean spawnOrb(Level level, Player player) {
        if (!level.isClientSide) {
            Vector3 originalPos = Vector3.fromEntityCenter(player);
            Vector3 vector = originalPos.add(new Vector3(player.getViewVector(1.0f)).multiply(1.0));
            vector.y += 0.5;
            Vector3 motion = new Vector3(player.getViewVector(1.0f)).multiply(1.5);

            EntityDarkMatterOrb orb = new EntityDarkMatterOrb(level, player);
            orb.setPos(vector.x, vector.y, vector.z);
            orb.setDeltaMovement(motion.x, motion.y, motion.z);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS,
                    0.6f, 0.8f + (float)Math.random() * 0.2f);

            level.addFreshEntity(orb);
            return true;
        }
        return false;
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

        if (!RelicsEventHandler.isOnCoodown(player)) {
            if (!player.getAbilities().instabuild && !hasEnoughExperience(player, EXPERIENCE_COST)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.5f, 1.0f);
                return InteractionResultHolder.fail(stack);
            }

            if (this.spawnOrb(level, player)) {
                RelicsEventHandler.setCasted(player, COOLDOWN_TICKS, true);

                if (!player.getAbilities().instabuild) {
                    consumeExperience(player, EXPERIENCE_COST);
                }

                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.fail(stack);
    }

    private boolean hasEnoughExperience(Player player, int requiredExp) {
        return player.totalExperience >= requiredExp;
    }

    private void consumeExperience(Player player, int expToConsume) {
        int currentExp = player.totalExperience;
        if (currentExp >= expToConsume) {
            player.totalExperience = 0;
            player.experienceLevel = 0;
            player.experienceProgress = 0;

            int remainingExp = currentExp - expToConsume;
            player.giveExperiencePoints(remainingExp);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    public static float getDamageForDimension(Level level) {
        String dimensionLocation = level.dimension().location().toString();
        if (dimensionLocation.contains("ender") || dimensionLocation.contains("nether")) {
            return ELDRITCH_SPELL_DAMAGE_EX;
        }
        return ELDRITCH_SPELL_DAMAGE;
    }
}