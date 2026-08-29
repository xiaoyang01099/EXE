package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class Loader {
    List<Integer> vaos = new ArrayList<>();
    List<Integer> vbos = new ArrayList<>();

    public VAOLayout newLayout() {
        return new VAOLayout();
    }

    public RawModel loadToVAO(VAOLayout layout) {
        int vaoID = createVAO();

        if (layout.indices != null) {
            bindIndicesBuffer(layout.indices);
        }

        for (VAOLayout.AttributeEntry attr : layout.attributes) {
            storeDataInAttributeList(attr.index, attr.size, attr.data);
        }

        unbindVAO();
        return new RawModel(vaoID, layout.vertexCount);
    }

    public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
        return loadToVAO(newLayout()
                .addAttribute(0, 3, positions)
                .addAttribute(1, 2, textureCoords)
                .addAttribute(2, 3, normals)
                .indices(indices));
    }

    public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, float[] tangents, int[] indices) {
        return loadToVAO(newLayout()
                .addAttribute(0, 3, positions)
                .addAttribute(1, 2, textureCoords)
                .addAttribute(2, 3, normals)
                .addAttribute(3, 3, tangents)
                .indices(indices));
    }

    public int createEmptyVbp(float floatCount) {
        int vboID = glGenBuffers();
        vbos.add(vboID);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long)(floatCount * 4), GL_STREAM_DRAW);  // floatCount * 4 需要的字节数
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return vboID;
    }

    public void addInstanceAttribute(int vaoID, int vbo, int attribute, int dataSize, int instanceDataLength, int  offset) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindVertexArray(vaoID);
        glVertexAttribPointer(attribute, dataSize, GL_FLOAT, false, instanceDataLength * 4, offset * 4);
        GL33.glVertexAttribDivisor(attribute, 1);   //每渲染一个实例，数据更改一次
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void updateVbo(int vboID, float[] data, FloatBuffer buffer){
        buffer.clear();
        buffer.put(data);
        buffer.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * 4L, GL_STREAM_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);    // 更新缓冲区数据
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public RawModel loadToVAO(float[] positions) {
        return loadToVAO(newLayout()
                .addAttribute(0, 2, positions)
                .vertexCount(positions.length / 2));
    }

    public RawModel loadToVAO(float[] positions, int dimensions) {
        return loadToVAO(newLayout()
                .addAttribute(0, dimensions, positions)
                .vertexCount(positions.length / dimensions));
    }

    private int createVAO() {
        int vaoID = glGenVertexArrays();
        vaos.add(vaoID);
        glBindVertexArray(vaoID);
        return vaoID;
    }

    private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
        int vboID = glGenBuffers();
        vbos.add(vboID);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        FloatBuffer dataBuffer = storeDataInFloatBuffer(data);
        glBufferData(GL_ARRAY_BUFFER, dataBuffer, GL_STATIC_DRAW);
        glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER,0); // 解绑VBO
    }

    private void unbindVAO() {
        glBindVertexArray(0);
    }

    public void bindIndicesBuffer(int[] indices) {
        int vboID = glGenBuffers();
        vbos.add(vboID);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDataInIntBuffer(indices);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
    }

    public IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public void cleanUp() {
        for (int vao : vaos) {
            glDeleteVertexArrays(vao);
        }
        for (int vbo : vbos) {
            glDeleteBuffers(vbo);
        }

    }
}
