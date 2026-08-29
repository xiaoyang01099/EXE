package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import java.util.ArrayList;
import java.util.List;

public class InstanceLayout {
    public final int strideFloats;
    public final List<AttrEntry> attributes = new ArrayList<>();

    public InstanceLayout(int strideFloats) {
        if (strideFloats <= 0) {
            throw new IllegalArgumentException("实例布局 strideFloats 必须大于 0");
        }
        this.strideFloats = strideFloats;
    }

    public static InstanceLayout create(int strideFloats) {
        return new InstanceLayout(strideFloats);
    }

    public InstanceLayout attr(int location, int size, int offset) {
        if (location < 0) {
            throw new IllegalArgumentException("实例 attribute location 不能小于 0");
        }
        if (size < 1 || size > 4) {
            throw new IllegalArgumentException("实例 attribute size 必须在 1 到 4 之间");
        }
        if (offset < 0 || offset + size > strideFloats) {
            throw new IllegalArgumentException("实例 attribute 超出 strideFloats 范围");
        }
        attributes.add(new AttrEntry(location, size, offset));
        return this;
    }

    public int count() {
        return attributes.size();
    }

    public static class AttrEntry {
        public final int location;
        public final int size;
        public final int offset;

        public AttrEntry(int location, int size, int offset) {
            this.location = location;
            this.size = size;
            this.offset = offset;
        }
    }
}
