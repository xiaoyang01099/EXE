package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;
import morph.avaritia.init.AvaritiaModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.BlockEntityBase;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlockss;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRecipes;
import net.xiaoyang010.ex_enigmaticlegacy.api.IPolychromeRecipe;
import org.jetbrains.annotations.NotNull;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.common.block.ModBlocks;
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

public class PolychromeCollapsePrismTile extends BlockEntityBase implements ISparkAttachable, IManaReceiver {
    public static final Supplier<IMultiblock> MULTIBLOCK = Suppliers.memoize(() -> PatchouliAPI.get().makeMultiblock(

            new String[][] {
                    {
                            "_______________",
                            "_______________",
                            "_______________",
                            "_______________",
                            "____X_____X____",
                            "_______Z_______",
                            "_______________",
                            "_____Z___Z_____",
                            "_______________",
                            "_______Z_______",
                            "____X_____X____",
                            "_______________",
                            "_______________",
                            "_______________",
                            "_______________"

                    },
                    {
                            "_______________",
                            "_______________",
                            "_______________",
                            "______J_J______",
                            "____H_____H____",
                            "_______K_______",
                            "___J_______J___",
                            "_____K_P_K_____",
                            "___J_______J___",
                            "_______K_______",
                            "____H_____H____",
                            "______J_J______",
                            "_______________",
                            "_______________",
                            "_______________"

                    },
                    {
                            "_______G_______",
                            "______OIO______",
                            "_____FIUIF_____",
                            "____FIYTYIF____",
                            "___FIYTETYIF___",
                            "__FIYEWQWEYIF__",
                            "_OIYTWRLRWTYIO_",
                            "GIUTEQL0LQETUIG",
                            "_OIYTWRLRWTYIO_",
                            "__FIYEWQWEYIF__",
                            "___FIYTETYIF___",
                            "____FIYTYIF____",
                            "_____FIUIF_____",
                            "______OIO______",
                            "_______G_______"

                    }
            },

            'P', ModBlockss.POLYCHROME_COLLAPSE_PRISM.get(),
            'R', ModBlockss.MITHRILL_BLOCK.get(),
            '0', ModBlockss.BLOCKNATURE.get(),
            'L', ModBlockss.GAIA_BLOCK.get(),
            'Q', ModBlockss.ARCANE_ICE_CHUNK.get(),
            'W', AvaritiaModContent.CRYSTAL_MATRIX_STORAGE_BLOCK.get(),
            'E', ModBlockss.DRAGON_CRYSTALS_BLOCK.get(),
            'T', AvaritiaModContent.NEUTRONIUM_STORAGE_BLOCK.get(),
            'Y', ModBlocks.livingrock,
            'U', Blocks.GLOWSTONE,
            'I', ModBlockss.INFINITYGlASS.get(),
            'O', ModBlocks.terrasteelBlock,
            'G', ModBlocks.dragonstoneBlock,
            'F', ModBlockss.DECAY_BLOCK.get(),
            'H', ModBlockss.MANA_CONTAINER.get(),
            'J', ModBlockss.INFINITY_POTATO.get(),
            'K', ModBlocks.fabulousPool,
            'Z',ModBlocks.manaPylon,
            'X',ModBlocks.naturaPylon
            ));

    private int mana;
    private static final String TAG_MANA = "mana";

    public PolychromeCollapsePrismTile(@NotNull BlockEntityType<PolychromeCollapsePrismTile>  type, BlockPos pos, BlockState state) {
        super(ModBlockEntities.POLYCHROME_COLLAPSE_PRISM_TILE.get(), pos, state, BotaniaForgeCapabilities.MANA_RECEIVER, BotaniaForgeCapabilities.SPARK_ATTACHABLE);
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
                    IXplatAbstractions.INSTANCE.sendToNear(level, worldPosition,
                            new PacketBotaniaEffect(EffectType.TERRA_PLATE, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), proportion));
                }

                if (self.mana >= recipe.getManaUsage()) {
                    ItemStack result = recipe.assemble(inv);
                    for (ItemStack item : items) {
                        item.setCount(0);
                    }
                    ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.2, worldPosition.getZ() + 0.5, result);
                    item.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(item);
                    level.playSound(null, item.getX(), item.getY(), item.getZ(), ModSounds.terrasteelCraft, SoundSource.BLOCKS, 1F, 1F);
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

    private List<ItemStack> getItems() {
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition, worldPosition.offset(1, 1, 1)), EntitySelector.ENTITY_STILL_ALIVE);
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemEntity entity : itemEntities) {
            if (!entity.getItem().isEmpty()) {
                stacks.add(entity.getItem());
            }
        }
        return stacks;
    }

    public SimpleContainer getInventory() {
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
    public IPolychromeRecipe getCurrentRecipe(SimpleContainer items) {
        if (items.isEmpty()) {
            return null;
        }
        return level.getRecipeManager().getRecipeFor(ModRecipes.POLYCHROME_TYPE, items, level).orElse(null);
    }

    private boolean isActive() {
        return getCurrentRecipe(getInventory()) != null;
    }

    public boolean hasValidPlatform() {
        return MULTIBLOCK.get().validate(level, getBlockPos().below()) != null;
    }

    @Override
    public void writePacketNBT(CompoundTag cmp) {
        cmp.putInt(TAG_MANA, mana);
    }

    @Override
    public void readPacketNBT(CompoundTag cmp) {
        mana = cmp.getInt(TAG_MANA);
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
        return recipe == null || getCurrentMana() >= recipe.getManaUsage();
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
    public boolean areIncomingTranfersDone() {
        return !isActive();
    }

    @Override
    public int getAvailableSpaceForMana() {
        IPolychromeRecipe recipe = getCurrentRecipe(getInventory());
        return recipe == null ? 0 : Math.max(0, recipe.getManaUsage() - getCurrentMana());
    }

    public float getCompletion() {
        IPolychromeRecipe recipe = getCurrentRecipe(getInventory());
        if (recipe == null) {
            return 0;
        }
        return ((float) getCurrentMana()) / recipe.getManaUsage();
    }

    public int getComparatorLevel() {
        int val = (int) (getCompletion() * 15.0);
        if (getCurrentMana() > 0) {
            val = Math.max(val, 1);
        }
        return val;
    }
}