package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Item.Relic;

import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Hud.ClientHelper;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntityAnonymousSteve;
import net.xiaoyang010.ex_enigmaticlegacy.api.INoEMCItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.IRelic;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.ItemRelic;
import vazkii.botania.common.item.relic.RelicImpl;


public class PocketWardrobe extends ItemRelic implements INoEMCItem {
    protected static final ResourceLocation glowPriorityTexture = new ResourceLocation("ex_enigmaticlegacy:textures/misc/glow2.png");
    protected static final ResourceLocation glowTexture = new ResourceLocation("ex_enigmaticlegacy:textures/misc/glow1.png");
    protected static final int segmentCount = 5;
    protected static final int maxSegmentCount = 12;

    public PocketWardrobe(Properties properties) {
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

    @Override
    public void inventoryTick(ItemStack pocketWardrobe, Level world, Entity entity, int pos, boolean equipped) {
        super.inventoryTick(pocketWardrobe, world, entity, pos, equipped);

        boolean eqLastTick = wasEquipped(pocketWardrobe);
        if (!equipped && eqLastTick) {
            setEquipped(pocketWardrobe, equipped);
        }

        if (!eqLastTick && equipped && entity instanceof LivingEntity) {
            setEquipped(pocketWardrobe, equipped);
            int angles = 360;
            int segAngles = angles / 12;
            float shift = (float)segAngles / 2.0F * 5.0F;
            setRotationBase(pocketWardrobe, getCheckingAngle((LivingEntity)entity) - shift);
        }

        int tick = getFightingTick(pocketWardrobe);
        if (tick > 0) {
            setFightingTick(pocketWardrobe, tick - 1);
        } else if (!world.isClientSide && tick == 0 && getFightingMode(pocketWardrobe) && entity instanceof Player) {
            Player player = (Player)entity;
            setFightingMode(pocketWardrobe, false);
            this.swapArmorSet(pocketWardrobe, player, getPrioritySet(pocketWardrobe));
        }
    }

    public void setArmorSet(ItemStack pocketWardrobe, ItemStack[] armorSet, int segment) {
        ListTag nbtList = new ListTag();

        for (int i = 0; i < armorSet.length; i++) {
            ItemStack armor = armorSet[i];
            if (armor != null && !armor.isEmpty()) {
                CompoundTag cmp = new CompoundTag();
                cmp.putByte("slot", (byte)i);
                armor.save(cmp);
                nbtList.add(cmp);
            }
        }

        ItemNBTHelper.setList(pocketWardrobe, "armorSet" + segment, nbtList);
    }

    public static ItemStack[] getArmorSet(ItemStack pocketWardrobe, int segment) {
        if (segment >= segmentCount) {
            return null;
        }

        ItemStack[] armorSet = new ItemStack[4];
        for (int i = 0; i < armorSet.length; i++) {
            armorSet[i] = ItemStack.EMPTY;
        }

        ListTag nbtList = ItemNBTHelper.getList(pocketWardrobe, "armorSet" + segment, 10, false);

        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag cmp = nbtList.getCompound(i);
            byte slotCount = cmp.getByte("slot");
            if (slotCount >= 0 && slotCount < armorSet.length) {
                armorSet[slotCount] = ItemStack.of(cmp);
            }
        }

        return armorSet;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack pocketWardrobe = player.getItemInHand(hand);

        var relicCap = pocketWardrobe.getCapability(BotaniaForgeCapabilities.RELIC);
        if (relicCap.isPresent()) {
            IRelic relic = relicCap.orElse(null);
            if (relic != null && !relic.isRightPlayer(player)) {
                return InteractionResultHolder.fail(pocketWardrobe);
            }
        }

        int segment = getSegmentLookedAt(pocketWardrobe, player);

        if (segment == -1) {
            return InteractionResultHolder.pass(pocketWardrobe);
        }

        if (player.isShiftKeyDown()) {
            setPrioritySet(pocketWardrobe, segment);
        } else {
            this.swapArmorSet(pocketWardrobe, player, segment);
        }

        return InteractionResultHolder.success(pocketWardrobe);
    }

    public void swapArmorSet(ItemStack stack, Player player, int segment) {
        ItemStack[] playerSet = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            playerSet[i] = player.getInventory().armor.get(i);
        }

        ItemStack[] newArmorSet = getArmorSet(stack, segment);
        if (newArmorSet != null) {
            for (int i = 0; i < 4; i++) {
                player.getInventory().armor.set(i, newArmorSet[i] != null ? newArmorSet[i] : ItemStack.EMPTY);
            }
        }

        this.setArmorSet(stack, playerSet, segment);

        if (!player.level.isClientSide) {
            player.level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.3F, 0.86F);
        }
    }

    @SubscribeEvent
    public void onPlayerAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player)event.getEntity();
            if (player.getAbilities().instabuild) {
                return;
            }

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof PocketWardrobe) {
                    PocketWardrobe item = (PocketWardrobe)stack.getItem();

                    var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
                    if (relicCap.isPresent()) {
                        IRelic relic = relicCap.orElse(null);
                        if (relic == null || !relic.isRightPlayer(player)) {
                            continue;
                        }
                    } else {
                        continue;
                    }

                    if (getFightingMode(stack)) {
                        setFightingTick(stack, 32);
                        return;
                    }

                    int armorPrioritySlot = getPrioritySet(stack);
                    ItemStack[] armorSet = getArmorSet(stack, armorPrioritySlot);
                    boolean hasArmor = false;

                    if (armorSet != null) {
                        for (ItemStack armor : armorSet) {
                            if (armor != null && !armor.isEmpty()) {
                                hasArmor = true;
                                break;
                            }
                        }
                    }

                    if (hasArmor) {
                        setFightingTick(stack, 32);
                        setFightingMode(stack, true);
                        item.swapArmorSet(stack, player, armorPrioritySlot);
                        return;
                    }
                }
            }
        }
    }

    public ResourceLocation getGlowTexture(ItemStack stack, int segment) {
        return getPrioritySet(stack) == segment ? glowPriorityTexture : glowTexture;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLast(RenderLevelLastEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof PocketWardrobe) {
            var relicCap = stack.getCapability(BotaniaForgeCapabilities.RELIC);
            if (relicCap.isPresent()) {
                IRelic relic = relicCap.orElse(null);
                if (relic != null && relic.isRightPlayer(player)) {
                    this.render(stack, player, event.getPoseStack(), event.getPartialTick());
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void render(ItemStack stack, Player player, PoseStack poseStack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();

        float alpha = ((float) Math.sin((double) ((float) ClientTickHandler.ticksInGame + partialTicks) * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;

        var camera = mc.gameRenderer.getMainCamera();
        double posX = player.xOld + (player.getX() - player.xOld) * (double) partialTicks;
        double posY = player.yOld + (player.getY() - player.yOld) * (double) partialTicks;
        double posZ = player.zOld + (player.getZ() - player.zOld) * (double) partialTicks;

        poseStack.translate(posX - camera.getPosition().x, posY - camera.getPosition().y, posZ - camera.getPosition().z);
        poseStack.translate(0.0, 2.0, 0.0);

        float base = getRotationBase(stack);
        int angles = 360;
        int segAngles = angles / 12;
        float shift = base - (float) segAngles / 2.0F * 5.0F;
        float u = 1.0F;
        float v = 0.25F;
        float s = 3.6F;
        float m = 0.8F;
        float y = v * 6.0F;
        float y0 = 0.0F;
        int segmentLookedAt = getSegmentLookedAt(stack, player);

        for (int seg = 0; seg < segmentCount; seg++) {
            ClientHelper.setLightmapTextureCoords(15728880, 240, 240);
            float rotationAngle = ((float) seg + 0.5F) * (float) segAngles + shift;

            poseStack.pushPose();
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
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

            RenderSystem.setShaderTexture(0, getGlowTexture(stack, seg));
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            Matrix4f matrix = poseStack.last().pose();

            for (int i = 0; i < segAngles; i++) {
                float ang = (float) (i + seg * segAngles) + shift;
                double xp = Math.cos((double) ang * Math.PI / 180.0) * (double) s;
                double zp = Math.sin((double) ang * Math.PI / 180.0) * (double) s;

                buffer.vertex(matrix, (float) (xp * (double) m), y, (float) (zp * (double) m)).uv(u, v).endVertex();
                buffer.vertex(matrix, (float) xp, y0, (float) zp).uv(u, 0.0F).endVertex();

                xp = Math.cos((double) (ang + 1.0F) * Math.PI / 180.0) * (double) s;
                zp = Math.sin((double) (ang + 1.0F) * Math.PI / 180.0) * (double) s;

                buffer.vertex(matrix, (float) xp, y0, (float) zp).uv(0.0F, 0.0F).endVertex();
                buffer.vertex(matrix, (float) (xp * (double) m), y, (float) (zp * (double) m)).uv(0.0F, v).endVertex();
            }

            y0 = 0.0F;
            tesselator.end();
            poseStack.popPose();

            AbstractClientPlayer steve = new EntityAnonymousSteve((ClientLevel) player.level);
            ItemStack[] armorSet = getArmorSet(stack, seg);

            if (armorSet != null) {
                for (int l = 0; l < 4; l++) {
                    if (l < armorSet.length && armorSet[l] != null && !armorSet[l].isEmpty()) {
                        steve.getInventory().armor.set(l, armorSet[l]);
                    }
                }
            }

            boolean hasArmor = false;
            for (int l = 0; l < steve.getInventory().armor.size(); l++) {
                if (!steve.getInventory().armor.get(l).isEmpty()) {
                    hasArmor = true;
                    break;
                }
            }

            if (hasArmor) {
                poseStack.pushPose();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(rotationAngle));

                double worldTime = (double) ((float) ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks + (float) seg * 2.75F);
                poseStack.translate((double) (s * m), (double) -0.75F + Math.sin(worldTime / 12.0) / 26.0, 0.0);
                poseStack.translate(0.0, -1.0, 0.0);

                float scale = 0.6F;
                poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
                poseStack.translate(0.0F, 0.8125F, 0.0F);
                poseStack.scale(scale, scale, scale);

                steve.setPos(player.getX(), player.getY(), player.getZ());

                steve.setYRot(0.0F);
                steve.yRotO = 0.0F;
                steve.setXRot(10.0F);
                steve.xRotO = 10.0F;
                steve.setYHeadRot(0.0F);
                steve.setYBodyRot(0.0F);
                steve.yBodyRotO = 0.0F;

                EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
                renderManager.render(steve, 0.0, 0.0, 0.0, 0.0F, partialTicks,
                        poseStack, mc.renderBuffers().bufferSource(), 15728880);
                mc.renderBuffers().bufferSource().endBatch();

                poseStack.popPose();
            }
        }
    }

    protected static int getSegmentLookedAt(ItemStack stack, LivingEntity player) {
        float yaw = getCheckingAngle(player, getRotationBase(stack));
        int angles = 360;
        int segAngles = angles / 12;

        for (int seg = 0; seg < segmentCount; seg++) {
            float calcAngle = (float)(seg * segAngles);
            if (yaw >= calcAngle && yaw < calcAngle + (float)segAngles) {
                return seg;
            }
        }

        return -1;
    }

    protected static float getCheckingAngle(LivingEntity player, float base) {
        float yaw = Mth.wrapDegrees(player.getYRot()) + 90.0F;
        int angles = 360;
        int segAngles = angles / 12;
        float shift = (float)segAngles / 2.0F * 5.0F;

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

    public static void setFightingMode(ItemStack stack, boolean mode) {
        ItemNBTHelper.setBoolean(stack, "fightingMode", mode);
    }

    public static boolean getFightingMode(ItemStack stack) {
        return ItemNBTHelper.getBoolean(stack, "fightingMode", false);
    }

    public static void setFightingTick(ItemStack stack, int tick) {
        ItemNBTHelper.setInt(stack, "fightingTick", tick);
    }

    public static int getFightingTick(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, "fightingTick", 0);
    }

    public static void setPrioritySet(ItemStack stack, int segment) {
        ItemNBTHelper.setInt(stack, "prioritySet", segment);
    }

    public static int getPrioritySet(ItemStack stack) {
        return ItemNBTHelper.getInt(stack, "prioritySet", 2);
    }

}