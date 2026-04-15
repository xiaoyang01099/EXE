package org.xiaoyang.ex_enigmaticlegacy.Compat.Botania.Flower.functional;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CelestialBlueHyacinthTile extends FunctionalFlowerBlockEntity {

    private static final int MANA_COST = 100000; // 消耗的mana量
    private static final int KNOWLEDGE_COUNT = 8; // 每次解锁的知识数量
    private static final int ACTIVATION_RANGE = 5; // 激活范围
    private static final int COOLDOWN_TICKS = 200; // 冷却时间 (10秒)
    private static final int RANGE = 8;
    private int cooldown = 0;

    public CelestialBlueHyacinthTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<CelestialBlueHyacinthTile> {
        public FunctionalWandHud(CelestialBlueHyacinthTile flower) {
            super(flower);
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }

    /**
     * 主要的花朵逻辑
     */
    @Override
    public void tickFlower() {
        super.tickFlower();

        if (getLevel().isClientSide() || !canActivate()) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (getMana() < MANA_COST) {
            return;
        }

        AABB searchBox = new AABB(getBlockPos()).inflate(ACTIVATION_RANGE);
        List<ServerPlayer> nearbyPlayers = getLevel().getEntitiesOfClass(
                ServerPlayer.class,
                searchBox,
                player -> player.isAlive() && !player.isSpectator()
        );

        if (nearbyPlayers.isEmpty()) {
            return;
        }

        ServerPlayer player = nearbyPlayers.get(0);
        if (unlockKnowledgeForPlayer(player)) {
            addMana(-MANA_COST);
            cooldown = COOLDOWN_TICKS;
            playActivationEffect();
        }
    }

    /**
     * 为玩家解锁知识
     */
    private boolean unlockKnowledgeForPlayer(ServerPlayer player) {
        try {
            ITransmutationProxy transmutationProxy = ProjectEAPI.getTransmutationProxy();
            IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();

            if (transmutationProxy == null || emcProxy == null) {
                return false;
            }

            IKnowledgeProvider knowledgeProvider = transmutationProxy.getKnowledgeProviderFor(player.getUUID());

            if (knowledgeProvider == null) {
                return false;
            }

            List<ItemInfo> availableItems = getAllItemsWithEMC(emcProxy);

            List<ItemInfo> unknownItems = availableItems.stream()
                    .filter(itemInfo -> !knowledgeProvider.hasKnowledge(itemInfo))
                    .collect(Collectors.toList());

            if (unknownItems.isEmpty()) {
                return false;
            }

            Collections.shuffle(unknownItems);
            int unlockCount = Math.min(KNOWLEDGE_COUNT, unknownItems.size());

            int successCount = 0;
            for (int i = 0; i < unlockCount; i++) {
                ItemInfo itemInfo = unknownItems.get(i);
                if (knowledgeProvider.addKnowledge(itemInfo)) {
                    successCount++;
                }
            }

            if (successCount > 0) {
                knowledgeProvider.sync(player);
            }

            return successCount > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取所有有EMC值的物品
     */
    private List<ItemInfo> getAllItemsWithEMC(IEMCProxy emcProxy) {
        List<ItemInfo> itemsWithEMC = new ArrayList<>();

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            try {
                ItemStack stack = new ItemStack(item);
                if (!stack.isEmpty()) {
                    ItemInfo itemInfo = ItemInfo.fromItem(item);
                    if (emcProxy.hasValue(itemInfo)) {
                        itemsWithEMC.add(itemInfo);
                    }
                }
            } catch (Exception e) {
            }
        }

        return itemsWithEMC;
    }

    /**
     * 播放激活效果
     */
    private void playActivationEffect() {
        if (getLevel().isClientSide()) {
            return;
        }

        // 生成紫色粒子效果
        for (int i = 0; i < 20; i++) {
            double x = getBlockPos().getX() + 0.5 + (Math.random() - 0.5) * 2;
            double y = getBlockPos().getY() + 0.5 + Math.random();
            double z = getBlockPos().getZ() + 0.5 + (Math.random() - 0.5) * 2;

            // 紫色粒子 (知识的颜色)
            ((ServerLevel) getLevel()).sendParticles(
                    ParticleTypes.ENCHANT,
                    x, y, z,
                    1, 0, 0, 0, 0.1
            );
        }
    }

    @Override
    public int getMaxMana() {
        return MANA_COST * 2;
    }

    @Override
    public int getColor() {
        return 0x4169E1;
    }

    @Override
    public boolean acceptsRedstone() {
        return true;
    }

    private boolean canActivate() {
        return redstoneSignal == 0;
    }

    @Override
    public @Nullable RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }
}