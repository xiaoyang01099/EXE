package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.api.IRenderHud;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.tile.TileMod;

import javax.annotation.Nullable;

public class TileLebethronCore extends TileMod implements IRenderHud {
    private static final String TAG_BLOCK = "block";
    private static final String TAG_TICK = "tick";
    private static final String TAG_VALID_TREE = "validTree";
    protected int tick = 0;
    protected Block block = null;
    protected boolean validTree = false;

    public TileLebethronCore(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEBETHRON_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileLebethronCore tile) {
        if (tile.tick <= 0) {
            tile.updateStructure();
            if (tile.validTree && tile.getStoredBlock() != null) {
                tile.spawnLeaves();
                tile.tick = 40;
            }
        } else {
            tile.tick--;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileLebethronCore tile) {
        if (level.random.nextBoolean()) {
            double x = pos.getX() + level.random.nextDouble();
            double y = pos.getY() + level.random.nextDouble();
            double z = pos.getZ() + level.random.nextDouble();

            SparkleParticleData data = SparkleParticleData.sparkle(
                    (float) level.random.nextDouble() * 2.0F,
                    0.5F, 1.0F, 0.5F,
                    2
            );

            level.addParticle(data, x, y, z, 0, 0, 0);
        }
    }

    public boolean getValidTree() {
        return validTree;
    }

    public void updateStructure() {
        boolean oldValidTree = validTree;
        validTree = hasValidTree();

        if (oldValidTree != validTree) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            setChanged();
        }
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        if (block != null) {
            tag.putString(TAG_BLOCK, ForgeRegistries.BLOCKS.getKey(block).toString());
        }
        tag.putInt(TAG_TICK, tick);
        tag.putBoolean(TAG_VALID_TREE, validTree);
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        if (tag.contains(TAG_BLOCK)) {
            String blockId = tag.getString(TAG_BLOCK);
            block = ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation(blockId));
        }
        tick = tag.getInt(TAG_TICK);
        validTree = tag.getBoolean(TAG_VALID_TREE);
    }

    public boolean setBlock(Player player, Block block) {
        if (this.block == null) {
            this.block = block;
            setChanged();
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            return true;
        }

        if (this.block == block) {
            return false;
        }

        if (level != null && !level.isClientSide) {
            Vec3 lookVec = player.getLookAngle().normalize();
            ItemEntity entityItem = new ItemEntity(
                    level,
                    player.getX() + lookVec.x,
                    player.getY() + 1.2,
                    player.getZ() + lookVec.z,
                    new ItemStack(this.block)
            );
            level.addFreshEntity(entityItem);
        }

        this.block = block;
        setChanged();
        VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        return true;
    }

    @Nullable
    public Block getStoredBlock() {
        return block != null && block != Blocks.AIR ? block : null;
    }

    public boolean hasValidTree() {
        if (level == null) return false;

        if (!checkBlock(worldPosition.below())) {
            return false;
        }

        for (int i = 1; i <= 4; i++) {
            if (!checkBlock(worldPosition.above(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean checkBlock(BlockPos pos) {
        if (level == null) return false;
        BlockState state = level.getBlockState(pos);
        return state.is(ModBlockss.LEBETHRON_CORE.get());
    }

    private void spawnLeaves() {
        if (level == null || block == null) return;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y < 5; y++) {
                    tryPlaceLeaves(worldPosition.offset(x, y + 2, z));
                }
            }
        }

        tryPlaceLeaves(worldPosition.offset(1, 1, 0));
        tryPlaceLeaves(worldPosition.offset(-1, 1, 0));
        tryPlaceLeaves(worldPosition.offset(0, 1, 1));
        tryPlaceLeaves(worldPosition.offset(0, 1, -1));

        tryPlaceLeaves(worldPosition.offset(1, 7, 0));
        tryPlaceLeaves(worldPosition.offset(-1, 7, 0));
        tryPlaceLeaves(worldPosition.offset(0, 7, 1));
        tryPlaceLeaves(worldPosition.offset(0, 7, -1));

        for (int i = 0; i <= 3; i++) {
            tryPlaceLeaves(worldPosition.offset(0, 6 + i, 0));
        }

        tryPlaceLeaves(worldPosition.offset(0, 2, -2));
        for (int i = -1; i <= 1; i++) {
            tryPlaceLeaves(worldPosition.offset(i, 3, -2));
            tryPlaceLeaves(worldPosition.offset(i, 4, -2));
        }
        tryPlaceLeaves(worldPosition.offset(0, 5, -2));

        tryPlaceLeaves(worldPosition.offset(0, 2, 2));
        for (int i = -1; i <= 1; i++) {
            tryPlaceLeaves(worldPosition.offset(i, 3, 2));
            tryPlaceLeaves(worldPosition.offset(i, 4, 2));
        }
        tryPlaceLeaves(worldPosition.offset(0, 5, 2));

        tryPlaceLeaves(worldPosition.offset(2, 2, 0));
        for (int i = -1; i <= 1; i++) {
            tryPlaceLeaves(worldPosition.offset(2, 3, i));
            tryPlaceLeaves(worldPosition.offset(2, 4, i));
        }
        tryPlaceLeaves(worldPosition.offset(2, 5, 0));

        tryPlaceLeaves(worldPosition.offset(-2, 2, 0));
        for (int i = -1; i <= 1; i++) {
            tryPlaceLeaves(worldPosition.offset(-2, 3, i));
            tryPlaceLeaves(worldPosition.offset(-2, 4, i));
        }
        tryPlaceLeaves(worldPosition.offset(-2, 5, 0));
    }

    private void tryPlaceLeaves(BlockPos pos) {
        if (level == null || block == null) return;

        if (level.random.nextInt(10) > 8) {
            return;
        }

        if (level.getBlockState(pos).isAir() && pos.getY() < level.getMaxBuildHeight()) {
            level.setBlock(pos, block.defaultBlockState(), 3);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderHud(Minecraft mc, PoseStack poseStack, int screenWidth, int screenHeight) {
        if (!validTree) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        ItemStack displayStack = getStoredBlock() != null
                ? new ItemStack(block)
                : new ItemStack(Blocks.OAK_LEAVES);

        int x = screenWidth / 2 - 7;
        int y = screenHeight / 2 + 12;

        GuiComponent.fill(poseStack, x - 2, y - 2, x + 18, y + 18, 0x44000000);
        GuiComponent.fill(poseStack, x, y, x + 16, y + 16, 0x44000000);

        ItemRenderer itemRenderer = mc.getItemRenderer();
        itemRenderer.renderGuiItem(displayStack, x, y);

        poseStack.pushPose();
        poseStack.translate(0, 0, 200);

        if (getStoredBlock() != null) {
            mc.font.draw(poseStack, "✓", x + 10, y + 9, 0x004C00);
            mc.font.draw(poseStack, "✓", x + 10, y + 8, 0x00BD4D);
        } else {
            mc.font.draw(poseStack, "✗", x + 10, y + 9, 0x4C0000);
            mc.font.draw(poseStack, "✗", x + 10, y + 8, 0xD1948D);
        }

        poseStack.popPose();

        RenderSystem.disableBlend();
    }
}
