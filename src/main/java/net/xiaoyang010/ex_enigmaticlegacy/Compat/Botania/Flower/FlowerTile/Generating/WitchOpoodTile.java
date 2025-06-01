package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import com.google.common.collect.Lists;
import com.integral.enigmaticlegacy.helpers.PotionHelper;
import com.integral.enigmaticlegacy.items.HastePotion;
import com.integral.enigmaticlegacy.items.UltimatePotionBase;
import com.integral.enigmaticlegacy.items.UltimatePotionLingering;
import com.integral.enigmaticlegacy.items.UltimatePotionSplash;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.RadiusDescriptor.Rectangle;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WitchOpoodTile extends TileEntityGeneratingFlower {
    private static final String TAG_COOLDOWN = "cooldown";
    private static final int RANGE = 1;
    private int cooldown;
    Map<MobEffect, Integer> map = new HashMap<>();

    public WitchOpoodTile( BlockPos pos, BlockState state) {
        super(ModBlockEntities.WITCH_OPOOD_TILE.get(), pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (getLevel().isClientSide) {
            return;
        }

        if (cooldown-- <= 0) {
            AABB boundingBox = new AABB(getBlockPos().offset(-RANGE, -RANGE, -RANGE),
                    getBlockPos().offset(RANGE + 1, RANGE + 1, RANGE + 1));
            List<ItemEntity> items = getLevel().getEntitiesOfClass(ItemEntity.class, boundingBox,
                    entity -> {
                        entity.getItem().getItem();
                        return entity.getAge() > getSlowdownFactor();
                    });

            for (ItemEntity item : items) {
                ItemStack stack = item.getItem();
                List<MobEffectInstance> potions;
                Item stackItem = stack.getItem();
                //兼容神秘遗物药水
                if (ExEnigmaticlegacyMod.isEx && ( stackItem instanceof HastePotion || stackItem instanceof UltimatePotionSplash ||
                        stackItem instanceof UltimatePotionLingering || stackItem instanceof UltimatePotionBase)){
                    if (stackItem instanceof HastePotion hastePotion) {
                        potions = hastePotion.effectList;
                    }else potions = PotionHelper.getEffects(stack);
                }else potions = PotionUtils.getMobEffects(stack);
                if (potions.isEmpty()) {
                    continue;
                }

                ArrayList<MobEffect> unRemoveList = new ArrayList<>();
                int mana = 0;

                for (MobEffectInstance potion : potions) {
                    MobEffect effect = potion.getEffect();
                    if (map.containsKey(effect)) {
                        int before = map.get(effect);
                        map.put(effect, before + 1);
                        mana += 1 / (before + 1) * 3500;
                    } else {
                        map.put(effect, 1);
                        mana += 3500;
                    }
                    unRemoveList.add(effect);
                }

                addMana(mana);
                stack.shrink(1);
                map.entrySet().removeIf(e -> !unRemoveList.contains(e.getKey()));

                getLevel().playSound(null, getBlockPos(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.BLOCKS, 0.2F, 0.6F);

                if (getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENTITY_EFFECT,
                            item.getX(), item.getY(), item.getZ(),
                            20, 0.1D, 0.1D, 0.1D, 0.05D);
                }

                sync();
                break;
            }
        }
    }

    public static List<MobEffectInstance> getAllEffects(@javax.annotation.Nullable CompoundTag pCompoundTag) {
        List<MobEffectInstance> $$1 = Lists.newArrayList();
        $$1.addAll(PotionUtils.getPotion(pCompoundTag).getEffects());
        getCustomEffects(pCompoundTag, $$1);
        return $$1;
    }

    public static void getCustomEffects(@javax.annotation.Nullable CompoundTag pCompoundTag, List<MobEffectInstance> pEffectList) {
        if (pCompoundTag != null && pCompoundTag.contains("EnigmaticPotion")) {
            String $$2 = pCompoundTag.getString("EnigmaticPotion");

            MobEffectInstance $$5 = MobEffectInstance.load(pCompoundTag);
            if ($$5 != null) {
                pEffectList.add($$5);
            }
        }

    }

    private int getSlowdownFactor() {
        return 5;
    }

    @Override
    public void writeToPacketNBT(CompoundTag cmp) {
        super.writeToPacketNBT(cmp);
        cmp.putInt(TAG_COOLDOWN, cooldown);
    }

    @Override
    public void readFromPacketNBT(CompoundTag cmp) {
        super.readFromPacketNBT(cmp);
        cooldown = cmp.getInt(TAG_COOLDOWN);
    }

    @Override
    public RadiusDescriptor getRadius() {
        return Rectangle.square(getEffectivePos(), RANGE);
    }

    @Override
    public int getColor() {
        return 0xFFB7C5;
    }

    @Override
    public int getMaxMana() {
        return 10000;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }
}