package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MiaoOutlineCapturedMaskBuffer {
    public final List<MiaoOutlineCapturedBatch> batches = new ArrayList<>();

    public void clear() {
        batches.clear();
    }

    public MiaoOutlineCapturedBatch beginBatch(VertexFormat.Mode mode, ResourceLocation texture) {
        MiaoOutlineCapturedBatch batch = new MiaoOutlineCapturedBatch(mode, texture);
        batches.add(batch);
        return batch;
    }

    public boolean isEmpty() {
        for (MiaoOutlineCapturedBatch batch : batches) {
            if (!batch.isEmpty()) return false;
        }
        return true;
    }

    public List<MiaoOutlineCapturedBatch> getBatches() {
        return Collections.unmodifiableList(batches);
    }
}
