package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Flower.FlowerTile.Functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityFunctionalFlower;

import javax.annotation.Nullable;
import java.util.List;

public class AstralKillopTile extends TileEntityFunctionalFlower {
    private static final String TAG_DAYS = "days";
    private static final String TAG_LAST_DAY_CHECK = "lastDayCheck";
    private static final String TAG_PLACEMENT_TIME = "placementTime";
    private static final String TAG_INITIALIZED = "initialized";
    private static final String TAG_ACCELERATED_TICKS = "acceleratedTicks";
    private static final String TAG_PROJECTE_BOOST = "projecteBoost";
    private static final String TAG_SIMULATED_TIME = "simulatedTime";

    private int days = 0;
    private long lastDayCheck = -1;
    private long placementTime = -1; // 记录放置时间
    private boolean initialized = false; // 防止重复初始化
    private int acceleratedTicks = 0; // 累积的加速tick数
    private boolean projecteBoost = false; // 是否被ProjectE加速
    private long simulatedTime = 0; // 模拟的加速时间

    public AstralKillopTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static class FunctionalWandHud extends TileEntityFunctionalFlower.FunctionalWandHud<AstralKillopTile> {
        public FunctionalWandHud(AstralKillopTile flower) {
            super(flower);
        }

        private boolean isProjectELoaded() {
            return ModList.get().isLoaded("projecte");
        }

        @Override
        public void renderHUD(PoseStack ms, Minecraft mc) {
            super.renderHUD(ms, mc);

            int currentDays = flower.getDays();
            int nuggetDay = ConfigHandler.FlowerConfig.getAstralKillopNuggetDay();
            int effectDay = ConfigHandler.FlowerConfig.getAstralKillopEffectDay();
            int manaCost = ConfigHandler.FlowerConfig.getAstralKillopManaCost();

            int daysToNugget = nuggetDay - currentDays;
            int daysToEffect = effectDay - currentDays;

            String statusInfo = new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.status",
                    currentDays, manaCost).getString();

            String nextRewardInfo;
            if (daysToEffect <= 0) {
                nextRewardInfo = "§6" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.effect_ready").getString();
            } else if (daysToNugget <= 0 && daysToNugget > -nuggetDay) {
                nextRewardInfo = "§b" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.next_effect",
                        daysToEffect).getString();
            } else if (daysToNugget > 0) {
                nextRewardInfo = "§9" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.next_nugget",
                        daysToNugget).getString();
            } else {
                nextRewardInfo = "§b" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.next_effect",
                        daysToEffect).getString();
            }

            String manaInfo = "§b" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.mana",
                    String.format("%,d", flower.getMana()),
                    String.format("%,d", flower.getMaxMana()),
                    ConfigHandler.FlowerConfig.getAstralKillopRange()).getString();

            String specialInfo = "";
            if (flower.getMana() < manaCost) {
                specialInfo = "§c" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.insufficient_mana").getString();
            } else if (daysToEffect == 1) {
                specialInfo = "§6" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.tomorrow_effect").getString();
            } else if (daysToNugget == 1) {
                specialInfo = "§9" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.tomorrow_nugget").getString();
            } else if (currentDays == 0) {
                specialInfo = "§a" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.starting").getString();
            }

            String projecteInfo = "";
            if (isProjectELoaded() && flower.isProjecteBoost()) {
                projecteInfo = "§d" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.projecte_acceleration",
                        flower.getAcceleratedTicks()).getString();
            }

            int yOffset = 40;
            mc.font.draw(ms, statusInfo, 10, yOffset, 0xFFFFFF);
            yOffset += 12;
            mc.font.draw(ms, nextRewardInfo, 10, yOffset, 0xFFFFFF);
            yOffset += 12;
            mc.font.draw(ms, manaInfo, 10, yOffset, 0xFFFFFF);

            if (!specialInfo.isEmpty()) {
                yOffset += 12;
                mc.font.draw(ms, specialInfo, 10, yOffset, 0xFFFFFF);
            }

            if (!projecteInfo.isEmpty()) {
                yOffset += 12;
                mc.font.draw(ms, projecteInfo, 10, yOffset, 0xFFFFFF);
            }

            if (daysToEffect > 0) {
                yOffset += 12;
                int progress = (int)((float)(effectDay - daysToEffect) / effectDay * 100);
                String progressInfo = "§7" + new TranslatableComponent("hud.ex_enigmaticlegacy.astral_killop.progress",
                        progress).getString();
                mc.font.draw(ms, progressInfo, 10, yOffset, 0xFFFFFF);
            }
        }
    }

    @Override
    public void tickFlower() {
        super.tickFlower();

        if (level == null || level.isClientSide) return;

        if (!initialized) {
            if (placementTime == -1) {
                placementTime = level.getGameTime();
                long effectiveTime = level.getDayTime() + simulatedTime;
                lastDayCheck = effectiveTime / 24000L;
                setChanged();
            }
            initialized = true;
            setChanged();
        }

        detectProjectEAcceleration();

        processTimeLogic();

        if (ticksExisted % 40 == 0) {
            syncToClient();
        }
    }

    private void detectProjectEAcceleration() {
        if (!isProjectELoaded()) {
            projecteBoost = false;
            return;
        }

        AABB searchArea = new AABB(
                getBlockPos().offset(-8, -8, -8),
                getBlockPos().offset(8, 8, 8)
        );

        List<Player> players = level.getEntitiesOfClass(Player.class, searchArea);
        boolean foundTimeWatch = false;

        for (Player player : players) {
            if (hasActiveTimeWatch(player)) {
                foundTimeWatch = true;
                acceleratedTicks++;
                break;
            }
        }

        if (!foundTimeWatch) {
            foundTimeWatch = hasActivePedestal();
            if (foundTimeWatch) {
                acceleratedTicks++;
            }
        }

        boolean wasBoost = projecteBoost;
        projecteBoost = foundTimeWatch;

        if (wasBoost != projecteBoost) {
            setChanged();
            syncToClient();
        }

        if (projecteBoost) {

            simulatedTime += 4;

            if (acceleratedTicks % 5 == 0) {
                processTimeLogic();
            }
        }

        if (acceleratedTicks >= 100) {
            acceleratedTicks = 0;
        }
    }

    private boolean hasActiveTimeWatch(Player player) {
        try {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    String itemClass = stack.getItem().getClass().getSimpleName();
                    if (itemClass.equals("TimeWatch") && stack.hasTag()) {
                        CompoundTag tag = stack.getTag();
                        if (tag != null) {
                            boolean hasActive = tag.getBoolean("Active");
                            byte timeMode = tag.getByte("TimeMode");

                            return hasActive && timeMode == 1;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean hasActivePedestal() {
        try {
            AABB pedestalArea = new AABB(
                    getBlockPos().offset(-8, -8, -8),
                    getBlockPos().offset(8, 8, 8)
            );

            for (int x = (int)pedestalArea.minX; x <= pedestalArea.maxX; x++) {
                for (int y = (int)pedestalArea.minY; y <= pedestalArea.maxY; y++) {
                    for (int z = (int)pedestalArea.minZ; z <= pedestalArea.maxZ; z++) {
                        BlockPos checkPos = new BlockPos(x, y, z);
                        if (level.isLoaded(checkPos)) {
                            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(checkPos);
                            if (be != null) {
                                String beClass = be.getClass().getSimpleName();
                                if (beClass.contains("Pedestal") || beClass.contains("DM")) {
                                    if (checkPedestalTimeWatch(be)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean checkPedestalTimeWatch(net.minecraft.world.level.block.entity.BlockEntity be) {
        try {
            String[] possibleMethods = {"getInventory", "getStack", "getItem", "getStoredStack"};

            for (String methodName : possibleMethods) {
                try {
                    java.lang.reflect.Method method = be.getClass().getMethod(methodName);
                    if (method != null) {
                        Object result = method.invoke(be);

                        if (result instanceof IItemHandler) {
                            IItemHandler itemHandler = (IItemHandler) result;
                            ItemStack pedestalStack = itemHandler.getStackInSlot(0);
                            return isValidTimeWatch(pedestalStack);
                        } else if (result instanceof ItemStack) {
                            ItemStack pedestalStack = (ItemStack) result;
                            return isValidTimeWatch(pedestalStack);
                        }
                    }
                } catch (Exception methodEx) {
                    continue;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean isValidTimeWatch(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem().getClass().getSimpleName().equals("TimeWatch")) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                byte timeMode = tag.getByte("TimeMode");
                return timeMode == 1;
            }
        }
        return false;
    }

    private void processTimeLogic() {
        long realTime = level.getDayTime();

        long effectiveTime = realTime + simulatedTime;
        long currentDay = effectiveTime / 24000L;

        if (currentDay > lastDayCheck) {
            if (getMana() < ConfigHandler.FlowerConfig.getAstralKillopManaCost()) {
                lastDayCheck = currentDay;
                setChanged();
                syncToClient();
                return;
            }

            addMana(-ConfigHandler.FlowerConfig.getAstralKillopManaCost());
            days++;
            lastDayCheck = currentDay;
            setChanged();

            ItemStack dropStack;
            int count = 1;

            if (days == ConfigHandler.FlowerConfig.getAstralKillopEffectDay()) {
                dropStack = new ItemStack(ModItems.ASTRAL_PILE.get());
                count = ConfigHandler.FlowerConfig.getAstralKillopEffectDropCount();

                int effectRange = ConfigHandler.FlowerConfig.getAstralKillopRange();
                AABB area = new AABB(
                        getEffectivePos().offset(-effectRange, -effectRange, -effectRange),
                        getEffectivePos().offset(effectRange, effectRange, effectRange)
                );
                List<Player> players = level.getEntitiesOfClass(Player.class, area);

                for (Player player : players) {
                    int effectDuration = ConfigHandler.FlowerConfig.getAstralKillopEffectDuration();
                    int effectLevel = ConfigHandler.FlowerConfig.getAstralKillopEffectLevel();
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, effectDuration, effectLevel));
                }

                days = 0;
            }
            else if (days == ConfigHandler.FlowerConfig.getAstralKillopNuggetDay()) {
                dropStack = new ItemStack(ModItems.ASTRAL_NUGGET.get());
            }
            else {
                dropStack = new ItemStack(ModItems.ASTRAL_PILE.get());
            }

            for (int i = 0; i < count; i++) {
                ItemEntity itemEntity = new ItemEntity(level,
                        getEffectivePos().getX() + 0.5,
                        getEffectivePos().getY() + 1.0,
                        getEffectivePos().getZ() + 0.5,
                        dropStack.copy());

                itemEntity.setDeltaMovement(
                        (level.random.nextDouble() - 0.5) * 0.1,
                        0.2,
                        (level.random.nextDouble() - 0.5) * 0.1
                );

                level.addFreshEntity(itemEntity);
            }

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        getEffectivePos().getX() + 0.5,
                        getEffectivePos().getY() + 1.0,
                        getEffectivePos().getZ() + 0.5,
                        20, 0.5, 0.5, 0.5, 0.1);

                if (days == 0) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            getEffectivePos().getX() + 0.5,
                            getEffectivePos().getY() + 1.0,
                            getEffectivePos().getZ() + 0.5,
                            50, 1.0, 1.0, 1.0, 0.2);
                }

                level.playSound(null, getEffectivePos(),
                        SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            syncToClient();
        }
    }

    private boolean isProjectELoaded() {
        return ModList.get().isLoaded("projecte");
    }

    private void syncToClient() {
        if (level instanceof ServerLevel) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeToPacketNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readFromPacketNBT(tag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        tag.putInt(TAG_DAYS, days);
        tag.putLong(TAG_LAST_DAY_CHECK, lastDayCheck);
        tag.putLong(TAG_PLACEMENT_TIME, placementTime);
        tag.putBoolean(TAG_INITIALIZED, initialized);
        tag.putInt(TAG_ACCELERATED_TICKS, acceleratedTicks);
        tag.putBoolean(TAG_PROJECTE_BOOST, projecteBoost);
        tag.putLong(TAG_SIMULATED_TIME, simulatedTime);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        days = tag.getInt(TAG_DAYS);
        lastDayCheck = tag.getLong(TAG_LAST_DAY_CHECK);
        placementTime = tag.getLong(TAG_PLACEMENT_TIME);
        initialized = tag.getBoolean(TAG_INITIALIZED);
        acceleratedTicks = tag.getInt(TAG_ACCELERATED_TICKS);
        projecteBoost = tag.getBoolean(TAG_PROJECTE_BOOST);
        simulatedTime = tag.getLong(TAG_SIMULATED_TIME);
    }

    @Override
    public int getMaxMana() {
        return ConfigHandler.FlowerConfig.getAstralKillopMaxMana();
    }

    @Override
    public int getColor() {
        return 0x00008B;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BotaniaForgeClientCapabilities.WAND_HUD.orEmpty(cap,
                LazyOptional.of(() -> new FunctionalWandHud(this)).cast());
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), ConfigHandler.FlowerConfig.getAstralKillopRange());
    }

    public int getDays() {
        return days;
    }

    public int getAcceleratedTicks() {
        return acceleratedTicks;
    }

    public boolean isProjecteBoost() {
        return projecteBoost;
    }
}