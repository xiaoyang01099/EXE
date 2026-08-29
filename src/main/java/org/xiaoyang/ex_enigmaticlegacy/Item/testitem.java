package org.xiaoyang.ex_enigmaticlegacy.Item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.ExcaliburChargeParticleEffects;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.trident.TridentLightningStrikeEntity;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system.ParticleEmitTask;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleMaterialKey;
import org.xiaoyang.ex_enigmaticlegacy.Exe;
import org.xiaoyang.ex_enigmaticlegacy.Init.ModEntities;


public class testitem extends Item {
    public testitem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    public void placeCircle(ServerLevel level, double cx, double cy, double cz,
                            int radius, int pointCount) {
        for (int i = 0; i < pointCount; i++) {
            double angle = i * 2.0 * Math.PI / pointCount;
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            Vec3 pos = new Vec3(
                cx + offsetX,
                cy + 0.1D,
                cz + offsetZ
            );
            spawnLightningAtPos(pos, level);
        }
    }

    public void spawnLightningAtPos(Vec3 pos, ServerLevel level) {
        TridentLightningStrikeEntity strike = new TridentLightningStrikeEntity(
                ModEntities.TRIDENT_LIGHTNING_STRIKE.get(), level);

        int visualSeed = level.random.nextInt();
        strike.setStrikeData(pos, null, true, visualSeed);

        level.addFreshEntity(strike);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level pLevel = context.getLevel();
        if (pLevel.isClientSide()) {
            BlockPos pos = context.getClickedPos();
            addTestRisingShockwaveParticle(context);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }



    // 右键方块放置金色三噪声螺旋光效，方便观察 fx_noise015 替换后的流动 mask。
    public void addTestGoldenSpiralEffectPreview(Level level, UseOnContext context) {
        if (Exe.POST == null) return;
        Direction face = context.getClickedFace();
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        Vec3 center = context.getClickedPos().getCenter().add(normal.scale(0.75D));
        long seed = level.getGameTime() * 83L
                ^ Double.doubleToLongBits(center.x)
                ^ Double.doubleToLongBits(center.y)
                ^ Double.doubleToLongBits(center.z);
        Exe.POST.effects().addGoldenSpiralEffect(center, seed);
    }

    // 在点击方块顶面生成一个长生命周期的水平基础能量法阵，方便观察径向纹理、圆形遮罩和 Bloom。
    public void addTestMagicCircleEnergyParticle(UseOnContext context) {
        if (Exe.POST == null) return;
        Vec3 center = context.getClickedPos().getCenter().add(0.0D, 0.55D, 0.0D);

        // 测试粒子保持静止并延长到八秒，避免正常蓄力短生命周期影响材质细节观察。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(center)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(10.0F)
                .gravity(0.0F)
                .size(7.50F, 7.50F, 0.0F)
                .color(0xFFE9A0, 0.12F)
                .midColor(0xFFB21A, 1F)
                .midColorTime(0.38F)
                .endColor(0x7A1900, 0.1F)
                .material(ParticleMaterialKey.MAGIC_CIRCLE_ENERGY)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1));
    }

    // 在点击方块顶面生成正式参数的冲击波法阵，方便单独观察 trail_2 径向效果。
    public void addTestShockwaveMagicCircleParticle(UseOnContext context) {
        if (Exe.POST == null) return;
        Vec3 center = context.getClickedPos().getCenter().add(0.0D, 0.55D, 0.0D);
        ExcaliburChargeParticleEffects.emitShockwaveMagicCircle(center);
    }

    // 在点击方块外侧放置一个根据玩家水平朝向展开的竖直 EX 剑气粒子。
    public void addTestExSwordWaveParticle(UseOnContext context) {
        if (Exe.POST == null || context.getPlayer() == null) return;
        Player player = context.getPlayer();
        Vec3 faceNormal = Vec3.atLowerCornerOf(context.getClickedFace().getNormal());
        Vec3 center = context.getClickedPos().getCenter()
                .add(faceNormal.scale(0.65D))
                .add(0.0D, 1.45D, 0.0D);

        // 只使用水平视线保证剑气平面竖直；视线接近竖直时按玩家 YRot 构造回退方向。
        Vec3 look = player.getLookAngle();
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() < 1.0E-6D) {
            double yaw = Math.toRadians(player.getYRot());
            horizontalLook = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        } else {
            horizontalLook = horizontalLook.normalize();
        }

        // 单粒子保持十秒，三段尺寸便于直接观察出生、展开和结束阶段。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(center)
                .direction((float) horizontalLook.x, (float) horizontalLook.y, (float) horizontalLook.z)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(10.0F)
                .gravity(0.0F)
                .sizeOverLife(
                        0.25F, 1.60F,
                        1.20F, 3.20F,
                        1.65F, 3.80F,
                        0.35F)
                .fixedRotation(0.0F)
                .color(0xFF9E1A, 1.0F)
                .midColor(0xFF9E1A, 1.0F)
                .endColor(0xFF9E1A, 0.0F)
                .material(ParticleMaterialKey.EX_SWORD_WAVE)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1));
    }

    // 在点击方块外侧生成一个 ai_star 贴图十字粒子，方便观察相机朝向和 rotationSpeed 自旋。
    public void addTestStarTextureParticle(UseOnContext context) {
        if (Exe.POST == null) return;
        Vec3 faceNormal = Vec3.atLowerCornerOf(context.getClickedFace().getNormal());
        Vec3 center = context.getClickedPos().getCenter()
                .add(faceNormal.scale(0.75D))
                .add(0.0D, 1.10D, 0.0D);

        // 单个粒子保持三秒，固定尺寸倍率便于确认贴图 R 通道遮罩和逆时针自旋。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(center)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(5.0F)
                .gravity(0.0F)
                .sizeOverLife(
                        13.20F, 13.20F,
                        24.60F, 24.60F,
                        20.20F, 20.20F,
                        0.28F)
                .fixedRotation(0.0F)
                .rotationSpeed(1.2F)
                .fixedSizeScale()
                .color(0xFFF4A8, 0.5F)
                .midColor(0xFFFFFF, 0.95F)
                .midColorTime(0.22F)
                .endColor(0xFF9E1A, 0.1F)
                .material(ParticleMaterialKey.STAR_TEXTURE)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1));
    }

    // 在点击方块顶面生成一个上窄下宽的圆台上升冲击波，方便观察 t_fx_tile_0016、1-Fresnel 和 UV 流动。
    public void addTestRisingShockwaveParticle(UseOnContext context) {
        if (Exe.POST == null) return;
        Vec3 center = context.getClickedPos().getCenter().add(0.0D, 0.12D, 0.0D);

        // 单个圆台粒子保持数秒，三段尺寸用于观察主体从底部爆开到上升扩张的过程。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(center)
                .direction(0.0F, 1.0F, 0.0F)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(43.20F)
                .gravity(0.0F)
                .size(5.50F, 50.50F, 0.0F)
                .fixedRotation(0.0F)
                .fixedSizeScale()
                .color(0xFFB12A, 0.62F)
                .midColor(0xFFF1A8, 0.92F)
                .midColorTime(0.32F)
                .endColor(0xFF5A08, 0.4F)
                .risingShockwave(2.00F, 1.0F, 3.00F, 1.0F, 0.35F)
                .material(ParticleMaterialKey.RISING_SHOCKWAVE)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1));
    }

    // 在点击方块上方生成一个高 200、宽 10 的定向光效长光柱，方便观察 DIRECTED_LIGHT_EFFECT 几何和遮罩。
    public void addTestDirectedLightColumnParticle(UseOnContext context) {
        if (Exe.POST == null) return;
        Vec3 center = context.getClickedPos().getCenter().add(0.0D, 100.50D, 0.0D);

        // 光柱中心上移半个高度，让底部贴近点击方块顶部，便于观察完整 200 格高度。
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(center)
                .direction(0.0F, 0.0F, 1.0F)
                .speed(0.0F, 0.0F)
                .spread(0.0F)
                .life(12.0F)
                .gravity(0.0F)
                .size(10.0F, 200.0F, 0.0F)
                .fixedRotation(0.0F)
                .fixedSizeScale()
                .lightEffectMask(0.50F, 0.16F)
                .color(0xFFF4A8, 0.60F)
                .midColor(0xFFFFFF, 1.00F)
                .midColorTime(0.25F)
                .endColor(0xFF9E1A, 0.0F)
                .material(ParticleMaterialKey.DIRECTED_LIGHT_EFFECT)
                .motion(ParticleEmitTask.MOTION_BALLISTIC)
                .rate(0)
                .duration(0.0F)
                .burst(1));
    }

    // 右键方块提交新 GPU 粒子综合预览，当前默认测试随机运动的 SDF 粒子。
    public void addTestGpuParticleFeaturePreview(Level level, UseOnContext context) {
        if (Exe.POST == null || context.getPlayer() == null) return;
        Direction face = context.getClickedFace();
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        Vec3 center = context.getClickedPos().getCenter().add(normal.scale(1.05D)).add(0.0D, 0.65D, 0.0D);
        Vec3 look = context.getPlayer().getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

//        addTestTurbulentRiseParticles(center);
//        addTestRandomMovingParticles(center.add(right.scale(-0.85D)));
//        addTestLightEffectParticles(center, look);
        addTestLightEffectParticles2(center, look);// 光效粒子
//        addTestReverseLightEffectParticles(center.add(right.scale(0.85D)), look);
    }





    public void addTestLightEffectParticles2(Vec3 center, Vec3 direction) {
        Exe.POST.addParticle(new ParticleEmitTask()
                .position(center)
                .direction(0.0F, 1.0F, 0.0F)
//                .directionPlaneRandom(0.35F, 2.4F, 1.0F)
                .speed(0.5F, 0.00F)
                .speedCurve(1.15F)
                .spread(0.8f)
                .life(10.55F)
                .gravity(0.0F)
                .size(0.52F, 2.42F, 0.0F)
                .color(0xFFF4A8, 0.1F)
                .midColor(0xFFB000, 0.82F)
                .endColor(0x5A0800, 0.0F)
//                .midColorTime(0.36F)
                .material(ParticleMaterialKey.LIGHT_EFFECT)
                .rate(1)
                .duration(3.0F)
                .burst(1));
    }

}
