package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstanceVBO {
    public final InstanceLayout layout;
    public final int maxInstances;
    public int vboID = 0;
    public int vaoID = 0;
    public final FloatBuffer buffer;

    public InstanceVBO(InstanceLayout layout, int maxInstances) {
        if (layout == null) {
            throw new IllegalArgumentException("实例布局不能为空");
        }
        if (maxInstances <= 0) {
            throw new IllegalArgumentException("maxInstances 必须大于 0");
        }
        this.layout = layout;
        this.maxInstances = maxInstances;
        this.buffer = BufferUtils.createFloatBuffer(maxInstances * layout.strideFloats);
        createVBO();
    }

    public void createVBO() {
        vboID = glGenBuffers();
        long bytes = (long) maxInstances * layout.strideFloats * 4;
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, bytes, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void attachTo(int vaoID) {
        if (vaoID == 0) {
            throw new IllegalArgumentException("目标 VAO 不能为 0");
        }
        this.vaoID = vaoID;
        int strideBytes = layout.strideFloats * 4;

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBindVertexArray(vaoID);

        for (InstanceLayout.AttrEntry attr : layout.attributes) {
            int offsetBytes = attr.offset * 4;
            glVertexAttribPointer(attr.location, attr.size, GL_FLOAT, false, strideBytes, offsetBytes);
            glVertexAttribDivisor(attr.location, 1);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void update(float[] instanceData, int instanceCount) {
        validateInstanceCount(instanceCount);
        int floatCount = instanceCount * layout.strideFloats;
        if (instanceData == null || instanceData.length < floatCount) {
            throw new IllegalArgumentException("实例数组长度不足");
        }
        buffer.clear();
        buffer.put(instanceData, 0, floatCount);
        buffer.flip();

        uploadFlippedBuffer(buffer, floatCount);
    }

    public void update(FloatBuffer dataBuffer, int instanceCount) {
        validateInstanceCount(instanceCount);
        int floatCount = instanceCount * layout.strideFloats;
        if (dataBuffer == null || dataBuffer.position() < floatCount) {
            throw new IllegalArgumentException("实例 FloatBuffer 写入数量不足");
        }
        dataBuffer.flip();
        uploadFlippedBuffer(dataBuffer, floatCount);
    }

    public void uploadFlippedBuffer(FloatBuffer dataBuffer, int floatCount) {
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long) maxInstances * layout.strideFloats * 4, GL_STREAM_DRAW);
        if (floatCount > 0) {
            dataBuffer.limit(floatCount);
            glBufferSubData(GL_ARRAY_BUFFER, 0, dataBuffer);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void validateInstanceCount(int instanceCount) {
        if (instanceCount < 0) {
            throw new IllegalArgumentException("instanceCount 不能小于 0");
        }
        if (instanceCount > maxInstances) {
            throw new IllegalArgumentException("instanceCount 不能超过 maxInstances");
        }
    }

    public FloatBuffer getBuffer() {
        return buffer;
    }

    public void enable(int... baseAttribs) {
        glBindVertexArray(vaoID);
        for (int loc : baseAttribs) {
            glEnableVertexAttribArray(loc);
        }
        for (InstanceLayout.AttrEntry attr : layout.attributes) {
            glEnableVertexAttribArray(attr.location);
        }
    }

    public void disable(int... baseAttribs) {
        for (InstanceLayout.AttrEntry attr : layout.attributes) {
            glDisableVertexAttribArray(attr.location);
        }
        for (int loc : baseAttribs) {
            glDisableVertexAttribArray(loc);
        }
        glBindVertexArray(0);
    }

    public void cleanup() {
        if (vboID != 0) {
            glDeleteBuffers(vboID);
            vboID = 0;
        }
    }
}
