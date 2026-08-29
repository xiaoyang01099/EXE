package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.system;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.xiaoyang.ex_enigmaticlegacy.Exe;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

public abstract class ShaderProgram {
    protected int programID;
    private int vertexShaderID;
    private int fragmentShaderID;

    private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public ShaderProgram(String vertexFile, String fragmentFile) {
        vertexShaderID = loadShader(vertexFile, GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL_FRAGMENT_SHADER);
        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);    //绑定着色器
        bindAttributes();  //绑定传给着色器的值
        glLinkProgram(programID);       //链接着色器
        glValidateProgram(programID);   //验证着色器
        getAllUniformLocations();    //获取所有uniform变量的位置
    }

    protected int getUniformLocation(String uniformName) {
        return glGetUniformLocation(programID, uniformName);
    }
    protected abstract void getAllUniformLocations();

    public void bindAttribute(int attribute, String name) {
        glBindAttribLocation(programID, attribute, name);
    }

    protected void bindAttributes() {

    }

    public void start() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void cleanUp() {
        stop();
        glDetachShader(programID, vertexShaderID);
        glDetachShader(programID, fragmentShaderID);
        glDeleteShader(vertexShaderID);
        glDeleteShader(fragmentShaderID);
        glDeleteProgram(programID);
    }

    protected static int loadShader(String file, int type) {
        String shaderSource = new String(loadFromResources(file), StandardCharsets.UTF_8);

        int shaderID = GL30.glCreateShader(type);
        GL30.glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shaderID);
            System.err.println("[Compute Shader 编译错误]\n" + log);
            System.err.println("Could not compile shader!\n" + log);
            System.exit(-1);
        }
        return shaderID;
    }

    public static byte[] loadFromResources(String name) {
        try {
            InputStream is = Exe.class.getClassLoader().getResourceAsStream("assets/ex_enigmaticlegacy/" + name);
            return is.readAllBytes();
        } catch (Exception e) {
            System.err.println("无法加载 Shader 文件!");
            System.err.println("Could not read file!");
            throw new RuntimeException(e);
        }
    }


    public void loadFloat(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    public void loadVector(int location, Vector4f vector) {
        GL20.glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
    }

    public void loadVector(int location, Vector3f vector) {
        GL20.glUniform3f(location, vector.x, vector.y, vector.z);
    }

    public void loadVector(int location, Vector2f vector) {
        GL20.glUniform2f(location, vector.x, vector.y);
    }

    public void loadInt(int location, int value) {
        GL20.glUniform1i(location, value);
    }

    public void loadBoolean(int location, boolean value) {
        float toLoad = 0;
        if (value) {
            toLoad = 1;
        }
        GL20.glUniform1f(location, toLoad);
    }

    public void loadMatrix(int location, Matrix4f matrix) {
        matrix.get(matrixBuffer);
        GL20.glUniformMatrix4fv(location, false, matrixBuffer); //false 是否转置矩阵
    }
}
