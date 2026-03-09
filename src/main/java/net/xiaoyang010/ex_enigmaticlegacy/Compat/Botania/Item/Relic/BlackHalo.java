package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IBlockProvider;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.ItemBlackHoleTalisman;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.IXplatAbstractions;

import java.util.List;

public class BlackHalo extends Item implements IBlockProvider, INoEMCItem {
    public static final ResourceLocation GLOW_TEXTURE = new ResourceLocation("ex_enigmaticlegacy:textures/misc/glow.png");
    private static final float BLOCK_RADIAL_OFFSET = 0.0F;      // 径向偏移：正值向外，负值向内  // 稍微向外
    private static final float BLOCK_HEIGHT_OFFSET = -0.3F;      // 高度偏移：正值向上，负值向下 // 稍微向上
    private static final float BLOCK_TANGENTIAL_OFFSET = 0.3F; // 切向偏移：正值顺时针，负值逆时针 // 向外移动
    private static final float BLOCK_SCALE_ADJUSTMENT = 1.0F;   // 缩放调整：1.0为原始大小 // 向内移动

    public BlackHalo(Properties properties) {
        super(properties);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @org.jetbrains.annotations.Nullable CompoundTag nbt) {
        return new RelicCapProvider(stack);
    }

    private static class RelicCapProvider implements ICapabilityProvider {
        private final LazyOptional<IRelic> relic;

        public RelicCapProvider(ItemStack stack) {
            this.relic = LazyOptional.of(() -> new RelicImpl(stack, null));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @org.jetbrains.annotations.Nullable Direction direction) {
            if (capability == BotaniaForgeCapabilities.RELIC) {
                return relic.cast();
            }
            return LazyOptional.empty();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flags) {
        RelicImpl.addDefaultTooltip(stack, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack halo = player.getItemInHand(hand);

        var relicCap = halo.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(halo);
            }
        }

        int segment = getSegmentLookedAt(halo, player);

        if (getItemForSlot(halo, segment) != null) {
            ItemStack stack = getItemForSlot(halo, segment).copy();
            if (player.isShiftKeyDown()) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                this.setItemSlot(halo, null, segment);
                return InteractionResultHolder.success(halo);
            } else {
                stack.setDamageValue(stack.getDamageValue() == 0 ? 1 : 0);
                if (!world.isClientSide) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, player.getSoundSource(), 0.3F, 0.1F);
                }
                this.setItemSlot(halo, stack, segment);
                return InteractionResultHolder.success(halo);
            }
        } else {
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof ItemBlackHoleTalisman) {

                    Block block = ItemBlackHoleTalisman.getBlock(stack);
                    boolean hasBlock = false;

                    if (block != null && block != Blocks.AIR) {
                        int count = ItemBlackHoleTalisman.getBlockCount(stack);
                        if (count > 0) {
                            hasBlock = true;
                        }
                    }

                    if (hasBlock) {
                        if (getItemForSlot(halo, segment) != null) {
                            return InteractionResultHolder.success(halo);
                        }

                        this.setItemSlot(halo, stack.copy(), segment);

                        if (stack.getCount() > 1) {
                            stack.shrink(1);
                        } else {
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                        }

                        return InteractionResultHolder.success(halo);
                    }
                }
            }

            return InteractionResultHolder.success(halo);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack halo, Level world, Entity entity, int slot, boolean selected) {
        boolean eqLastTick = wasEquipped(halo);

        if (!world.isClientSide && entity instanceof Player player) {
            var relic = IXplatAbstractions.INSTANCE.findRelic(halo);
            if (relic != null) {
                relic.tickBinding(player);
            }
        }

        if (!selected && eqLastTick) {
            setEquipped(halo, selected);
        }

        if (!eqLastTick && selected && entity instanceof LivingEntity) {
            setEquipped(halo, selected);
            int angles = 360;
            int segAngles = angles / 12;
            float shift = (float)(segAngles / 2);
            setRotationBase(halo, getCheckingAngle((LivingEntity)entity) - shift);
        }

        if (!world.isClientSide) {
            if (entity.tickCount % 10 == 0) {
                if (entity instanceof Player player) {
                    var relicCap = halo.getCapability(BotaniaForgeCapabilities.RELIC);
                    if (relicCap.isPresent()) {
                        IRelic relic = relicCap.orElse(null);
                        if (relic != null && !relic.isRightPlayer(player)) {
                            return;
                        }
                    }
                }

                for (int i = 0; i < 12; ++i) {
                    ItemStack stack = getItemForSlot(halo, i);
                    if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemBlackHoleTalisman) {
                        stack.inventoryTick(world, entity, slot, selected);
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        ItemStack halo = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        var relicCap = halo.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResult.FAIL;
            }
        }

        for (int i = 0; i < 12; ++i) {
            ItemStack tal = getItemForSlot(halo, i);
            if (tal != null && !tal.isEmpty() && tal.getItem() instanceof ItemBlackHoleTalisman) {
                Block bBlock = ItemBlackHoleTalisman.getBlock(tal);
                if (bBlock == null) continue;

                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof Container inv) {
                    int[] slots = inv instanceof WorldlyContainer sidedInv ?
                            sidedInv.getSlotsForFace(side) :
                            buildSlotsForLinearInventory(inv);

                    for (int slotIndex : slots) {
                        ItemStack stackInSlot = inv.getItem(slotIndex);
                        if (stackInSlot.isEmpty()) {
                            ItemStack newStack = new ItemStack(bBlock, 1);
                            int maxSize = newStack.getMaxStackSize();
                            newStack.setCount(ItemBlackHoleTalisman.remove(tal, maxSize));
                            if (newStack.getCount() != 0 && inv.canPlaceItem(slotIndex, newStack)) {
                                inv.setItem(slotIndex, newStack);
                                inv.setChanged();
                            }
                        } else if (stackInSlot.getItem() == bBlock.asItem()) {
                            int maxSize = stackInSlot.getMaxStackSize();
                            int missing = maxSize - stackInSlot.getCount();
                            if (inv.canPlaceItem(slotIndex, stackInSlot)) {
                                stackInSlot.grow(ItemBlackHoleTalisman.remove(tal, missing));
                                inv.setChanged();
                            }
                        }
                    }
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static int[] buildSlotsForLinearInventory(Container inv) {
        int[] slots = new int[inv.getContainerSize()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    public void setItemSlot(ItemStack halo, ItemStack stack, int slot) {
        String key = "itemSlot" + slot;

        if (stack == null || stack.isEmpty()) {
            ItemNBTHelper.setCompound(halo, key, new CompoundTag());
        } else {
            CompoundTag cmp = new CompoundTag();
            stack.copy().save(cmp);
            ItemNBTHelper.setCompound(halo, key, cmp);
        }
    }

    public static ItemStack getItemForSlot(ItemStack halo, int slot) {
        if (slot >= 12) {
            return null;
        }

        CompoundTag cmp = ItemNBTHelper.getCompound(halo, "itemSlot" + slot, true);

        if (cmp != null && !cmp.isEmpty()) {
            try {
                return ItemStack.of(cmp);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderHUD(Minecraft mc, ItemStack halo, PoseStack poseStack, int screenWidth, int screenHeight) {
        Player player = mc.player;
        if (player == null) return;

        int slot = getSegmentLookedAt(halo, player);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        ItemStack stack = getItemForSlot(halo, slot);
        if (stack != null && !stack.isEmpty()) {
            String name = stack.getHoverName().getString();
            String count = String.valueOf(ItemBlackHoleTalisman.getBlockCount(stack));

            int l = mc.font.width(name);
            int x = screenWidth / 2 - l / 2;
            int y = screenHeight / 2 - 65;

            GuiComponent.fill(poseStack, x - 6, y - 6, x + l + 6, y + 43, 0x22000000);
            GuiComponent.fill(poseStack, x - 4, y - 4, x + l + 4, y + 41, 0x22000000);

            mc.getItemRenderer().renderAndDecorateItem(stack, screenWidth / 2 - 8, screenHeight / 2 - 52);

            mc.font.draw(poseStack, name, x, y, 0xFFFFFF);
            mc.font.draw(poseStack, count, screenWidth / 2 - mc.font.width(count) / 2, y + 32, 0xFFFFFF);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof BlackHalo) {
            try {
                render(stack, player, event.getPartialTick(), event.getPoseStack());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void render(ItemStack stack, Player player, float partialTicks, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        float alpha = ((float)Math.sin((ClientTickHandler.total()) * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;

        var cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        double posX = Mth.lerp(partialTicks, player.xOld, player.getX());
        double posY = Mth.lerp(partialTicks, player.yOld, player.getY()) + player.getEyeHeight();
        double posZ = Mth.lerp(partialTicks, player.zOld, player.getZ());

        poseStack.translate(posX - cameraPos.x, posY - cameraPos.y, posZ - cameraPos.z);
        poseStack.translate(0.0F, 0.25F, 0.0F);

        float base = getRotationBase(stack);
        int angles = 360;
        int segAngles = angles / 12;
        float shift = base - (float)(segAngles / 2);
        float u = 1.0F;
        float v = 0.25F;
        float s = 3.0F;
        float m = 0.8F;
        float y = v * s * 2.0F;
        float y0 = 0.0F;
        int segmentLookedAt = getSegmentLookedAt(stack, player);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, GLOW_TEXTURE);
        RenderSystem.disableCull();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        for (int seg = 0; seg < 12; ++seg) {
            boolean inside = segmentLookedAt == seg;

            poseStack.pushPose();
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));

            float a = alpha;
            if (inside) {
                a = alpha + 0.3F;
                y0 = -y;
            }

            float color = seg % 2 == 0 ? 0.6F : 1.0F;

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            for (int i = 0; i < segAngles; ++i) {
                float ang = (float)(i + seg * segAngles) + shift;
                double xp = Math.cos(Math.toRadians(ang)) * s;
                double zp = Math.sin(Math.toRadians(ang)) * s;

                bufferBuilder.vertex(poseStack.last().pose(), (float)(xp * m), y, (float)(zp * m))
                        .uv(u, v)
                        .color(color, color, color, a)
                        .endVertex();
                bufferBuilder.vertex(poseStack.last().pose(), (float)xp, y0, (float)zp)
                        .uv(u, 0.0F)
                        .color(color, color, color, a)
                        .endVertex();

                xp = Math.cos(Math.toRadians(ang + 1.0F)) * s;
                zp = Math.sin(Math.toRadians(ang + 1.0F)) * s;

                bufferBuilder.vertex(poseStack.last().pose(), (float)xp, y0, (float)zp)
                        .uv(0.0F, 0.0F)
                        .color(color, color, color, a)
                        .endVertex();
                bufferBuilder.vertex(poseStack.last().pose(), (float)(xp * m), y, (float)(zp * m))
                        .uv(0.0F, v)
                        .color(color, color, color, a)
                        .endVertex();
            }

            tessellator.end();
            y0 = 0.0F;
            poseStack.popPose();
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (int seg = 0; seg < 12; ++seg) {
            float rotationAngle = ((float)seg + 0.5F) * (float)segAngles + shift;

            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationAngle));

            double worldTime = ClientTickHandler.total() + (float)seg * 2.75F;

            // 计算位置（可微调）
            float centerRadius = s * m + (s - s * m) / 2.0F + BLOCK_RADIAL_OFFSET;
            float heightPos = (float) (-0.75F + Math.sin(worldTime / 12.0F) / 30.0F + BLOCK_HEIGHT_OFFSET);
            float tangentialPos = BLOCK_TANGENTIAL_OFFSET;

            poseStack.translate(centerRadius, heightPos, tangentialPos);

            ItemStack slotStack = getItemForSlot(stack, seg);
            if (slotStack != null && !slotStack.isEmpty()) {
                Block block = ItemBlackHoleTalisman.getBlock(slotStack);
                if (block != null) {
                    poseStack.pushPose();

                    // 应用缩放调整
                    float scale = 0.6F * BLOCK_SCALE_ADJUSTMENT;
                    poseStack.scale(scale, scale, scale);
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
                    poseStack.translate(0.0F, 0.6F, 0.0F);

                    try {
                        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
                        BlockState blockState = block.defaultBlockState();
                        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

                        blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource,
                                15728880, OverlayTexture.NO_OVERLAY);
                        bufferSource.endBatch();
                    } catch (Exception e) {
                    }

                    poseStack.popPose();
                }
            }

            poseStack.popPose();
        }


        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    private static int getSegmentLookedAt(ItemStack stack, LivingEntity player) {
        float yaw = getCheckingAngle(player, getRotationBase(stack));
        int angles = 360;
        int segAngles = angles / 12;

        for (int seg = 0; seg < 12; ++seg) {
            float calcAngle = (float)(seg * segAngles);
            if (yaw >= calcAngle && yaw < calcAngle + (float)segAngles) {
                return seg;
            }
        }

        return -1;
    }

    public static void setRotationBase(ItemStack stack, float rotation) {
        ItemNBTHelper.setFloat(stack, "rotationBase", rotation);
    }

    public static float getRotationBase(ItemStack stack) {
        return ItemNBTHelper.getFloat(stack, "rotationBase", 0.0F);
    }

    private static float getCheckingAngle(LivingEntity player, float base) {
        float yaw = Mth.wrapDegrees(player.getYRot()) + 90.0F;
        int angles = 360;
        int segAngles = angles / 12;
        float shift = (float)(segAngles / 2);
        if (yaw < 0.0F) {
            yaw += 360.0F;
        }

        yaw -= 360.0F - base;
        float angle = 360.0F - yaw + shift;
        if (angle < 0.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    private static float getCheckingAngle(LivingEntity player) {
        return getCheckingAngle(player, 0.0F);
    }

    public static void setEquipped(ItemStack stack, boolean equipped) {
        ItemNBTHelper.setBoolean(stack, "equipped", equipped);
    }

    public static boolean wasEquipped(ItemStack stack) {
        return ItemNBTHelper.getBoolean(stack, "equipped", false);
    }

    @Override
    public boolean provideBlock(Player player, ItemStack requestor, Block block, boolean doit) {
        var relicCap = requestor.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return false;
            }
        }

        for (int i = 0; i < 12; ++i) {
            ItemStack tal = getItemForSlot(requestor, i);
            if (tal != null && !tal.isEmpty()) {
                Block stored = ItemBlackHoleTalisman.getBlock(tal);
                if (stored == block) {
                    int count = ItemBlackHoleTalisman.getBlockCount(tal);
                    if (count > 0) {
                        if (doit) {
                            ItemNBTHelper.setInt(tal, "blockCount", count - 1);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int getBlockCount(Player player, ItemStack requestor, Block block) {
        var relicCap = requestor.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return 0;
            }
        }

        for (int i = 0; i < 12; ++i) {
            ItemStack tal = getItemForSlot(requestor, i);
            if (tal != null && !tal.isEmpty()) {
                Block stored = ItemBlackHoleTalisman.getBlock(tal);
                if (stored == block) {
                    return ItemBlackHoleTalisman.getBlockCount(tal);
                }
            }
        }
        return 0;
    }
}