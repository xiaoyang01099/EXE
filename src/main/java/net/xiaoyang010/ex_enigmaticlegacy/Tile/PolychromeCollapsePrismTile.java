package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import com.google.common.base.Predicates;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.BlockEntityBase;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModItems;
import net.xiaoyang010.ex_enigmaticlegacy.Network.NetworkHandler;
import net.xiaoyang010.ex_enigmaticlegacy.Network.inputMessage.PrismRenderPacket;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.common.block.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class PolychromeCollapsePrismTile extends BlockEntityBase implements Container, IManaReceiver, ISparkAttachable {
    private final NonNullList<ItemStack> ITEMS = NonNullList.withSize(5, ItemStack.EMPTY);
    private static final List<Block> ALTAR_BLOCKS = new ArrayList<>();
    //祭坛方块
    static {
        ALTAR_BLOCKS.add(ModBlockss.DRAGON_CRYSTALS_BLOCK.get());
        ALTAR_BLOCKS.add(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.ARCANE_ICE_CHUNK.get());
        ALTAR_BLOCKS.add(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.DRAGON_CRYSTALS_BLOCK.get());
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(ModBlocks.creativePool);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.MITHRILL_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.GAIA_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.MITHRILL_BLOCK.get());
        ALTAR_BLOCKS.add(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(ModBlockss.ARCANE_ICE_CHUNK.get());
        ALTAR_BLOCKS.add(ModBlockss.GAIA_BLOCK.get());
        ALTAR_BLOCKS.add(AvaritiaModContent.NEUTRONIUM_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.GAIA_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.ARCANE_ICE_CHUNK.get());
        ALTAR_BLOCKS.add(ModBlocks.creativePool);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(ModBlockss.POLYCHROME_COLLAPSE_PRISM.get());
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(ModBlocks.creativePool);
        ALTAR_BLOCKS.add(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.MITHRILL_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.GAIA_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.MITHRILL_BLOCK.get());
        ALTAR_BLOCKS.add(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(ModBlockss.DRAGON_CRYSTALS_BLOCK.get());
        ALTAR_BLOCKS.add(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.ARCANE_ICE_CHUNK.get());
        ALTAR_BLOCKS.add(AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get());
        ALTAR_BLOCKS.add(ModBlockss.DRAGON_CRYSTALS_BLOCK.get());
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(ModBlocks.creativePool);
        ALTAR_BLOCKS.add(Blocks.AIR);
        ALTAR_BLOCKS.add(Blocks.AIR);
    }

    public PolychromeCollapsePrismTile(@NotNull BlockEntityType<PolychromeCollapsePrismTile> type, BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), pos, state, BotaniaForgeCapabilities.MANA_RECEIVER, BotaniaForgeCapabilities.SPARK_ATTACHABLE);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PolychromeCollapsePrismTile tile) {
        BlockPos below = pos.below();
        BlockPos south = below.south();
        BlockPos north = below.north();
        BlockPos west = below.west();
        BlockPos east = below.east();

        BlockState blockB = level.getBlockState(below);
        BlockState blockS = level.getBlockState(south);
        BlockState blockN = level.getBlockState(north);
        BlockState blockW = level.getBlockState(west);
        BlockState blockE = level.getBlockState(east);

        if (level.getGameTime() % 10 != 0) return;


        if (tile.isRecipeAltarBlock(tile.getAltarBlocks(pos))) {
            ItemStack stack1 = tile.getItem(0);
            ItemStack stack2 = tile.getItem(1);
            if (stack1.is(Items.EMERALD) && stack2.is(Items.DIAMOND)) {
                ItemStack out = new ItemStack(ModItems.INGOT_ANIMATION.get());
                tile.removeItemNoUpdate(0);
                tile.removeItemNoUpdate(1);
                tile.setItem(4, out);
                setChanged(level, pos, state);
                NetworkHandler.CHANNEL.sendToServer(new PrismRenderPacket(pos, tile.getITEMS()));
                tile.triggerEvent(1, 0);
            }
        }
    }

    /**
     * 获取祭坛核心方块周围方块
     */
    public List<Block> getAltarBlocks(BlockPos pos){
        if (level == null) return new ArrayList<>();
        Iterable<BlockPos> blockPos = BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 0, 2));

        List<Block> blockList = new ArrayList<>();
        for (BlockPos blockPo : blockPos) {
            Block block = level.getBlockState(blockPo).getBlock();
            blockList.add(block);
        }

        return blockList;
    }

    /**
     * 对比祭坛方块
     * @param blockList 实际取得的
     */
    public boolean isRecipeAltarBlock(List<Block> blockList) {
        for (int i = 0; i < blockList.size(); i++) {
            Block block = blockList.get(i);
            Block block1 = ALTAR_BLOCKS.get(i);
            if (block != block1) return false;
        }
        return true;
    }

    @Override
    public boolean triggerEvent(int pId, int pType) {
        if (pId == 1 && level != null) {
            BlockPos pos = getBlockPos();
            for (int i = 0; i < 10; i++) {
                level.addAlwaysVisibleParticle(ParticleTypes.LAVA, pos.getX(), pos.getY() + 0.5, pos.getZ(), 0.01, 0.05, 0.01);
            }
            level.playSound((Player) null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 3.0f);
        }
        return super.triggerEvent(pId, pType);
    }

    /**
     * 添加物品
     * @param item
     * @return
     */
    public boolean addItem(ItemStack item) {
        for (int i = 0; i < ITEMS.size() - 1; i++) {
            if (ITEMS.get(i).isEmpty()){
                ITEMS.set(i, item);
                NetworkHandler.CHANNEL.sendToServer(new PrismRenderPacket(getBlockPos(), ITEMS));
                return true;
            }
        }

        return false;
    }

    public NonNullList<ItemStack> getITEMS() {
        return ITEMS;
    }

    @Override
    public int getContainerSize() {
        return ITEMS.size();
    }

    @Override
    public boolean isEmpty() {
        return ITEMS.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return ITEMS.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        return ContainerHelper.removeItem(ITEMS, i, i1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(ITEMS, i);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        ITEMS.set(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        ITEMS.clear();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
    int mana;
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        ContainerHelper.saveAllItems(pTag, ITEMS);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        ContainerHelper.loadAllItems(pTag, ITEMS);
        super.load(pTag);
    }

    @Override
    public Level getManaReceiverLevel() {
        return null;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return this.getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return this.mana;
    }

    @Override
    public boolean isFull() {
        return this.mana == 100000;
    }

    @Override
    public void receiveMana(int i) {
        this.mana += i;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return false;
    }

    @Override
    public boolean canAttachSpark(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        int space = Math.max(0, this.mana - this.getCurrentMana());
        if (space > 0) {
            return space;
        } else {
            return this.level.getBlockState(this.worldPosition.below()).is(ModBlocks.manaVoid) ? this.mana : 0;
        }
    }

    @Override
    public IManaSpark getAttachedSpark() {
        List<Entity> sparks = this.level.getEntitiesOfClass(Entity.class, new AABB(this.worldPosition.above(), this.worldPosition.above().offset(1, 1, 1)), Predicates.instanceOf(IManaSpark.class));
        if (sparks.size() == 1) {
            Entity e = (Entity)sparks.get(0);
            return (IManaSpark)e;
        } else {
            return null;
        }
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }
}