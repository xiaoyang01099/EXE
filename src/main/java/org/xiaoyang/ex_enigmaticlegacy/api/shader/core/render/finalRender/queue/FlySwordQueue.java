package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.queue;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.FlySwordEntity;
import org.xiaoyang.ex_enigmaticlegacy.Entity.others.sword.FlySwordEntityRender;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.type.TrailRibbonRenderType;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.shader.TrailRibbonShader;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture.EXETextureAtlas;

public class FlySwordQueue extends EntityQueue<FlySwordEntity>{
    public Matrix4f modelMatrix;
    static TextureAtlasSprite sprite2;

    public FlySwordQueue() {
        super();
        sprite2 = EXETextureAtlas.getTextureLocation(EXETextureAtlas.multi_gradient);
    }

    public void updateModelMatrix(Matrix4f model){
        this.modelMatrix = new Matrix4f(model);
    }


    public void render(MultiBufferSource.BufferSource fboBuffer, Camera camera, float parTick, Matrix4f viewMatrix){
        if(sprite2 == null){
            sprite2 = EXETextureAtlas.getTextureLocation(EXETextureAtlas.multi_gradient);
        }
        if (TrailRibbonShader.spriteUV0 != null) {
            TrailRibbonShader.spriteUV0.set(sprite2.getU0(), sprite2.getV0(), sprite2.getU1(), sprite2.getV1());
        }


        for(FlySwordEntity entity : entities){
            FlySwordEntityRender.renderTrail(entity, parTick, modelMatrix, fboBuffer, camera.getPosition());
        }
        fboBuffer.endBatch(TrailRibbonRenderType.getRenderType());
    }

}
