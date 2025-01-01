package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Future.FunctionalFlower;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubTileCurseThistle extends TileEntityFunctionalFlower {
    public SubTileCurseThistle(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<SubTileCurseThistle> {
        public FunctionalWandHud(SubTileCurseThistle flower) {
            super(flower);
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (level == null || level.isClientSide || ticksExisted % 40 != 0) {
            return;
        }

        AABB searchArea = new AABB(getEffectivePos().offset(-getRange(), -getRange(), -getRange()),
                getEffectivePos().offset(getRange(), getRange(), getRange()));

        List<Player> list = level.getEntitiesOfClass(Player.class, searchArea);

        thisTick:
        for (Player player : list) {
            for(ItemStack armor : player.getArmorSlots()) {
                Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(armor);

                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    Enchantment enchant = entry.getKey();
                    int level = entry.getValue();

                    if (enchant.isCurse()) {
                        double manaCost = cost() * level * Math.pow(1.2, level - 1);
                        if (getMana() > manaCost) {
                            addMana((int)-manaCost);

                            // 移除或降低诅咒等级
                            int newLevel = level - 1;
                            Map<Enchantment, Integer> newEnchants = new HashMap<>(enchants);

                            if (newLevel == 0) {
                                newEnchants.remove(enchant);
                            } else {
                                newEnchants.put(enchant, newLevel);
                            }

                            EnchantmentHelper.setEnchantments(newEnchants, armor);

                            // 添加粒子效果和声音
                            if(this.level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.WITCH,
                                        player.getX(), player.getY() + 1.0, player.getZ(),
                                        20, 0.5, 0.5, 0.5, 0.1);

                                this.level.playSound(null, player.blockPosition(),
                                        SoundEvents.ENCHANTMENT_TABLE_USE,
                                        SoundSource.BLOCKS, 1.0f, 1.0f);
                            }

                            break thisTick;
                        }
                    }
                }
            }
        }
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), getRange());
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new SubTileCurseThistle.FunctionalWandHud(this)).cast());
    }


    @Override
    public int getMaxMana() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getColor() {
        return 0xD3D609;
    }

    private int getRange() {
        return 2;
    }

    private int cost() {
        return 10000;
    }
}