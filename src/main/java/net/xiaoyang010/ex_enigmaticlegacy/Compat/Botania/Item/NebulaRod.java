package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Client.ConfigHandler;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.client.fx.WispParticleData;

import java.awt.*;
import java.util.Optional;

public class NebulaRod extends Item implements IManaItem {
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
        for (String str : ConfigHandler.lockWorldNameNebulaRod) {
            if (str.equals(name)) return false;
        }
        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        int time = getUseDuration(stack) - timeLeft;
        if (time > 20 && !player.isCrouching()) {
            if (!level.isClientSide) {

//                if (!tryConsumeMana(stack, player, MANA_PER_USE, true) || !player.isCreative()) {
//                    player.stopUsingItem();
//                    player.sendMessage(new TranslatableComponent("ex_enigmaticlegacy.nebulaRod.noMana").withStyle(ChatFormatting.DARK_PURPLE), player.getUUID());
//                    return;
//                } 魔力系統接入？？？

                BlockPos targetPos = getTopBlock(level, player); //准心坐標
                if (targetPos == null) {
                    player.stopUsingItem();
                    player.sendMessage(new TranslatableComponent("ex_enigmaticlegacy.nebulaRod.notTeleporting").withStyle(ChatFormatting.DARK_PURPLE), player.getUUID());
                    return;
                }

                EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(player, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);

                if (MinecraftForge.EVENT_BUS.post(event)) {
                    player.stopUsingItem();
                    player.sendMessage(new TranslatableComponent("ex_enigmaticlegacy.nebulaRod.notTeleportingEvent").withStyle(ChatFormatting.DARK_PURPLE), player.getUUID());
                    return;
                }

                tryConsumeMana(stack, player, MANA_PER_USE, false);

                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.teleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), player.getYRot(), player.getXRot());
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.2F, 1.2F);
                spawnPortalParticle(level, player, time, level.random.nextBoolean() ? 9641964 : 4920962, 1.0F);
            }

            if (!player.getAbilities().instabuild) {
                stack.setDamageValue(100);
            }

            player.stopUsingItem();
        }

    }

    private void spawnPortalParticle(Level level, Player player, int time, int color, float particleTime) {
        if (!level.isClientSide) return;

        boolean isFinish = time > 80;
        int ticks = Math.min(100, time);
        int totalSpiritCount = (int) Math.max(3.0F, (float) ticks / 100.0F * 18.0F);
        double tickIncrement = (double) 360.0F / (double) totalSpiritCount;
        double wticks = (double) (ticks * 8) - tickIncrement;
        double r = Math.sin((double) ticks / (double) 100.0F) * Math.max((double) 0.75F, 1.4 * (double) ticks / (double) 100.0F);

        Vec3 look = player.getViewVector(1.0F);
        float yawOffset = Minecraft.getInstance().player == player ? 0.0F : 1.62F;
        Vec3 playerPos = player.position();

        if (time % 10 == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.1F, 1.0F, false);
        }

        for (int i = 0; i < totalSpiritCount; ++i) {
            float size = Math.max(0.215F, (float) ticks / 100.0F);

            double px = Math.sin(wticks * Math.PI / 180.0) / 1.825 * r;
            double py = Math.cos(wticks * Math.PI / 180.0) * r;
            double pz = 0.8;

            float pitch = player.getXRot();
            float yaw = player.getYRot();

            double xp = pz * Math.sin(yaw * Math.PI / 180) + px * Math.cos(yaw * Math.PI / 180);
            double zp = pz * Math.cos(yaw * Math.PI / 180) - px * Math.sin(yaw * Math.PI / 180);
            double yp = py - Math.sin(pitch * Math.PI / 180) * pz;

            Vec3 particlePos = playerPos.add(
                    look.x + xp,
                    look.y + yp + yawOffset,
                    look.z + zp
            );

            wticks += tickIncrement;

            float[] hsb = Color.RGBtoHSB(color & 255, color >> 8 & 255, color >> 16 & 255, null);
            int color1 = Color.HSBtoRGB(hsb[0], hsb[1], (float) ticks / 100.0F);
            float[] colorsfx = new float[]{
                    (float) (color1 & 255) / 255.0F,
                    (float) (color1 >> 8 & 255) / 255.0F,
                    (float) (color1 >> 16 & 255) / 255.0F
            };

            float motionSpeed = 0.25F * Math.min(1.0F, (float) (time - 80) / 30.0F);

            WispParticleData mainWisp = WispParticleData.wisp(
                    0.3F * size,
                    colorsfx[0], colorsfx[1], colorsfx[2],
                    1.0F
            );

            level.addParticle(mainWisp,
                    particlePos.x, particlePos.y, particlePos.z,
                    isFinish ? -look.x * motionSpeed : 0,
                    isFinish ? -look.y * motionSpeed : 0,
                    isFinish ? -look.z * motionSpeed : 0);

            float randomSize = (float) (Math.random() * 0.1F + 0.05F) * size;
            float randMotionX = (float) (Math.random() - 0.5F) * 0.05F;
            float randMotionY = (float) (Math.random() - 0.5F) * 0.05F;
            float randMotionZ = (float) (Math.random() - 0.5F) * 0.05F;

            WispParticleData randomWisp = WispParticleData.wisp(
                    randomSize,
                    colorsfx[0], colorsfx[1], colorsfx[2],
                    1.0F
            );

            level.addParticle(randomWisp,
                    particlePos.x, particlePos.y, particlePos.z,
                    randMotionX, randMotionY, randMotionZ);
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
        Vec3 vec3 = player.getLookAngle();
        BlockPos pos1 = new BlockPos(vec3.x, vec3.y, vec3.z);
        return pos1;
        /*
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

        return null;*/
    }

    @Override
    public int getMana() {
        return getManaInternal(new ItemStack(this));
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public void addMana(int mana) {
        setManaInternal(new ItemStack(this), getMana() + mana);
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
}