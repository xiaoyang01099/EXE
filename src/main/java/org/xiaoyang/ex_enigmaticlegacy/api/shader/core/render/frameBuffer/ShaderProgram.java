package org.xiaoyang.ex_enigmaticlegacy.api.shader.core.render.frameBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL20.*;

public abstract class ShaderProgram {
    private int programID;
    private int vertexShaderID;
    private int fragmentShaderID;

    public ShaderProgram(ResourceLocation vertexFile, ResourceLocation fragmentFile) {
        vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
        fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
        programID = glCreateProgram();
        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        bindAttributes();
        glLinkProgram(programID);
        glValidateProgram(programID);
        getAllUniformLocations();
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

    private static int loadShader(ResourceLocation resource, int type) {
        String content = "";
        if (resource != null) {
            try {
                InputStream stream = Minecraft.getInstance().getResourceManager().getResourceOrThrow(resource).open();
                content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }catch (IOException e) {
                System.err.println("EXE Could not read file!");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        int shaderID = GL30.glCreateShader(type);
        GL30.glShaderSource(shaderID, content);
        glCompileShader(shaderID);
        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.out.println(glGetShaderInfoLog(shaderID, 500));
            System.err.println("Could not compile shader!");
            System.exit(-1);
        }
        return shaderID;
    }

    public void loadFloat(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    public void loadVector(int location, Vector4f vector) {
        GL20.glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
    }

    public void loadVector(int location, Vector2f vector) {
        GL20.glUniform2f(location, vector.x, vector.y);
    }

    public void loadInt(int location, int value) {
        GL20.glUniform1i(location, value);
    }

}
