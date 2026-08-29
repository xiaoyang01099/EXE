package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.bloomQueue;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Util.MathUtil;
import org.xiaoyang.ex_enigmaticlegacy.Client.renderer.others.BeamRender;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.colorful.ColorfulEntity;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue.EntityQueue;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.BeamRenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.BeamShader;

public class ColorfulCoinQueue extends EntityQueue<ColorfulEntity> {
    public ColorfulCoinQueue() {
        super();
    }

    @Override
    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float parTick, Matrix4f viewMatrix) {
        if (!BeamShader.isLoaded()) return;

        BeamRender.BeamStyle style = BeamRender.COLORFUL;
        float time = MathUtil.getClientTime(parTick);
        BeamShader.setEffectParams(time, style.bloomStrength, style.noiseStrength, 0.0f);
        BeamShader.setRenderFlags(0, 1, 0, 0);
        BeamShader.setBeamColors(
                style.coreR, style.coreG, style.coreB,
                style.innerR, style.innerG, style.innerB,
                style.outerR, style.outerG, style.outerB
        );
        BeamShader.setView(viewMatrix);

        VertexConsumer consumer = fboBuffer.getBuffer(BeamRenderType.getRenderType());
        for (ColorfulEntity beam : entities) {
            BeamRender.writeBeam(consumer, beam, camera, parTick, style);
        }
        fboBuffer.endBatch(BeamRenderType.getRenderType());
    }
}
