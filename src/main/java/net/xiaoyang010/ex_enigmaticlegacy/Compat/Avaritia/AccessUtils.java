package net.xiaoyang010.ex_enigmaticlegacy.Compat.Avaritia;

import net.minecraft.client.renderer.RenderStateShard;

public final class AccessUtils {
    public static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShardAccess.EQUAL_DEPTH_TEST;
    public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShardAccess.TRANSLUCENT_TRANSPARENCY;
    public static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShardAccess.BLOCK_SHEET_MIPPED;
    public static final RenderStateShard.LightmapStateShard LIGHT_MAP = RenderStateShardAccess.LIGHT_MAP;

    private static final class RenderStateShardAccess extends RenderStateShard {
        private static final LightmapStateShard LIGHT_MAP = RenderStateShard.LIGHTMAP;
        private static final DepthTestStateShard EQUAL_DEPTH_TEST = RenderStateShard.EQUAL_DEPTH_TEST;
        private static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = RenderStateShard.TRANSLUCENT_TRANSPARENCY;
        private static final TextureStateShard BLOCK_SHEET_MIPPED = RenderStateShard.BLOCK_SHEET_MIPPED;

        private RenderStateShardAccess(String pName, Runnable pSetupState, Runnable pClearState) {
            super(pName, pSetupState, pClearState);
            throw new AssertionError();
        }
    }
}