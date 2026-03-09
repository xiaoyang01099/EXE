package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EnderLavenderTeleporter;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;
import vazkii.botania.mixin.AccessorItemEntity;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.PacketBotaniaEffect;
import vazkii.botania.xplat.IXplatAbstractions;

import javax.annotation.Nullable;

public class EnderLavenderTile extends TileEntityFunctionalFlower {
    private static final int COST = 24;
    private static final int RANGE = 3;

    public EnderLavenderTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<EnderLavenderTile> {
        public FunctionalWandHud(EnderLavenderTile flower) {
            super(flower);
        }
    }

    public void tickFlower() {
        super.tickFlower();
        if (!this.getLevel().isClientSide) {
            ResourceKey<Level> worldKey = this.getLevel().dimension();
            ServerLevel destination;
            if (worldKey == Level.END) {
                destination = this.getLevel().getServer().getLevel(Level.OVERWORLD);
            } else {
                if (worldKey != Level.OVERWORLD) {
                    return;
                }

                destination = this.getLevel().getServer().getLevel(Level.END);
            }

            if (this.redstoneSignal == 0 && destination.isLoaded(this.getEffectivePos())) {
                BlockPos pos = this.getEffectivePos();
                boolean did = false;
                List<ItemEntity> items = this.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos.offset(-2, -2, -2), pos.offset(3, 3, 3)));
                int slowdown = this.getSlowdownFactor();

                for(ItemEntity item : items) {
                    int itemAge = item.tickCount;
                    int pickupDelay = ((AccessorItemEntity)item).getPickupDelay();
                    if (itemAge >= 60 + slowdown && pickupDelay == 0 && item.isAlive()) {
                        ItemStack stack = item.getItem();
                        if (!stack.isEmpty()) {
                            Item sitem = stack.getItem();
                            if (!(sitem instanceof IManaItem)) {
                                int cost = stack.getCount() * COST;
                                if (this.getMana() >= cost) {
                                    spawnExplosionParticles(item, 10);
                                    item.changeDimension(destination, EnderLavenderTeleporter.getInstance());
                                    item.setDeltaMovement(Vec3.ZERO);
                                    spawnExplosionParticles(item, 10);
                                    this.addMana(-cost);
                                    did = true;
                                }
                            }
                        }
                    }
                }

                if (did) {
                    this.sync();
                }
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap, LazyOptional.of(()-> new FunctionalWandHud(this)).cast());
    }

    private int getSlowdownFactor() {
        return 0;
    }

    static void spawnExplosionParticles(Entity item, int p) {
        IXplatAbstractions.INSTANCE.sendToNear(item.level, item.blockPosition(), new PacketBotaniaEffect(EffectType.ITEM_SMOKE, item.getX(), item.getY(), item.getZ(), new int[]{item.getId(), p}));
    }

    public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Circle(this.getEffectivePos(), RANGE);
    }

    public boolean acceptsRedstone() {
        return true;
    }

    public int getColor() {
        return 11879654;
    }

    public int getMaxMana() {
        return 16000;
    }
}