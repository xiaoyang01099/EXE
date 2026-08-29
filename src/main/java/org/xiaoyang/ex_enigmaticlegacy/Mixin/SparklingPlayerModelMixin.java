package org.xiaoyang.ex_enigmaticlegacy.Mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiaoyang.ex_enigmaticlegacy.Client.particle.state.SparklingFlightClientState;

@Mixin(PlayerModel.class)
public abstract class SparklingPlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {
    private static final float ARM_X_ROT = 0.18F;
    private static final float ARM_Y_ROT = 0.18F;
    private static final float LEG_X_ROT = 0.08F;
    private static final float LEG_Y_ROT = 0.05F;

    public SparklingPlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    public void ex_enigmaticlegacy$lockSparklingFruitFlightLimbs(T entity, float limbSwing, float limbSwingAmount,
                                                           float ageInTicks, float netHeadYaw, float headPitch,
                                                           CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        if (!SparklingFlightClientState.isHorizontalPoseActive(player.getId())) return;
        if (player.getForcedPose() != Pose.FALL_FLYING && player.getPose() != Pose.FALL_FLYING) return;

        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;

        this.rightArm.xRot = ARM_X_ROT;
        this.leftArm.xRot = ARM_X_ROT;
        this.rightArm.yRot = ARM_Y_ROT;
        this.leftArm.yRot = -ARM_Y_ROT;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;

        this.rightLeg.xRot = LEG_X_ROT;
        this.leftLeg.xRot = LEG_X_ROT;
        this.rightLeg.yRot = LEG_Y_ROT;
        this.leftLeg.yRot = -LEG_Y_ROT;
        this.rightLeg.zRot = 0.0F;
        this.leftLeg.zRot = 0.0F;
    }
}
