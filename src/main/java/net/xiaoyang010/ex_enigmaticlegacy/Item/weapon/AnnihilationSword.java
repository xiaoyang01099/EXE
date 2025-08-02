package net.xiaoyang010.ex_enigmaticlegacy.Item.weapon;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.xiaoyang010.ex_enigmaticlegacy.Entity.biological.Xingyun2825Entity;
import net.xiaoyang010.ex_enigmaticlegacy.Init.ModTabs;
import net.xiaoyang010.ex_enigmaticlegacy.Util.ColorText;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class AnnihilationSword extends SwordItem {

    public AnnihilationSword() {
        super(new Tier() {
            @Override
            public int getUses() {
                return 0;
            }

            @Override
            public float getSpeed() {
                return 500;
            }

            @Override
            public float getAttackDamageBonus() {
                return 1024-1;
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null;
            }
        }, 0, 0F, new Properties().fireResistant().stacksTo(1).tab(ModTabs.TAB_EXENIGMATICLEGACY_WEAPON_ARMOR));
    }

    @Override
    public Component getName(ItemStack pStack) {
        return new TranslatableComponent(ColorText.GetColor1("湮")+"灭の冲刺者");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer.level instanceof ServerLevel) {
            pLevel.explode(pPlayer,pPlayer.getX(),pPlayer.getY(),pPlayer.getZ(),1, Explosion.BlockInteraction.NONE);
            List<Entity> list1 = pPlayer.level.getEntities(pPlayer, new AABB(pPlayer.getX() - 40, pPlayer.getY() - 40, pPlayer.getZ() - 40, pPlayer.getX() + 40, pPlayer.getY() +40.0, pPlayer.getZ() + 40), (p_147140_) -> {
                return !(p_147140_ instanceof Xingyun2825Entity);
            });
            pPlayer.setDeltaMovement(pPlayer.deltaMovement.x*2,pPlayer.deltaMovement.y*2,pPlayer.deltaMovement.z*2);
            //pPlayer.lookAt();
            list1.forEach(entity -> {
                if (entity instanceof LivingEntity l ){
                    if (!(l instanceof Player player && player.isCreative())) {
                      l.setDeltaMovement(2*(pPlayer.getX()-l.getX())/Math.abs(pPlayer.getX()-l.getX()),2*(pPlayer.getY()-l.getY())/Math.abs((pPlayer.getY()-l.getY())),2*(pPlayer.getZ()-l.getZ())/Math.abs((pPlayer.getZ()-l.getZ())));
                      pLevel.addParticle(ParticleTypes.PORTAL,true,l.getX(),l.getY(),l.getZ(),pPlayer.getX(),pPlayer.getY(),pPlayer.getZ());
                        onEntitySwing(getDefaultInstance(),l);
                    }
                }
                //if (deathTick>900)level.explode(this,getX(),getY(),getZ(),5, Explosion.BlockInteraction.DESTROY);
            });

        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(new TranslatableComponent("§7》右键进行§c湮灭§7冲刺，摧毁撞到的生物和方块(WIP)"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public Collection<CreativeModeTab> getCreativeTabs() {
        Collection collection = super.getCreativeTabs();
        return collection;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (pEntity instanceof LivingEntity living){
            //living.hurtTime=-2;
            //living.deathTime=-2;
            //living.setHealth(living.getMaxHealth());
            living.hurtDuration=2;
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,10,3));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,10,4));
            living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,10,4));
            living.addEffect(new MobEffectInstance(MobEffects.HEAL,10,4));
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        entity.hurt(DamageSource.OUT_OF_WORLD, (entity.getMaxHealth()) / 20);
        return super.onEntitySwing(stack, entity);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity living) {
            entity.hurt(DamageSource.OUT_OF_WORLD, (living.getMaxHealth()) / 10);
            onEntitySwing(stack,living);
        }
        return super.onLeftClickEntity(stack, player, entity);
    }
}
