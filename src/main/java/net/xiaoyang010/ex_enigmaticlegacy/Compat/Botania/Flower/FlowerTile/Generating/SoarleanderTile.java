package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Generating;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Objects;

public class SoarleanderTile extends TileEntityGeneratingFlower {

    private static final String BURN_TIME = "burnTime";
    private static final int RANGE = 3;
    private static final int START_BURN_EVENT = 0;
    int burnTime = 0;

    public SoarleanderTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOARLEANDERTILE.get(), pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        if (burnTime > 0) {
            burnTime -= 50;
            addMana(500);
        }

        if (level.isClientSide) {
            if (burnTime > 0 && level.random.nextInt(10) == 0) {
                Vec3 offset = level.getBlockState(this.worldPosition).getOffset(level, this.worldPosition).add(0.4, 0.7, 0.4);
                level.addParticle(ParticleTypes.FLAME, this.worldPosition.getX() + offset.x + Math.random() * 0.2, this.worldPosition.getY() + offset.y, this.worldPosition.getZ() + offset.z + Math.random() * 0.2, 0.0D, 0.0D, 0.0D);
            }
        }

        if (findBoundTile() != null) {
            if (burnTime == 0 && getMana() < getMaxMana()) {
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(this.worldPosition.offset(-RANGE, -RANGE, -RANGE), this.worldPosition.offset(RANGE + 1, RANGE + 1, RANGE + 1)))) {
                    if (item.getAge() >= 59 && !item.isRemoved()) {
                        ItemStack stack = item.getItem();
                        if (stack.isEmpty() || stack.getItem().hasCraftingRemainingItem()) {
                            continue;
                        }

                        int burnTime = switch (Objects.requireNonNull(stack.getItem().getRegistryName()).toString()) {
                            case "minecraft:chicken" -> 1000;
                            case "minecraft:cooked_chicken" -> 2500;
                            case "minecraft:egg" -> 500;
                            case "minecraft:feather" -> 250;
                            case "ex_enigmaticlegacy:enigmatic_viscous_substance" -> 80000;
                            case "ex_enigmaticlegacy:tabooapex" -> 999999999;
                            default -> 0;
                        };

                        if (burnTime > 0 && stack.getCount() > 0) {
                            this.burnTime = burnTime;
                            stack.shrink(1);
                            level.playSound(null, this.worldPosition, SoundEvents.CHICKEN_HURT, SoundSource.BLOCKS, 0.2F, 1F);
                            level.blockEvent(this.worldPosition, level.getBlockState(this.worldPosition).getBlock(), START_BURN_EVENT, item.getId());
                            sync();
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean triggerEvent(int event, int param) {
        if (event == START_BURN_EVENT) {
            Entity entity = null;
            if (level != null) {
                entity = level.getEntity(param);
            }
            if (entity != null) {
                level.addParticle(ParticleTypes.SMOKE, entity.getX(), entity.getY() + 0.1, entity.getZ(), 0.0D, 0.0D, 0.0D);
                level.addParticle(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 0.0D, 0.0D, 0.0D);
            }
            return true;
        } else {
            return super.triggerEvent(event, param);
        }
    }

    @Override
    public int getMaxMana() {
        return 114514;
    }

    @Override
    public int getColor() {
        return 0x11FF00;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }


    @Override
    public void writeToPacketNBT(CompoundTag tagCompound) {
        super.writeToPacketNBT(tagCompound);
        tagCompound.putInt(BURN_TIME, burnTime);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tagCompound) {
        super.readFromPacketNBT(tagCompound);
        burnTime = tagCompound.getInt(BURN_TIME);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new GeneratingWandHud(this)).cast());
    }
}
