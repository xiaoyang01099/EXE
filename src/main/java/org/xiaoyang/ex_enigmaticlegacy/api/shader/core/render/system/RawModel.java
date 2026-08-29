package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

public class RawModel {
    int vaoID;
    int vertexCount;

    public RawModel(int vaoID, int vertexCount) {
        this.vaoID = vaoID;
        this.vertexCount = vertexCount;
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
