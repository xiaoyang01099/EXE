package net.xiaoyang010.ex_enigmaticlegacy.Compat.Botania.Block.tile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;

public class AlphirinePortal extends Entity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK =
            SynchedEntityData.defineId(AlphirinePortal.class, EntityDataSerializers.ITEM_STACK);

    private static final ItemStack DEFAULT_ITEM_STACK = new ItemStack(Items.AIR);

    public AlphirinePortal(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public AlphirinePortal(Level level) {
        this(ModEntities.ALPHIRINE_PORTAL.get(), level);
        this.entityData.set(DATA_ITEM_STACK, DEFAULT_ITEM_STACK.copy());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ITEM_STACK, DEFAULT_ITEM_STACK);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount >= 40) {
            if (this.getStack().equals(DEFAULT_ITEM_STACK)) {
                this.discard();
                return;
            }

            if (!this.getLevel().isClientSide) {
                ItemEntity itemResult = new ItemEntity(
                        this.getLevel(),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        this.getStack()
                );
                itemResult.setDefaultPickUpDelay();
                this.getLevel().addFreshEntity(itemResult);
                this.discard();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.tickCount = compound.getInt("portalTick");

        if (compound.contains("dropStack")) {
            CompoundTag stackTag = compound.getCompound("dropStack");
            ItemStack stack = ItemStack.of(stackTag);
            this.setStack(stack);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("portalTick", this.tickCount);

        ItemStack stack = this.getStack();
        CompoundTag stackTag = new CompoundTag();
        if (!stack.isEmpty()) {
            stack.save(stackTag);
        }
        compound.put("dropStack", stackTag);
    }

    public ItemStack getStack() {
        return this.entityData.get(DATA_ITEM_STACK);
    }

    public void setStack(ItemStack stack) {
        this.entityData.set(DATA_ITEM_STACK, stack);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        return false;
    }
}