package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.texture;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.material.ParticleMaterialRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AtlasReloadListener implements PreparableReloadListener {
    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepProfiler, ProfilerFiller applyProfiler, Executor prepExecutor, Executor applyExecutor) {
        SpriteLoader spriteLoader = SpriteLoader.create(EXETextureAtlas.EXE_TOOL_ATLAS);
        return spriteLoader.loadAndStitch(manager, EXETextureAtlas.EXE_TOOL_ATLAS_LOCATION, 4, prepExecutor)
                .thenCompose(SpriteLoader.Preparations::waitForUpload)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(preparations -> {
                    EXETextureAtlas.EXE_TOOL_ATLAS.upload(preparations);
                    EXETextureAtlas.applyLinearFilter(true);
                    ParticleMaterialRegistry.markDirty();
                }, applyExecutor);
    }
}
