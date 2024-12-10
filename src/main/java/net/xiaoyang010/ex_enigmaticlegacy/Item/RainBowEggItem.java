package net.xiaoyang010.ex_enigmaticlegacy.Item;

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
import net.xiaoyang010.ex_enigmaticlegacy.Entity.Xingyun2825Entity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RainBowEggItem extends Item {
    public RainBowEggItem(){
        super(new Item.Properties().tab(ModTabs.TAB_EXENIGMATICLEGACY_ITEM).stacksTo(64).fireResistant().rarity(ModRarities.MIRACLE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
        LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(p_41432_);
        Xingyun2825Entity entityToSpawn1 = ModEntities.XINGYUN2825.get().create(p_41432_);
        entityToSpawn.moveTo(Vec3.atBottomCenterOf(new BlockPos(p_41433_.getX(),p_41433_. getY(),p_41433_. getZ())));
        entityToSpawn1.moveTo(Vec3.atBottomCenterOf(new BlockPos(p_41433_.getX(),p_41433_. getY(),p_41433_. getZ())));
        entityToSpawn.setVisualOnly(true);
        p_41432_.addFreshEntity(entityToSpawn);
        p_41432_.addFreshEntity(entityToSpawn1);
        p_41433_.getInventory().removeItem(this.getDefaultInstance());
        return super.use(p_41432_, p_41433_, p_41434_);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        p_41423_.add(Component.nullToEmpty("PS:打不过可以左键清除"));
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        return Component.nullToEmpty("未知水晶");
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof Xingyun2825Entity entity1){
                entity1.costomDie();
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
