package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import java.util.ArrayList;
import java.util.List;

public class VAOLayout {
    final List<AttributeEntry> attributes = new ArrayList<>();
    int[] indices;
    int vertexCount;

    public VAOLayout addAttribute(int index, int size, float[] data) {
        attributes.add(new AttributeEntry(index, size, data));
        return this;
    }

    public VAOLayout indices(int[] indices) {
        this.indices = indices;
        this.vertexCount = indices.length;
        return this;
    }

    public VAOLayout vertexCount(int count) {
        this.vertexCount = count;
        return this;
    }

    static class AttributeEntry {
        final int index;
        final int size;
        final float[] data;

        AttributeEntry(int index, int size, float[] data) {
            this.index = index;
            this.size = size;
            this.data = data;
        }
    }
}
