package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.TChest.ContainerItemChest;
import net.xiaoyang010.ex_enigmaticlegacy.Util.EComponent;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.ItemRelic;
import vazkii.botania.common.item.relic.RelicImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TalismanHiddenRiches extends ItemRelic implements INoEMCItem {
    protected static final List<CustomChestEntity> chestList = new ArrayList<>();
    protected static final ResourceLocation glowTexture = new ResourceLocation("ex_enigmaticlegacy:textures/misc/glow3.png");
    protected static final int segmentCount = 11;
    private static final Map<Integer, Boolean> chestSoundPlayed = new HashMap<>();
    private static final Map<Integer, Boolean> prevChestOpen = new HashMap<>();

    public TalismanHiddenRiches(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CustomChestEntity extends ChestBlockEntity {
        private boolean shouldBeOpen = false;
        private boolean wasOpen = false;

        public CustomChestEntity() {
            super(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
        }

        public void setOpen(boolean open) {
            this.shouldBeOpen = open;
        }

        public boolean isOpen() {
            return shouldBeOpen;
        }

        public void updateLidAnimation() {
            ChestBlockEntity.lidAnimateTick(null, this.getBlockPos(), this.getBlockState(), this);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!(entity instanceof Player player)) return;

        boolean eqLastTick = wasEquipped(stack);

        if (!selected && eqLastTick) {
            setEquipped(stack, false);
            if (world.isClientSide) {
                for (int i = 0; i < segmentCount; i++) {
                    closeChest(i);
                }
            }
        }

        if (!eqLastTick && selected) {
            setEquipped(stack, true);
            int angles = 360;
            int segAngles = angles / 16;
            float shift = (float) segAngles / 2.0F * 11.0F;
            setRotationBase(stack, getCheckingAngle(player) - shift);
        }

        if (world.isClientSide && selected) {
            updateChestAnimations(stack, player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void updateChestAnimations(ItemStack stack, Player player) {
        int openChest = getOpenChest(stack);

        for (int i = 0; i < segmentCount; i++) {
            CustomChestEntity chest = getChestForSegment(i);
            if (chest != null) {
                boolean shouldBeOpen = (i == openChest);
                boolean wasOpen = prevChestOpen.getOrDefault(i, false);

                if (shouldBeOpen && !wasOpen) {
                    if (!chestSoundPlayed.getOrDefault(i, false)) {
                        playChestSound(player, SoundEvents.CHEST_OPEN);
                        chestSoundPlayed.put(i, true);
                    }
                    chest.triggerEvent(1, 1);
                } else if (!shouldBeOpen && wasOpen) {
                    playChestSound(player, SoundEvents.CHEST_CLOSE);
                    chestSoundPlayed.put(i, false);
                    chest.triggerEvent(1, 0);
                }

                chest.setOpen(shouldBeOpen);
                chest.updateLidAnimation();
                prevChestOpen.put(i, shouldBeOpen);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void closeChest(int segment) {
        CustomChestEntity chest = getChestForSegment(segment);
        if (chest != null && chest.isOpen()) {
            chest.triggerEvent(1, 0);
            chest.setOpen(false);
            chest.updateLidAnimation();
            prevChestOpen.put(segment, false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playChestSound(Player player, net.minecraft.sounds.SoundEvent sound) {
        ClientLevel world = (ClientLevel) player.level;
        world.playLocalSound(player.getX(), player.getY() - 0.5, player.getZ(),
                sound, SoundSource.PLAYERS, 0.5F,
                world.random.nextFloat() * 0.1F + 0.9F, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(stack);
            }
        }

        int segment = getSegmentLookedAt(stack, player);

        if (segment == -1) {
            return InteractionResultHolder.pass(stack);
        }

        setOpenChest(stack, segment);

        if (!world.isClientSide) {
            NetworkHooks.openGui((net.minecraft.server.level.ServerPlayer) player,
                    new net.minecraft.world.MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return EComponent.translatable("container.chest")
                                    .append(" - ")
                                    .append(EComponent.translatable("item.ex_enigmaticlegacy.talisman_hidden_riches.name"))
                                    .append(" #" + (segment + 1));
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
                            return new ContainerItemChest(id, playerInventory, player);
                        }
                    });
        }

        return InteractionResultHolder.success(stack);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLast(RenderLevelLastEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof TalismanHiddenRiches)) return;

        render(stack, player, event.getPoseStack(), event.getPartialTick());
    }

    @OnlyIn(Dist.CLIENT)
    public void render(ItemStack stack, Player player, PoseStack poseStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();

        float alpha = ((float) Math.sin(ClientTickHandler.total() * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;

        double posX = player.xOld + (player.getX() - player.xOld) * partialTicks;
        double posY = player.yOld + (player.getY() - player.yOld) * partialTicks;
        double posZ = player.zOld + (player.getZ() - player.zOld) * partialTicks;

        var camera = mc.gameRenderer.getMainCamera();
        poseStack.translate(
                posX - camera.getPosition().x,
                posY - camera.getPosition().y + 1.8F,
                posZ - camera.getPosition().z
        );

        float base = getRotationBase(stack);
        int angles = 360;
        int segAngles = angles / 16;
        float shift = base - (float)segAngles / 2.0F * 11.0F;

        float s = 3.2F;
        float m = 0.8F;
        int segmentLookedAt = getSegmentLookedAt(stack, player);

        for (int seg = 0; seg < segmentCount; seg++) {
            renderChestSegment(poseStack, stack, player, seg, segmentLookedAt,
                    partialTicks, alpha, shift, segAngles, s, m);
        }

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderChestSegment(PoseStack poseStack, ItemStack stack, Player player, int seg,
                                    int segmentLookedAt, float partialTicks, float alpha,
                                    float shift, int segAngles, float s, float m) {
        float rotationAngle = ((float)seg + 0.5F) * (float)segAngles + shift;

        poseStack.pushPose();

        poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationAngle));
        poseStack.translate(s * m, -0.75F, 0.0F);

        double worldTime = ClientTickHandler.total() + (float)seg * 2.75F;
        poseStack.translate(0.375F, Math.sin(worldTime / 8.0F) / 20.0F, -0.375F);

        float scale = 0.75F;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));

        CustomChestEntity chest = getChestForSegment(seg);
        if (chest != null) {
            try {
                Minecraft mc = Minecraft.getInstance();
                MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

                BlockEntityRenderer<ChestBlockEntity> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(chest);

                if (renderer != null) {
                    int lightLevel = 15728880;
                    int overlay = 0;

                    renderer.render(chest, partialTicks, poseStack, bufferSource, lightLevel, overlay);
                    bufferSource.endBatch();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        poseStack.popPose();

        renderGlowEffect(poseStack, stack, seg, segmentLookedAt, alpha, shift, segAngles, s, m);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderGlowEffect(PoseStack poseStack, ItemStack stack, int seg, int segmentLookedAt,
                                  float alpha, float shift, int segAngles, float s, float m) {
        poseStack.pushPose();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));

        float a = alpha;
        float y = 0.25F * 6.0F;
        float y0 = 0.0F;

        if (segmentLookedAt == seg) {
            a = alpha + 0.3F;
            y0 = -y;
        }

        if (seg % 2 == 0) {
            RenderSystem.setShaderColor(0.6F, 0.6F, 0.6F, a);
        } else {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, a);
        }

        RenderSystem.setShaderTexture(0, glowTexture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < segAngles; i++) {
            float ang = (float) (i + seg * segAngles) + shift;
            double xp = Math.cos(Math.toRadians(ang)) * s;
            double zp = Math.sin(Math.toRadians(ang)) * s;

            buffer.vertex(matrix, (float)(xp * m), y, (float)(zp * m)).uv(1.0F, 0.25F).endVertex();
            buffer.vertex(matrix, (float)xp, y0, (float)zp).uv(1.0F, 0.0F).endVertex();

            xp = Math.cos(Math.toRadians(ang + 1.0F)) * s;
            zp = Math.sin(Math.toRadians(ang + 1.0F)) * s;

            buffer.vertex(matrix, (float)xp, y0, (float)zp).uv(0.0F, 0.0F).endVertex();
            buffer.vertex(matrix, (float)(xp * m), y, (float)(zp * m)).uv(0.0F, 0.25F).endVertex();
        }

        tesselator.end();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        poseStack.popPose();
    }

    protected static int getSegmentLookedAt(ItemStack stack, LivingEntity player) {
        float yaw = getCheckingAngle(player, getRotationBase(stack));
        int angles = 360;
        int segAngles = angles / 16;

        for (int seg = 0; seg < segmentCount; seg++) {
            float calcAngle = (float) (seg * segAngles);
            if (yaw >= calcAngle && yaw < calcAngle + (float) segAngles) {
                return seg;
            }
        }
        return -1;
    }

    protected static float getCheckingAngle(LivingEntity player, float base) {
        float yaw = Mth.wrapDegrees(player.getYRot()) + 90.0F;
        int angles = 360;
        int segAngles = angles / 16;
        float shift = (float) segAngles / 2.0F * 11.0F;

        if (yaw < 0.0F) {
            yaw += 360.0F;
        }

        yaw -= 360.0F - base;
        float angle = 360.0F - yaw + shift;

        if (angle > 360.0F) {
            angle %= 360.0F;
        }

        return angle;
    }

    protected static float getCheckingAngle(LivingEntity player) {
        return getCheckingAngle(player, 0.0F);
    }

    public static void setEquipped(ItemStack stack, boolean equipped) {
        ItemNBTHelper.setBoolean(stack, "equipped", equipped);
    }

    public static boolean wasEquipped(ItemStack stack) {
        return ItemNBTHelper.getBoolean(stack, "equipped", false);
    }

    public static void setRotationBase(ItemStack stack, float rotation) {
        ItemNBTHelper.setFloat(stack, "rotationBase", rotation);
    }

    public static float getRotationBase(ItemStack stack) {
        return ItemNBTHelper.getFloat(stack, "rotationBase", 0.0F);
    }

    public static void setOpenChest(ItemStack stack, int segment) {
        ItemNBTHelper.setInt(stack, "openChest", segment);
    }

    public static int getOpenChest(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, "openChest", -1);
    }

    @OnlyIn(Dist.CLIENT)
    public static CustomChestEntity getChestForSegment(int segment) {
        if (chestList.isEmpty()) {
            for (int i = 0; i < segmentCount; i++) {
                CustomChestEntity chest = new CustomChestEntity();
                chestList.add(chest);
            }
        }

        return segment >= 0 && segment < segmentCount ? chestList.get(segment) : null;
    }

    public static void setChestLoot(ItemStack stack, ItemStack[] loot, int segment) {
        ListTag nbtList = new ListTag();

        for (int i = 0; i < loot.length; i++) {
            ItemStack item = loot[i];
            if (item != null && !item.isEmpty()) {
                CompoundTag cmp = new CompoundTag();
                cmp.putByte("slot", (byte) i);
                item.save(cmp);
                nbtList.add(cmp);
            }
        }

        ItemNBTHelper.setList(stack, "chestLoot" + segment, nbtList);
    }

    public static ItemStack[] getChestLoot(ItemStack stack, int segment) {
        if (segment >= segmentCount) {
            return new ItemStack[27];
        }

        ItemStack[] loot = new ItemStack[27];
        for (int i = 0; i < loot.length; i++) {
            loot[i] = ItemStack.EMPTY;
        }

        ListTag nbtList = ItemNBTHelper.getList(stack, "chestLoot" + segment, 10, false);

        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag cmp = nbtList.getCompound(i);
            byte slotCount = cmp.getByte("slot");
            if (slotCount >= 0 && slotCount < loot.length) {
                ItemStack loadedStack = ItemStack.of(cmp);
                if (!loadedStack.isEmpty()) {
                    loot[slotCount] = loadedStack;
                }
            }
        }

        return loot;
    }
}