package net.xiaoyang010.ex_enigmaticlegacy.Tile;

import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;

import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.xiaoyang010.ex_enigmaticlegacy.ExEnigmaticlegacyMod;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;

import net.xiaoyang010.ex_enigmaticlegacy.Recipe.IPolychromeRecipe;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileMod;
import vazkii.botania.common.handler.ModSounds;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.PacketBotaniaEffect;
import vazkii.botania.xplat.IXplatAbstractions;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.PatchouliAPI;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PolychromeCollapsePrismTile extends TileMod implements ISparkAttachable, IManaReceiver {
    public static final Supplier<IMultiblock> MULTIBLOCK = Suppliers.memoize(() -> PatchouliAPI.get().makeMultiblock(
            new String[][] {
                    // 第一层 - 底部基础结构（棋盘格模式）
                    {
                            "_______________",
                            "_______________",
                            "_______________",
                            "__ZL__K_K__LZ__",
                            "_______________",
                            "_______X_______",
                            "___K_______K___",
                            "_____X_Q_X_____",
                            "___K_______K___",
                            "_______X_______",
                            "_______________",
                            "__ZL__K_K___LZ_",
                            "_______________",
                            "_______________",
                            "_______________"

                    },
                    // 第二层 - 顶部结构
                    {
                            "_______W_______",
                            "______DED______",
                            "_____JEREJ_____",
                            "____JEYTYEJ____",
                            "___JEYTUTYEJ___",
                            "__JEYUOIOUYEJ__",
                            "_FEYTOAPAOTYEH_",
                            "WERTUIP0PIUTREW",
                            "_FEYTOAPAOTYEH_",
                            "__JEYUOIOUYEJ__",
                            "___JEYTUTYEJ___",
                            "____JEYTYEJ____",
                            "_____JEREJ_____",
                            "______GEG______",
                            "_______W_______"
                    }
            },
            'Q', ModBlockss.POLYCHROME_COLLAPSE_PRISM.get(),
            'W', ModBlocks.manasteelBlock,
            'E', ModBlockss.INFINITYGlASS.get(),
            'R', Blocks.GLOWSTONE,
            'T', AvaritiaModContent.NEUTRONIUM_STORAGE_BLOCK.get(),
            'U', ModBlockss.DRAGON_CRYSTALS_BLOCK.get(),
            'I', ModBlockss.ARCANE_ICE_CHUNK.get(),
            'O', AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get(),
            'P', ModBlockss.GAIA_BLOCK.get(),
            'A', ModBlockss.MITHRILL_BLOCK.get(),
            '0', ModBlockss.BLOCKNATURE.get(),
            'D', ModBlocks.terrasteelBlock,
            'F', ModBlocks.elementiumBlock,
            'G', ModBlocks.manaDiamondBlock,
            'H', ModBlocks.dragonstoneBlock,
            'J', ModBlockss.DECAY_BLOCK.get(),
            'K', ModBlockss.INFINITY_POTATO.get(),
            'L', ModBlockss.infinitySpreader.get(),
            'Z', ModBlockss.ASGARDANDELION.get(),
            'X', ModBlocks.creativePool,
            'Y', "#botania:terra_plate_base"
            ));

    private static final String TAG_MANA = "mana";
    private static final String TAG_PROGRESS = "progress";
    private static final int MAX_MANA = 1000000;

    private int mana;
    private int progress;

    public PolychromeCollapsePrismTile(@NotNull BlockEntityType<PolychromeCollapsePrismTile> polychromeCollapsePrismTileBlockEntityType, BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos worldPosition, BlockState state, PolychromeCollapsePrismTile self) {
        boolean removeMana = true;

        if (self.hasValidPlatform()) {
            List<ItemStack> items = self.getItems();
            SimpleContainer inv = self.getInventory();

            IPolychromeRecipe recipe = self.getCurrentRecipe(inv);
            if (recipe != null) {
                removeMana = false;
                IManaSpark spark = self.getAttachedSpark();
                if (spark != null) {
                    SparkHelper.getSparksAround(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, spark.getNetwork())
                            .filter(otherSpark -> spark != otherSpark && otherSpark.getAttachedManaReceiver() instanceof IManaPool)
                            .forEach(os -> os.registerTransfer(spark));
                }
                if (self.mana > 0) {
                    VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
                    int proportion = Float.floatToIntBits(self.getCompletion());
                    // 发送效果包
                    IXplatAbstractions.INSTANCE.sendToNear(level, worldPosition,
                            new PacketBotaniaEffect(EffectType.TERRA_PLATE, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), proportion));

                    // 增加进度
                    self.progress += 1;
                    if (self.progress >= 100) {
                        self.progress = 0;
                        // 每100 ticks触发特殊效果
                        self.triggerSpecialEffect();
                    }
                }

                if (self.mana >= recipe.getMana()) {
                    ItemStack result = recipe.assemble(inv);
                    for (ItemStack item : items) {
                        item.setCount(0);
                    }
                    ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.2, worldPosition.getZ() + 0.5, result);
                    item.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(item);
                    level.playSound(null, item.getX(), item.getY(), item.getZ(), ModSounds.manaPoolCraft, SoundSource.BLOCKS, 1F, 1F);
                    self.mana = 0;
                    level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
                    VanillaPacketDispatcher.dispatchTEToNearbyPlayers(self);
                }
            }
        }

        if (removeMana) {
            self.receiveMana(-1000);
        }
    }

    private void triggerSpecialEffect() {
        // 触发特殊效果，如粒子，声音，临时增益等
        if (level != null && !level.isClientSide) {
            // 发送特殊效果包给客户端
            IXplatAbstractions.INSTANCE.sendToNear(level, worldPosition,
                    new PacketBotaniaEffect(EffectType.TERRA_PLATE, worldPosition.getX(), worldPosition.getY() + 2, worldPosition.getZ(), 0));
        }
    }

    private List<ItemStack> getItems() {
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition, worldPosition.offset(1, 1, 1)), Entity::isAlive);
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : itemEntities) {
            if (!entity.getItem().isEmpty()) {
                stacks.add(entity.getItem());
            }
        }
        return stacks;
    }

    private SimpleContainer getInventory() {
        List<ItemStack> items = getItems();
        return new SimpleContainer(flattenStacks(items));
    }

    private static ItemStack[] flattenStacks(List<ItemStack> items) {
        ItemStack[] stacks;
        int i = 0;
        for (ItemStack item : items) {
            i += item.getCount();
        }
        if (i > 64) {
            return new ItemStack[0];
        }

        stacks = new ItemStack[i];
        int j = 0;
        for (ItemStack item : items) {
            if (item.getCount() > 1) {
                ItemStack temp = item.copy();
                temp.setCount(1);
                for (int count = 0; count < item.getCount(); count++) {
                    stacks[j] = temp.copy();
                    j++;
                }
            } else {
                stacks[j] = item;
                j++;
            }
        }
        return stacks;
    }

    @Nullable
    private IPolychromeRecipe getCurrentRecipe(SimpleContainer items) {
        if (items.isEmpty()) {
            return null;
        }
        return level.getRecipeManager().getRecipeFor(ModRecipes.POLYCHROME_TYPE, items, level).orElse(null);
    }

    private boolean hasValidPlatform() {
        return MULTIBLOCK.get().validate(level, getBlockPos().below()) != null;
    }

    @Override
    public void writePacketNBT(CompoundTag cmp) {
        cmp.putInt(TAG_MANA, mana);
        cmp.putInt(TAG_PROGRESS, progress);
    }

    @Override
    public void readPacketNBT(CompoundTag cmp) {
        mana = cmp.getInt(TAG_MANA);
        progress = cmp.getInt(TAG_PROGRESS);
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        IPolychromeRecipe recipe = getCurrentRecipe(getInventory());
        return recipe == null || getCurrentMana() >= recipe.getMana();
    }

    @Override
    public void receiveMana(int mana) {
        this.mana = Math.max(0, this.mana + mana);
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return isActive();
    }

    private boolean isActive() {
        return getCurrentRecipe(getInventory()) != null && hasValidPlatform();
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public IManaSpark getAttachedSpark() {
        List<Entity> sparks = level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.above(), worldPosition.above().offset(1, 1, 1)), Predicates.instanceOf(IManaSpark.class));
        if (sparks.size() == 1) {
            Entity e = sparks.get(0);
            return (IManaSpark) e;
        }

        return null;
    }

    @Override
    public void attachSpark(IManaSpark spark) {
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return !isActive();
    }

    @Override
    public int getAvailableSpaceForMana() {
        IPolychromeRecipe recipe = getCurrentRecipe(getInventory());
        return recipe == null ? 0 : Math.max(0, recipe.getMana() - getCurrentMana());
    }

    public float getCompletion() {
        IPolychromeRecipe recipe = getCurrentRecipe(getInventory());
        if (recipe == null) {
            return 0;
        }
        return ((float) getCurrentMana()) / recipe.getMana();
    }

    public int getComparatorLevel() {
        int val = (int) (getCompletion() * 15.0);
        if (getCurrentMana() > 0) {
            val = Math.max(val, 1);
        }
        return val;
    }
}