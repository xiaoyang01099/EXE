package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;
import java.util.List;

public class ManaitaShears extends ShearsItem {
    private static final String NBT_RANGE = "DigRange";
    private static final String NBT_DOUBLING = "Doubling";
    private static final int MAX_RANGE = 19;

    public ManaitaShears(Properties properties) {
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);

        if (!level.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(stack);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }

        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }

        if (getRange(stack) == 0) {
            setRange(stack, 1);
        }

        if (!getDoubling(stack)) {
            setDoubling(stack, true);
        }
    }

    public static int getRange(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(NBT_RANGE);
    }

    public static void setRange(ItemStack stack, int range) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_RANGE, range);
    }

    public static boolean getDoubling(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean(NBT_DOUBLING);
    }

    public static void setDoubling(ItemStack stack, boolean doubling) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(NBT_DOUBLING, doubling);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction);
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

        if (!level.isClientSide && player.isShiftKeyDown()) {
            int currentRange = getRange(stack);

            if (currentRange >= MAX_RANGE) {
                setRange(stack, 1);
                player.displayClientMessage(
                        new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.message.mode_switch", 1, 1)
                                .withStyle(ChatFormatting.AQUA), false);
            } else {
                int newRange = currentRange + 2;
                setRange(stack, newRange);
                player.displayClientMessage(
                        new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.message.mode_switch", newRange, newRange)
                                .withStyle(ChatFormatting.AQUA), false);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.5F, 1.0F);

            return InteractionResultHolder.success(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResult.FAIL;
            }
        }

        if (player.level.isClientSide) {
            return InteractionResult.PASS;
        }

        int range = getRange(stack);
        AABB searchArea = target.getBoundingBox().inflate(range / 2.0);

        List<Entity> entities = player.level.getEntitiesOfClass(Entity.class, searchArea,
                entity -> entity instanceof IForgeShearable && entity instanceof LivingEntity);

        boolean success = false;
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && entity instanceof IForgeShearable shearable) {
                if (shearable.isShearable(stack, player.level, entity.blockPosition())) {
                    List<ItemStack> drops = shearable.onSheared(player, stack, player.level,
                            entity.blockPosition(), 0);

                    for (ItemStack drop : drops) {
                        entity.spawnAtLocation(drop);

                        if (getDoubling(stack)) {
                            for (int i = 0; i < 5; i++) {
                                entity.spawnAtLocation(drop.copy());
                            }
                        }
                    }
                    success = true;
                }
            }
        }

        if (success) {
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            return InteractionResult.SUCCESS;
        }

        return super.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return true;
            }
        }

        if (player.level.isClientSide) {
            return false;
        }

        int range = getRange(stack);
        int halfRange = range / 2;

        for (int x = -halfRange; x <= halfRange; x++) {
            for (int y = -halfRange; y <= halfRange; y++) {
                for (int z = -halfRange; z <= halfRange; z++) {
                    BlockPos targetPos = pos.offset(x, y, z);

                    if (targetPos.equals(pos)) {
                        continue;
                    }

                    BlockState state = player.level.getBlockState(targetPos);

                    if (super.getDestroySpeed(stack, state) > 1.0F) {
                        player.level.destroyBlock(targetPos, true, player);

                        if (getDoubling(stack)) {
                            List<ItemStack> drops = Block.getDrops(state, (ServerLevel)player.level, targetPos, null, player, stack);
                            for (ItemStack drop : drops) {
                                for (int i = 0; i < 5; i++) {
                                    Block.popResource(player.level, targetPos, drop.copy());
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        RelicImpl.addDefaultTooltip(stack, tooltip);
        super.appendHoverText(stack, level, tooltip, flag);

        int range = getRange(stack);
        boolean doubling = getDoubling(stack);

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.tooltip.mode", range, range)
                .withStyle(ChatFormatting.AQUA));

        String doublingStatus = doubling ?
                new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.tooltip.doubling.on").getString() :
                new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.tooltip.doubling.off").getString();

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.tooltip.doubling", doublingStatus)
                .withStyle(doubling ? ChatFormatting.GREEN : ChatFormatting.RED));

        tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.tooltip.usage")
                .withStyle(ChatFormatting.GRAY));

        if(Screen.hasShiftDown()) {
            tooltip.add(Component.nullToEmpty(""));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.desc")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.lore1")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            tooltip.add(new TranslatableComponent("item.ex_enigmaticlegacy.manaita_shears.lore2")
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(new TranslatableComponent("item.FRShiftTooltip.lore"));
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}