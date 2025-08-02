package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.others.EntitySword;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModEntities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModRarities;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.api.EXEAPI;

public class Claymore extends SwordItem {
    private final float damage;
    private final int pierceCount;

    public Claymore(float damage, int pierceCount) {
        super(EXEAPI.MIRACLE_ITEM_TIER, 90, -2.4F, new Properties()
                .tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR)
                .rarity(ModRarities.MIRACLE)
        );
        this.damage = damage;
        this.pierceCount = pierceCount;
    }

    public Claymore(float damage) {
        this(damage, 10);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            EntitySword sword = new EntitySword(ModEntities.ENTITY_SWORD.get(), level, player);

            sword.setDamage(this.damage);
            sword.setAttacker(player.getName().getString());
            sword.setMaxPierce(this.pierceCount);

            sword.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
            sword.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 1.0F);

            level.addFreshEntity(sword);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}