package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;/*package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.TileInventory;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModBlocks;
import net.xiaoyang010.ex_enigmaticlegacy.Recipe.RecipeAdvancedPlate;
import net.xiaoyang010.ex_enigmaticlegacy.api.AdvancedBotanyAPI;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.ISparkEntity;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.spark.IManaSpark;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.fx.WispParticleData;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Random;


public class NidavellirForgeTile extends TileInventory implements ISparkAttachable, WorldlyContainer {
    private int mana;
    public int manaToGet;
    private RecipeAdvancedPlate currentRecipe;
    private int recipeID;
    public boolean requestUpdate;

    public NidavellirForgeTile(BlockPos pos, BlockState state) {
        super(ModBlocks.NIDAVELLIR_FORGE_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof NidavellirForgeTile tile) {
            if (!level.isClientSide()) {
                tile.updateServer();
            } else {
                tile.updateClient();
            }

            ISparkEntity spark = tile.getAttachedSpark();
            if (spark != null) {
                SparkHelper.getSparksAround(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                        .stream()
                        .filter(otherSpark -> spark != otherSpark &&
                                otherSpark.getAttachedTile() instanceof IManaPool)
                        .forEach(otherSpark -> otherSpark.registerTransfer((IManaSpark) spark));
            }
        }
    }

    private void updateServer() {
        if (requestUpdate) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }

        boolean hasUpdate = false;

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1))) {
            if (!item.getItem().isEmpty() && !item.isRemoved()) {
                ItemStack stack = item.getItem();
                int splitCount = addItemStack(stack);
                stack.shrink(splitCount);
                if (stack.isEmpty()) {
                    item.discard();
                }
                if (splitCount > 0) {
                    hasUpdate = true;
                }
                break;
            }
        }

        int wasManaToGet = manaToGet;
        boolean hasCraft = false;
        int recipeID = 0;

        for (RecipeAdvancedPlate recipe : AdvancedBotanyAPI.advancedPlateRecipes) {
            if (recipe.matches(this)) {
                this.recipeID = recipeID;
                if (mana > 0 && isFull()) {
                    ItemStack output = recipe.getOutput().copy();
                    recieveMana(-recipe.getManaUsage());
                    manaToGet = 0;

                    for (int i = 1; i < getContainerSize(); i++) {
                        if (getItem(i).getCount() > 1) {
                            getItem(i).shrink(1);
                        } else {
                            setItem(i, ItemStack.EMPTY);
                        }
                    }

                    if (!getItem(0).isEmpty()) {
                        getItem(0).grow(1);
                    } else {
                        setItem(0, output);
                    }

                    hasUpdate = true;
                    level.playLocalSound(
                                                worldPosition.getX(),
                                                worldPosition.getY(),
                                                worldPosition.getZ(),
                                                vazkii.botania.common.block.ModBlocks.terraPlate.defaultBlockState()
                                                        .getSoundType()
                                                        .getPlaceSound(),
                            null,
                            1.0F,
                            2.0F,
                            false
                    );
                    break;
                }

                if (getItem(0).isEmpty()) {
                    manaToGet = recipe.getManaUsage();
                    currentRecipe = recipe;
                    hasCraft = true;
                    break;
                }

                if (isItemEqual(recipe.getOutput(), getItem(0)) &&
                        getItem(0).getCount() < recipe.getOutput().getMaxStackSize()) {
                    manaToGet = recipe.getManaUsage();
                    currentRecipe = recipe;
                    hasCraft = true;
                    break;
                }
            }
            recipeID++;
        }

        if (!hasCraft) {
            currentRecipe = null;
            mana = 0;
            manaToGet = 0;
        }

        if (manaToGet != wasManaToGet) {
            hasUpdate = true;
        }

        requestUpdate = hasUpdate;
    }

    private void updateClient() {
        if (mana > 0) {
            double worldTime = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
            worldTime += new Random(worldPosition.asLong()).nextInt(360);
            float indetY = (float) (Math.sin(worldTime / 18.0) / 24.0);
            float ticks = 100.0F * getCurrentMana() / manaToGet;
            int totalSpiritCount = 3;
            double tickIncrement = 360.0 / totalSpiritCount;
            int speed = 5;
            double wticks = ticks * speed - tickIncrement;
            double r = Math.sin((ticks - 100.0) / 10.0) * 0.5;
            double g = Math.sin(wticks * Math.PI / 180.0 * 0.55);
            float size = 0.4F;

            for (int i = 0; i < totalSpiritCount; i++) {
                double x = worldPosition.getX() + Math.sin(wticks * Math.PI / 180.0) * r + 0.5;
                double y = worldPosition.getY() - indetY + 0.85 + Math.abs(r) * 0.7;
                double z = worldPosition.getZ() + Math.cos(wticks * Math.PI / 180.0) * r + 0.5;
                wticks += tickIncrement;

                int color = 0x24B900;
                if (currentRecipe != null) {
                    color = currentRecipe.getColor();
                }

                float[] hsb = Color.RGBtoHSB(color & 255, color >> 8 & 255, color >> 16 & 255, null);
                int color1 = Color.HSBtoRGB(hsb[0], hsb[1], ticks / 100.0F);
                float[] colorsfx = {
                        (color1 & 255) / 255.0F,
                        (color1 >> 8 & 255) / 255.0F,
                        (color1 >> 16 & 255) / 255.0F
                };

                WispParticleData wpd = WispParticleData.wisp(0.85F * size, colorsfx[0], colorsfx[1], colorsfx[2]);
                level.addParticle(wpd, x, y, z, (float)g * 0.05F, 0.0F, 0.25F);

                wpd = WispParticleData.wisp((float) Math.random() * 0.1F + 0.1F * size,
                        colorsfx[0], colorsfx[1], colorsfx[2]);
                level.addParticle(wpd, x, y, z,
                        (float)(Math.random() - 0.5) * 0.05F,
                        (float)(Math.random() - 0.5) * 0.05F,
                        (float)(Math.random() - 0.5) * 0.05F);

                if (ticks == 100.0F) {
                    for (int j = 0; j < 12; j++) {
                        wpd = WispParticleData.wisp((float) Math.random() * 0.15F + 0.15F * size,
                                colorsfx[0], colorsfx[1], colorsfx[2]);
                        level.addParticle(wpd,
                                worldPosition.getX() + 0.5,
                                worldPosition.getY() + 1.1 - indetY,
                                worldPosition.getZ() + 0.5,
                                (float)(Math.random() - 0.5) * 0.125F * size,
                                (float)(Math.random() - 0.5) * 0.125F * size,
                                (float)(Math.random() - 0.5) * 0.125F * size);
                    }
                }
            }
        }
    }

    public static boolean isItemEqual(ItemStack stack, ItemStack stack1) {
        return ItemStack.isSame(stack, stack1) && ItemStack.tagMatches(stack, stack1);
    }

    private int addItemStack(ItemStack stack) {
        for (int i = 1; i < getContainerSize(); i++) {
            if (getItem(i).isEmpty()) {
                ItemStack stackToAdd = stack.copy();
                setItem(i, stackToAdd);
                return stack.getCount();
            }
            if (isItemEqual(stack, getItem(i)) && getItem(i).getCount() < stack.getMaxStackSize()) {
                int count = Math.min(stack.getCount(), stack.getMaxStackSize() - getItem(i).getCount());
                getItem(i).grow(count);
                return count;
            }
        }
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("mana", mana);
        tag.putInt("manaToGet", manaToGet);
        tag.putBoolean("requestUpdate", requestUpdate);
        tag.putInt("recipeID", currentRecipe == null ? -1 : recipeID);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mana = tag.getInt("mana");
        manaToGet = tag.getInt("manaToGet");
        requestUpdate = tag.getBoolean("requestUpdate");
        int recipeID = tag.getInt("recipeID");
        if (recipeID == -1) {
            currentRecipe = null;
        } else {
            currentRecipe = AdvancedBotanyAPI.advancedPlateRecipes.get(recipeID);
        }
    }

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    public boolean isFull() {
        return mana >= manaToGet;
    }

    @Override
    public void recieveMana(int mana) {
        this.mana = Math.min(this.mana + mana, manaToGet);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isFull();
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(ISparkEntity entity) {
    }

    @Override
    @Nullable
    public IManaSpark getAttachedSpark() {
        java.util.List<net.minecraft.world.entity.Entity> sparks = level.getEntitiesOfClass(
                net.minecraft.world.entity.Entity.class,
                new AABB(worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(),
                        worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1),
                entity -> entity instanceof ISparkEntity && entity instanceof IManaSpark);

        if (sparks.size() == 1) {
            Entity sparkEntity = sparks.get(0);
            if (sparkEntity instanceof IManaSpark manaSpark) {
                return manaSpark;
            }
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull();
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, manaToGet - getCurrentMana());
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 2, 3};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        for (int i = 1; i < getContainerSize(); i++) {
            ItemStack slotStack = getItem(i);
            if (!slotStack.isEmpty() && slotStack.getCount() == slotStack.getMaxStackSize()
                    && isItemEqual(stack, slotStack)) {
                return false;
            }
        }
        return direction == Direction.UP && slot != 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return (direction == Direction.DOWN && slot == 0) ||
                (direction != Direction.DOWN && direction != Direction.UP && slot != 0);
    }
}
*/