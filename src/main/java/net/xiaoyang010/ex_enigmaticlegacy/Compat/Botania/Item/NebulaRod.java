package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ClientHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Model.Vector3;
import net.xiaoyang010.ex_enigmaticlegacy.Config.ConfigHandler;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.client.fx.WispParticleData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;

public class NebulaRod extends Item {
    private static final int MAX_MANA = 500000;
    private static final int MANA_PER_USE = 5000;

    public NebulaRod(Properties properties) {
        super(properties.durability(100).setNoRepair().fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() == 0 && checkWorld(level.dimension().location().getPath())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    private boolean checkWorld(String name) {
        for(Object str : ConfigHandler.lockWorldNameNebulaRod) {
            if (str.equals(name)) return false;
        }
        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int time = getUseDuration(stack) - timeLeft;
        if (time > 110 && !player.isCrouching()) {
            if (!level.isClientSide) {

                if (!tryConsumeMana(stack, player, MANA_PER_USE, true)) {
                    player.stopUsingItem();
                    player.sendMessage(
                            new TranslatableComponent("ex_enigmaticlegacy.nebulaRod.noMana")
                                    .withStyle(ChatFormatting.DARK_PURPLE),
                            player.getUUID()
                    );
                    return;
                }

                BlockPos targetPos = getTopBlock(level, player);
                if (targetPos == null) {
                    player.stopUsingItem();
                    player.sendMessage(
                            new TranslatableComponent("ex_enigmaticlegacy.nebulaRod.notTeleporting")
                                    .withStyle(ChatFormatting.DARK_PURPLE),
                            player.getUUID()
                    );
                    return;
                }

                EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(
                        player,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 0.5,
                        targetPos.getZ() + 0.5
                );

                if (MinecraftForge.EVENT_BUS.post(event)) {
                    player.stopUsingItem();
                    player.sendMessage(
                            new TranslatableComponent("ex_enigmaticlegacy.nebulaRod.notTeleportingEvent")
                                    .withStyle(ChatFormatting.DARK_PURPLE),
                            player.getUUID()
                    );
                    return;
                }

                tryConsumeMana(stack, player, MANA_PER_USE, false);

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.teleport(
                            event.getTargetX(),
                            event.getTargetY(),
                            event.getTargetZ(),
                            player.getYRot(),
                            player.getXRot()
                    );
                }

                level.playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PORTAL_TRIGGER,
                        SoundSource.PLAYERS, 1.2F, 1.2F
                );
            }

            if (!player.getAbilities().instabuild) {
                stack.setDamageValue(100);
            }

            player.stopUsingItem();
        }

        spawnPortalParticle(level, player, time,
                level.random.nextBoolean() ? 9641964 : 4920962, 1.0F);
    }

    private void spawnPortalParticle(Level level, Player player, int time, int color, float particleTime) {
        if (!level.isClientSide) return;

        if (time % 10 == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.1F, 1.0F, false);
        }

        boolean isFinish = time > 80;
        int ticks = Math.min(100, time);
        int totalSpiritCount = (int)Math.max(3.0F, (float)ticks / 100.0F * 18.0F);
        double tickIncrement = (double)360.0F / (double)totalSpiritCount;
        int speed = 8;
        double wticks = (double)(ticks * speed) - tickIncrement;

        double r = Math.sin((double)ticks / (double)100.0F) * Math.max((double)0.75F, 1.4 * (double)ticks / (double)100.0F);

        Vec3 look = player.getViewVector(1.0F);
        float yawOffset = Minecraft.getInstance().player == player ? 0.0F : 1.62F;

        Vector3 l = new Vector3(look.x, look.y + (double)yawOffset, look.z);
        Vector3 playerPos = Vector3.fromEntity(player).add(0.0, 0.0, 0.0);
        Vector3 v3 = new Vector3();

        // ========== 位置调整参数 ==========
        double forwardDistance = 0.5;    // 前方距离（Z轴）- 调整这个改变距离远近
        double sideOffset = 0.0;         // 左右偏移（相对于视线）
        double upOffset = 0.0;           // 上下偏移（相对于视线）
        double viewDirectionMultiplier = 1.0;  // 视线方向倍数

        // 额外的世界坐标偏移（不受视角影响）
        double worldOffsetX = 0.0;       // 世界X轴偏移
        double worldOffsetY = 1.65;       // 世界Y轴偏移
        double worldOffsetZ = 0.0;       // 世界Z轴偏移
        // ================================

        for(int i = 0; i < totalSpiritCount; ++i) {
            float size = Math.max(0.215F, (float)ticks / 100.0F);

            // 基础圆形位置 + 可调整的偏移
            v3.set(
                    Math.sin(wticks * Math.PI / 180.0) / 1.825 * r + sideOffset,   // X + 左右偏移
                    Math.cos(wticks * Math.PI / 180.0) * r + upOffset,             // Y + 上下偏移
                    forwardDistance                                                 // Z = 前方距离
            );

            ClientHelper.setRotation(player.getXRot(), 1.0F, 0.0F, 0.0F, v3);
            ClientHelper.setRotation(-player.getYRot(), 0.0F, 1.0F, 0.0F, v3);

            v3.add(l.copy().multiply(viewDirectionMultiplier)).add(playerPos)
                    .add(worldOffsetX, worldOffsetY, worldOffsetZ);

            wticks += tickIncrement;

            float[] hsb = Color.RGBtoHSB(color & 255, color >> 8 & 255, color >> 16 & 255, null);
            int color1 = Color.HSBtoRGB(hsb[0], hsb[1], (float)ticks / 100.0F);
            float[] colorsfx = new float[]{
                    (float)(color1 & 255) / 255.0F,
                    (float)(color1 >> 8 & 255) / 255.0F,
                    (float)(color1 >> 16 & 255) / 255.0F
            };

            float motionSpeed = 0.25F * Math.min(1.0F, (float)(time - 80) / 30.0F);

            WispParticleData mainWisp = WispParticleData.wisp(
                    0.3F * size,
                    colorsfx[0], colorsfx[1], colorsfx[2],
                    0.3F * particleTime
            );

            level.addParticle(mainWisp,
                    v3.x, v3.y, v3.z,
                    isFinish ? (float)(look.x * -1.0) * motionSpeed : 0.0F,
                    isFinish ? (float)(look.y * -1.0) * motionSpeed : 0.0F,
                    isFinish ? (float)(look.z * -1.0) * motionSpeed : 0.0F
            );

            WispParticleData randomWisp = WispParticleData.wisp(
                    (float)(Math.random() * 0.1F + 0.05F) * size,
                    colorsfx[0], colorsfx[1], colorsfx[2],
                    0.4F * particleTime
            );

            level.addParticle(randomWisp,
                    v3.x, v3.y, v3.z,
                    (float)(Math.random() - 0.5F) * 0.05F,
                    (float)(Math.random() - 0.5F) * 0.05F,
                    (float)(Math.random() - 0.5F) * 0.05F
            );
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    private BlockPos getTopBlock(Level level, Player player) {
        Vec3 lookVec = player.getViewVector(1.0F).normalize();
        int limitXZ = ConfigHandler.limitXZCoords;

        for (int nextPos = 256; nextPos > 8; --nextPos) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                    (int) (player.getX() + lookVec.x * nextPos),
                    0,
                    (int) (player.getZ() + lookVec.z * nextPos)
            );

            pos.setX(Math.min(Math.max(pos.getX(), -(limitXZ - 1)), limitXZ - 1));
            pos.setZ(Math.min(Math.max(pos.getZ(), -(limitXZ - 1)), limitXZ - 1));

            for (int y = level.getMaxBuildHeight(); y > 0; --y) {
                pos.setY(y);
                BlockState state = level.getBlockState(pos);
                boolean hasTopAir =
                        level.getBlockState(pos.above()).isAir() &&
                                level.getBlockState(pos.above(2)).isAir();

                if (!state.isAir() &&
                        state.getBlock() != Blocks.BEDROCK &&
                        hasTopAir) {
                    return pos.above().immutable();
                }
            }
        }

        return null;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ManaItemCapabilityProvider(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new ManaBarTooltip(getManaInternal(stack) / (float) MAX_MANA));
    }

    protected int getManaInternal(ItemStack stack) {
        return stack.getOrCreateTag().getInt("mana");
    }

    protected void setManaInternal(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt("mana", Math.min(mana, MAX_MANA));
    }

    protected boolean tryConsumeMana(ItemStack stack, Player player, int amount, boolean simulate) {
        if (getManaInternal(stack) >= amount) {
            if (!simulate) {
                setManaInternal(stack, getManaInternal(stack) - amount);
            }
            return true;
        }
        return false;
    }

    private static class ManaItemCapabilityProvider implements ICapabilityProvider {
        private final ItemStack stack;
        private final LazyOptional<IManaItem> manaItemOptional;

        public ManaItemCapabilityProvider(ItemStack stack) {
            this.stack = stack;
            this.manaItemOptional = LazyOptional.of(() -> new ManaItemImpl(stack));
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
                return manaItemOptional.cast();
            }
            return LazyOptional.empty();
        }
    }

    private static class ManaItemImpl implements IManaItem {
        private final ItemStack stack;

        public ManaItemImpl(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return stack.getOrCreateTag().getInt("mana");
        }

        @Override
        public int getMaxMana() {
            return MAX_MANA;
        }

        @Override
        public void addMana(int mana) {
            int current = getMana();
            stack.getOrCreateTag().putInt("mana", Math.min(current + mana, MAX_MANA));
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            return true;
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
            return false;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean isNoExport() {
            return true;
        }
    }
}