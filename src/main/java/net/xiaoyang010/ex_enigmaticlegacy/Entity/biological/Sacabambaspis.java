/*package net.xiaoyang010.ex_enigmaticlegacy.entity.biological;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class Sacabambaspis extends WaterAnimal implements IAnimatable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public Sacabambaspis(EntityType<? extends WaterAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.isInWater()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sacabambaspis.swim", true));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sacabambaspis.flop", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isInWater()) {
            if (this.getDeltaMovement().y < 0.0D) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
            }

            if (this.isInWater() && !this.moveControl.hasWanted()) {
                this.setDeltaMovement(
                        this.getDeltaMovement().add(0.0D, -0.005D, 0.0D)
                );
            }
        } else {
            if (this.onGround) {
                this.setDeltaMovement(
                        this.getDeltaMovement().add(
                                (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F,
                                0.5D,
                                (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F
                        )
                );
                this.setYRot(this.random.nextFloat() * 360.0F);
                this.onGround = false;
                this.hasImpulse = true;
            }
        }
    }
}*/