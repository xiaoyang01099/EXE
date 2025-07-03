package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
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
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import net.xiaoyang010.ex_enigmaticlegacy.Item.all.ItemModRelic;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TalismanHiddenRiches extends ItemModRelic {
    protected static List<ChestBlockEntity> chestList = new ArrayList<>();
    protected static final ResourceLocation glowTexture = new ResourceLocation("ab:textures/misc/glow3.png");
    protected static final int segmentCount = 11;
    protected static final int maxSegmentCount = 16;

    private static final Map<Integer, Float> chestAnimationState = new HashMap<>();
    private static final Map<Integer, Float> lastChestAnimationState = new HashMap<>();

    public TalismanHiddenRiches(Properties tab) {
        super("talismanHiddenRiches", new Properties().stacksTo(1).fireResistant());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity != null && entity instanceof Player) {
            Player player = (Player) entity;
            boolean eqLastTick = wasEquipped(stack);
            if (!isSelected && eqLastTick) {
                setEquipped(stack, isSelected);
            }

            if (!eqLastTick && isSelected) {
                setEquipped(stack, isSelected);
                int angles = 360;
                int segAngles = angles / 16;
                float shift = (float) segAngles / 2.0F * 11.0F;
                setRotationBase(stack, getCheckingAngle((LivingEntity) entity) - shift);
            }

            if (level.isClientSide && isSelected) {
                updateChestAnimations(stack, player, level);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void updateChestAnimations(ItemStack stack, Player player, Level level) {
        int openChest = getOpenChest(stack);

        for (int i = 0; i < 11; ++i) {
            lastChestAnimationState.put(i, chestAnimationState.getOrDefault(i, 0.0F));

            float currentState = chestAnimationState.getOrDefault(i, 0.0F);

            if (i == openChest && currentState < 1.0F) {
                if (currentState == 0.0F) {
                    double y = player.getY() - 0.5F;
                    level.playLocalSound(player.getX(), y, player.getZ(),
                            SoundEvents.CHEST_OPEN,
                            SoundSource.BLOCKS,
                            0.5F, level.random.nextFloat() * 0.1F + 0.9F, false);
                }
                chestAnimationState.put(i, Math.min(1.0F, currentState + 0.1F));
            } else if (i != openChest && currentState > 0.0F) {
                if ((int)(currentState * 10.0F) == 5) {
                    double y = player.getY() - 0.5F;
                    level.playLocalSound(player.getX(), y, player.getZ(),
                            SoundEvents.CHEST_CLOSE,
                            SoundSource.BLOCKS,
                            0.5F, level.random.nextFloat() * 0.1F + 0.9F, false);
                }
                chestAnimationState.put(i, Math.max(0.0F, currentState - 0.1F));
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int segment = getSegmentLookedAt(stack, player);
        if (segment == -1) {
            return InteractionResultHolder.pass(stack);
        } else {
            setOpenChest(stack, segment);

            if (!level.isClientSide && player instanceof ServerPlayer) {
                NetworkHooks.openGui((ServerPlayer) player, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return new TranslatableComponent("gui.talisman_hidden_riches.title", segment + 1);
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                        return null;
                    }
                }, buf -> {
                    buf.writeItem(stack);
                    buf.writeVarInt(segment);
                });
            }

            return InteractionResultHolder.success(stack);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof TalismanHiddenRiches) {
                    this.render(stack, player, event.getPartialTick());
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void render(ItemStack stack, Player player, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = new PoseStack();

        float alpha = ((float) Math.sin(((float) ClientTickHandler.ticksInGame + partialTicks) * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;
        double posX = player.xOld + (player.getX() - player.xOld) * (double) partialTicks;
        double posY = player.yOld + (player.getY() - player.yOld) * (double) partialTicks;
        double posZ = player.zOld + (player.getZ() - player.zOld) * (double) partialTicks;

        poseStack.pushPose();
        poseStack.translate(posX - mc.getEntityRenderDispatcher().camera.getPosition().x(),
                posY - mc.getEntityRenderDispatcher().camera.getPosition().y(),
                posZ - mc.getEntityRenderDispatcher().camera.getPosition().z());

        float base = getRotationBase(stack);
        int angles = 360;
        int segAngles = angles / 16;
        float shift = base - (float) segAngles / 2.0F * 11.0F;
        float u = 1.0F;
        float v = 0.25F;
        float s = 3.2F;
        float m = 0.8F;
        float y = v * 6.0F;
        float y0 = 0.0F;
        int segmentLookedAt = getSegmentLookedAt(stack, player);

        for (int seg = 0; seg < 11; ++seg) {
            float rotationAngle = ((float) seg + 0.5F) * (float) segAngles + shift;

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationAngle));
            poseStack.translate(s * m, -0.75F, 0.0F);
            double worldTime = (double) ((float) ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks + (float) seg * 2.75F);
            poseStack.translate(0.375F, Math.sin(worldTime / 8.0F) / 20.0F, -0.375F);
            float scale = 0.75F;
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));

            ChestBlockEntity chest = getChestForSegment(seg);
            if (chest != null) {
                updateChestVisualState(chest, seg, partialTicks);

                MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
                BlockEntityRenderDispatcher renderDispatcher = mc.getBlockEntityRenderDispatcher();
                renderDispatcher.render(chest, partialTicks, poseStack, bufferSource);
            }
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
            float a = alpha;
            if (segmentLookedAt == seg) {
                a = alpha + 0.3F;
                y0 = -y;
            }

            if (seg % 2 == 0) {
                RenderSystem.setShaderColor(0.6F, 0.6F, 0.6F, a);
            } else {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, a);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, getGlowTexture(stack, seg));

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            Matrix4f matrix = poseStack.last().pose();

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            for (int i = 0; i < segAngles; ++i) {
                float ang = (float) (i + seg * segAngles) + shift;
                double xp = Math.cos(ang * Math.PI / 180.0F) * s;
                double zp = Math.sin(ang * Math.PI / 180.0F) * s;

                buffer.vertex(matrix, (float)(xp * m), y, (float)(zp * m)).uv(u, v).endVertex();
                buffer.vertex(matrix, (float)xp, y0, (float)zp).uv(u, 0.0F).endVertex();

                xp = Math.cos((ang + 1.0F) * Math.PI / 180.0F) * s;
                zp = Math.sin((ang + 1.0F) * Math.PI / 180.0F) * s;

                buffer.vertex(matrix, (float)xp, y0, (float)zp).uv(0.0F, 0.0F).endVertex();
                buffer.vertex(matrix, (float)(xp * m), y, (float)(zp * m)).uv(0.0F, v).endVertex();
            }

            y0 = 0.0F;
            tesselator.end();
            poseStack.popPose();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    private void updateChestVisualState(ChestBlockEntity chest, int segment, float partialTicks) {
        // 由于1.18.2中ChestBlockEntity的内部结构改变，我们需要通过反射或其他方式来设置动画状态
        // 这里我们使用自己的动画状态管理
        float currentState = chestAnimationState.getOrDefault(segment, 0.0F);
        float lastState = lastChestAnimationState.getOrDefault(segment, 0.0F);

        // 如果需要更精确的动画，可以在这里通过反射设置ChestBlockEntity的内部状态
        // 但为了兼容性，我们保持当前的实现
    }

    private ResourceLocation getGlowTexture(ItemStack stack, int seg) {
        return glowTexture;
    }

    protected static int getSegmentLookedAt(ItemStack stack, LivingEntity player) {
        float yaw = getCheckingAngle(player, getRotationBase(stack));
        int angles = 360;
        int segAngles = angles / 16;

        for (int seg = 0; seg < 11; ++seg) {
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
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean("equipped", equipped);
    }

    public static boolean wasEquipped(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("equipped");
    }

    public static void setRotationBase(ItemStack stack, float rotation) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat("rotationBase", rotation);
    }

    public static float getRotationBase(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getFloat("rotationBase") : 0.0F;
    }

    public static void setOpenChest(ItemStack stack, int segment) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("openChest", segment);
    }

    public static int getOpenChest(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt("openChest") : -1;
    }

    public static ChestBlockEntity getChestForSegment(int segment) {
        if (chestList.isEmpty()) {
            for (int i = 0; i < 11; ++i) {
                chestList.add(new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState()));
            }
        }

        return segment >= 0 && segment < 11 ? chestList.get(segment) : null;
    }

    public static void setChestLoot(ItemStack stack, ItemStack[] loot, int segment) {
        ListTag nbtList = new ListTag();
        int i = -1;

        for (ItemStack item : loot) {
            ++i;
            if (item != null && !item.isEmpty()) {
                CompoundTag cmp = new CompoundTag();
                cmp.putByte("slot", (byte) i);
                item.save(cmp);
                nbtList.add(cmp);
            }
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.put("chestLoot" + segment, nbtList);
    }

    public static ItemStack[] getChestLoot(ItemStack stack, int segment) {
        if (segment >= 11) {
            return null;
        } else {
            ItemStack[] loot = new ItemStack[27];
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("chestLoot" + segment, Tag.TAG_LIST)) {
                ListTag nbtList = tag.getList("chestLoot" + segment, Tag.TAG_COMPOUND);

                for (int i = 0; i < nbtList.size(); ++i) {
                    CompoundTag cmp = nbtList.getCompound(i);
                    byte slotCount = cmp.getByte("slot");
                    if (slotCount >= 0 && slotCount < loot.length) {
                        loot[slotCount] = ItemStack.of(cmp);
                    }
                }
            }

            return loot;
        }
    }

    public static float getChestAnimationState(int segment) {
        return chestAnimationState.getOrDefault(segment, 0.0F);
    }
}