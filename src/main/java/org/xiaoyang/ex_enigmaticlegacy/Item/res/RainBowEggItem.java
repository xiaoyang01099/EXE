package org.xiaoyang.ex_enigmaticlegacy.Item.res;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.xiaoyang.ex_enigmaticlegacy.Entity.biological.Xingyun2825Entity;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;

import java.util.List;

public class RainBowEggItem extends Item {
    public RainBowEggItem(){
        super(new Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(p_41432_);
        Xingyun2825Entity entityToSpawn1 = ModEntities.XINGYUN2825.get().create(p_41432_);
        entityToSpawn.moveTo(Vec3.atBottomCenterOf(new BlockPos((int) p_41433_.getX(), (int) p_41433_. getY(), (int) p_41433_. getZ())));
        entityToSpawn1.moveTo(Vec3.atBottomCenterOf(new BlockPos((int) p_41433_.getX(), (int) p_41433_. getY(), (int) p_41433_. getZ())));
        entityToSpawn.setVisualOnly(true);
        p_41432_.addFreshEntity(entityToSpawn);
        p_41432_.addFreshEntity(entityToSpawn1);
        p_41433_.getInventory().removeItem(this.getDefaultInstance());
        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.unknown_crystal.clear_tip"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.unknown_crystal.name");
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof Xingyun2825Entity entity1){
            entity1.costomDie();
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
