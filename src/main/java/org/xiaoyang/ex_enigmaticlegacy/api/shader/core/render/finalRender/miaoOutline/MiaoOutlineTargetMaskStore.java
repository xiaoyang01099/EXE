package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.finalRender.miaoOutline;

import java.util.HashMap;
import java.util.Map;

public class MiaoOutlineTargetMaskStore {
    public static final Map<Integer, MiaoOutlineCapturedMaskBuffer> BUFFERS = new HashMap<>();

    public static MiaoOutlineCapturedMaskBuffer beginCapture(int entityId) {
        MiaoOutlineCapturedMaskBuffer buffer = BUFFERS.computeIfAbsent(entityId, id -> new MiaoOutlineCapturedMaskBuffer());
        buffer.clear();
        return buffer;
    }

    public static MiaoOutlineCapturedMaskBuffer get(int entityId) {
        return BUFFERS.get(entityId);
    }

    public static void clear(int entityId) {
        MiaoOutlineCapturedMaskBuffer buffer = BUFFERS.get(entityId);
        if (buffer != null) {
            buffer.clear();
        }
    }

    public static void clearAll() {
        BUFFERS.clear();
    }
}
