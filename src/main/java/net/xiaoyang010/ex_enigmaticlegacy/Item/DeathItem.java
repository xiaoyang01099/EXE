/*package net.xiaoyang010.ex_enigmaticlegacy.item;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.pickloli_new.test.clear;

import java.util.Collection;
import java.util.Collections;

public class DeathItem extends Item {
    public DeathItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
        if (p_41406_ instanceof Player player) {
           // LolipickaxeItem.strongerRemove = true;
            clear.superClearInventoryPlayerTick(player.getInventory());
            if (p_41406_ instanceof LivingEntity livingEntity) {
                net.xiaoyang010.ex_enigmaticlegacy.item.LolipickaxeItem.addBanEntity(livingEntity);
            }
        }
        super.inventoryTick(p_41404_, p_41405_, p_41406_, p_41407_, p_41408_);
    }

    @Override
    public Collection<CreativeModeTab> getCreativeTabs() {
        return Collections.singletonList(ModTabs.TAB_EXENIGMATICLEGACY_ITEM);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_41432_, Player p_41433_, InteractionHand p_41434_) {
            // LolipickaxeItem.strongerRemove = true;
            clear.superClearInventoryPlayerTick(p_41433_.getInventory());
            net.xiaoyang010.ex_enigmaticlegacy.item.LolipickaxeItem.addBanEntity(p_41433_);
        return super.use(p_41432_, p_41433_, p_41434_);
    }
}
*/