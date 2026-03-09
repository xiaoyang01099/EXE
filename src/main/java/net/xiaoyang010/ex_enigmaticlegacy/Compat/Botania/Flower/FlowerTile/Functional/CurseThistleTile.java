package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nullable;
import java.util.*;

public class CurseThistleTile extends TileEntityFunctionalFlower {

    private static final int COOLDOWN_TIME = 200; // 10秒冷却时间
    private static final int BASE_COST = 100; // 降低基础消耗
    private static final int BASE_RANGE = 3; // 稍微增加范围
    private static final int EXTENDED_RANGE = 5; // 扩展范围

    private final Map<UUID, Integer> playerCooldowns = new HashMap<>();
    private boolean extendedMode = false;

    public CurseThistleTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<CurseThistleTile> {
        public FunctionalWandHud(CurseThistleTile flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (level == null || level.isClientSide) {
            return;
        }

        // 更新玩家冷却时间
        updateCooldowns();

        // 检查是否可以启用扩展模式
        checkExtendedMode();

        // 每40 ticks (2秒) 执行一次净化
        if (ticksExisted % 40 != 0) {
            return;
        }

        performCurseCleansing();
    }

    /**
     * 检查并启用扩展模式
     */
    private void checkExtendedMode() {
        int requiredMana = BASE_COST * 10;
        extendedMode = getMana() >= requiredMana;
    }

    /**
     * 更新玩家冷却时间
     */
    private void updateCooldowns() {
        playerCooldowns.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - 1);
            return entry.getValue() <= 0;
        });
    }

    /**
     * 执行诅咒净化
     */
    private void performCurseCleansing() {
        int range = extendedMode ? EXTENDED_RANGE : BASE_RANGE;
        AABB searchArea = new AABB(
                getEffectivePos().offset(-range, -range, -range),
                getEffectivePos().offset(range, range, range)
        );

        List<Player> players = null;
        if (level != null) {
            players = level.getEntitiesOfClass(Player.class, searchArea);
        }

        players.sort((p1, p2) -> Double.compare(
                p1.distanceToSqr(getEffectivePos().getX(), getEffectivePos().getY(), getEffectivePos().getZ()),
                p2.distanceToSqr(getEffectivePos().getX(), getEffectivePos().getY(), getEffectivePos().getZ())
        ));

        for (Player player : players) {
            if (playerCooldowns.containsKey(player.getUUID())) {
                continue;
            }

            if (tryCleansePlayer(player)) {
                playerCooldowns.put(player.getUUID(), COOLDOWN_TIME);
                break;
            }
        }
    }

    /**
     * 尝试净化玩家的诅咒
     */
    private boolean tryCleansePlayer(Player player) {
        List<ItemStack> equipment = new ArrayList<>();
        player.getArmorSlots().forEach(equipment::add);
        equipment.add(player.getMainHandItem());
        equipment.add(player.getOffhandItem());

        List<CurseInfo> curses = new ArrayList<>();
        for (ItemStack stack : equipment) {
            if (!stack.isEmpty()) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    if (entry.getKey().isCurse()) {
                        curses.add(new CurseInfo(stack, entry.getKey(), entry.getValue()));
                    }
                }
            }
        }

        if (curses.isEmpty()) {
            return false;
        }

        curses.sort(Comparator.comparingInt(c -> c.level));

        for (CurseInfo curse : curses) {
            double manaCost = calculateManaCost(curse.level);

            if (getMana() >= manaCost) {
                addMana((int)-manaCost);
                removeCurse(curse);
                playEffects(player);
                return true;
            }
        }

        return false;
    }

    /**
     * 计算mana消耗
     */
    private double calculateManaCost(int level) {
        double multiplier = extendedMode ? 0.8 : 1.0;
        return BASE_COST * level * (1 + (level - 1) * 0.5) * multiplier;
    }

    /**
     * 移除诅咒
     */
    private void removeCurse(CurseInfo curse) {
        Map<Enchantment, Integer> enchants = new HashMap<>(EnchantmentHelper.getEnchantments(curse.stack));

        int newLevel = curse.level - 1;
        if (newLevel <= 0) {
            enchants.remove(curse.enchantment);
        } else {
            enchants.put(curse.enchantment, newLevel);
        }

        EnchantmentHelper.setEnchantments(enchants, curse.stack);
    }

    /**
     * 播放特效
     */
    private void playEffects(Player player) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    30, 0.8, 0.8, 0.8, 0.15);

            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);

            level.playSound(null, player.blockPosition(),
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS, 1.0f, 1.2f);
        }
    }

    @Override
    public RadiusDescriptor getRadius() {
        int range = extendedMode ? EXTENDED_RANGE : BASE_RANGE;
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), range);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new FunctionalWandHud(this)).cast());
    }

    @Override
    public int getMaxMana() {
        return 100000;
    }

    @Override
    public int getColor() {
        return extendedMode ? 0xFFD700 : 0xD3D609;
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        playerCooldowns.clear();
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
    }

    /**
     * 诅咒信息包装类
     */
    private static class CurseInfo {
        final ItemStack stack;
        final Enchantment enchantment;
        final int level;

        CurseInfo(ItemStack stack, Enchantment enchantment, int level) {
            this.stack = stack;
            this.enchantment = enchantment;
            this.level = level;
        }
    }
}