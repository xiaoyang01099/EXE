package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.FlowerTile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.common.item.block.ItemBlockTinyPotato;

import java.util.List;
import java.util.Objects;

public class BelieverTile extends TileEntityGeneratingFlower {

    private static final String TAG_COOLDOWN = "cooldown";
    private static final int RANGE = 1;
    private static final int MANA_PER_POTATO = 1452;
    private int cooldown = 0;
    private static final Logger log = LoggerFactory.getLogger(BelieverTile.class);

    public BelieverTile(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.BELIEVERTILE.get(), blockPos, blockState);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (getLevel().isClientSide) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }


        AABB boundingBox = new AABB(getBlockPos().offset(-RANGE, -RANGE, -RANGE),
                getBlockPos().offset(RANGE + 1, RANGE + 1, RANGE + 1));

        List<ItemEntity> items = Objects.requireNonNull(getLevel()).getEntitiesOfClass(ItemEntity.class, boundingBox,
                entity -> entity.getItem().getItem() instanceof ItemBlockTinyPotato && entity.getAge() >= getSlowdownFactor());

        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            stack.shrink(1);
            addMana(100);

            getLevel().playSound(null, getBlockPos(), SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.2F, 0.6F);

            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack),
                        item.getX(), item.getY(), item.getZ(),
                        20, 0.1D, 0.1D, 0.1D, 0.05D);
            }

            sync();
            break;
        }
    }

    private int getSlowdownFactor() {
        return 5;
    }

    @Override
    public int getMaxMana() {
        return 5201314;
    }

    @Override
    public int getColor() {
        return 0xA52A2A;
    }

    @Nullable
    @Override
    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(getBlockPos(),2);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }
}
