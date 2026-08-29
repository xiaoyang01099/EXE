package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.fbos;

import net.minecraft.client.Minecraft;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer.FBO;

public class MainFBORender {
    private FBO fbo; // 后处理主 FBO，CA0 保存可见叠加，CA1 保存 bloom source，CA2 保存描边 mask/type。

    public MainFBORender() {
        this(FBO.DEPTH_TEXTURE, FBO.defaultInternalFormatForDepthBufferType(FBO.DEPTH_TEXTURE));
    }

    public MainFBORender(int depthBufferType, int depthInternalFormat) {
        int width = Minecraft.getInstance().getWindow().getWidth();
        int height = Minecraft.getInstance().getWindow().getHeight();
        fbo = new FBO(width, height, depthBufferType, 3, depthInternalFormat);
    }

    // 按当前深度格式重建主 FBO。
    public void resize(int width, int height) {
        fbo.resize(width, height);
    }

    // 主 RenderTarget 的 depth/stencil 格式变化时同步重建主 FBO。
    public void resize(int width, int height, int depthBufferType, int depthInternalFormat) {
        fbo.resize(width, height, depthBufferType, depthInternalFormat);
    }

    public FBO getFbo() {
        return fbo;
    }
}