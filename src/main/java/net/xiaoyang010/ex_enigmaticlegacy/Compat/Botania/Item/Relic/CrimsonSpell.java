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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityCrimsonOrb;
import net.xiaoyang010.ex_enigmaticlegacy.Event.RelicsEventHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CrimsonSpell extends Item implements ICursed {

    private static final float CRIMSON_SPELL_DAMAGE_MIN = 42.0f;
    private static final float CRIMSON_SPELL_DAMAGE_MAX = 100.0f;
    private static final float SEARCH_RANGE = 3.0F;
    private static final int COOLDOWN_TICKS = 30;
    private static final int SEARCH_DISTANCE = 32;

    public CrimsonSpell(Properties properties) {
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
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.lore1")
                    .withStyle(ChatFormatting.DARK_RED));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.lore2")
                    .withStyle(ChatFormatting.DARK_RED));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.lore3")
                    .withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.nullToEmpty(""));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.lore4")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.lore5")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.damage_range",
                    (int)CRIMSON_SPELL_DAMAGE_MIN, (int)CRIMSON_SPELL_DAMAGE_MAX)
                    .withStyle(ChatFormatting.RED));
        } else if (Screen.hasControlDown()) {
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.cooldown", COOLDOWN_TICKS / 20)
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.range", SEARCH_DISTANCE)
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(new TranslatableComponent("item.FRCtrlTooltip.lore")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    public boolean spawnOrb(Level level, Player player, LivingEntity target) {
        if (!level.isClientSide) {
            Vector3 originalPos = Vector3.fromEntityCenter(player);
            Vector3 vector = originalPos.add(new Vector3(player.getLookAngle()).multiply(1.0));
            vector.y += 0.5;
            Vector3 motion = new Vector3(player.getLookAngle()).multiply(0.75);

            EntityCrimsonOrb orb = new EntityCrimsonOrb(ModEntities.CRIMSON_ORB.get(), level, player, target, true);
            orb.setPos(vector.x, vector.y, vector.z);
            orb.setDeltaMovement(motion.x, motion.y, motion.z);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.6F,
                    0.8F + (float)Math.random() * 0.2F);

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

        if (!level.isClientSide && !RelicsEventHandler.isOnCoodown(player)) {
            LivingEntity target = findTarget(level, player);

            int expCost = 5;
            if (player.experienceLevel >= expCost || player.isCreative()) {

                if (!player.isCreative()) {
                    player.giveExperiencePoints(-expCost * 7);
                }

                this.spawnOrb(level, player, target);
                RelicsEventHandler.setCasted(player, COOLDOWN_TICKS, true);

                return InteractionResultHolder.success(stack);
            } else {

                player.displayClientMessage(
                        new TranslatableComponent("item.ex_enigmaticlegacy.crimson_spell.insufficient_exp", expCost)
                                .withStyle(ChatFormatting.RED), false
                );
                return InteractionResultHolder.fail(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    private LivingEntity findTarget(Level level, Player player) {
        List<LivingEntity> entities = new ArrayList<>();

        for (int distance = 1; entities.isEmpty() && distance < SEARCH_DISTANCE; distance++) {
            float superposition = 0.0F;
            if (distance > 10) {
                superposition = 3.0F;
            }
            if (distance > 20) {
                superposition = 5.0F;
            }

            Vector3 vec = Vector3.fromEntityCenter(player);
            vec.add(new Vector3(player.getLookAngle()).multiply(distance));
            vec.y += 0.5;

            float searchRange = SEARCH_RANGE + superposition;
            AABB searchArea = new AABB(
                    vec.x - searchRange, vec.y - searchRange, vec.z - searchRange,
                    vec.x + searchRange, vec.y + searchRange, vec.z + searchRange
            );

            entities = level.getEntitiesOfClass(LivingEntity.class, searchArea);
            entities.remove(player);
        }

        boolean notFound = false;

        if (entities.isEmpty()) {
            notFound = true;
            AABB largeArea = new AABB(
                    player.getX() - SEARCH_DISTANCE, player.getY() - SEARCH_DISTANCE, player.getZ() - SEARCH_DISTANCE,
                    player.getX() + SEARCH_DISTANCE, player.getY() + SEARCH_DISTANCE, player.getZ() + SEARCH_DISTANCE
            );
            entities = level.getEntitiesOfClass(LivingEntity.class, largeArea);
        }

        if (!entities.isEmpty() && notFound) {
            entities.removeIf(entity -> !RelicsEventHandler.canEntityBeSeen(entity,
                    player.getX(), player.getY() + player.getEyeHeight(), player.getZ()));
        }

        entities.remove(player);

        if (!entities.isEmpty()) {
            return entities.get((int)(Math.random() * entities.size()));
        }

        return null;
    }
}