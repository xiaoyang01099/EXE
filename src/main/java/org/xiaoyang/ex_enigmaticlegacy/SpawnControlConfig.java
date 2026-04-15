package org.xiaoyang.ex_enigmaticlegacy;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class SpawnControlConfig {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("spawncontrol");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("disabled_entities.json");
    private static final Set<String> disabledEntities = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(CONFIG_FILE)) {
                saveToFile();
                LOGGER.info("[SpawnControl] 创建默认配置文件: {}", CONFIG_FILE);
            } else {
                loadFromFile();
                LOGGER.info("[SpawnControl] 加载配置完成，已禁用 {} 个实体", disabledEntities.size());
            }
        } catch (IOException e) {
            LOGGER.error("[SpawnControl] 初始化配置失败", e);
        }
    }

    public static void loadFromFile() {
        if (!Files.exists(CONFIG_FILE)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            disabledEntities.clear();
            if (json != null && json.has("disabled_entities")) {
                JsonArray arr = json.getAsJsonArray("disabled_entities");
                for (JsonElement el : arr) {
                    disabledEntities.add(el.getAsString());
                }
            }
            LOGGER.info("[SpawnControl] 已加载 {} 个禁用实体", disabledEntities.size());
        } catch (Exception e) {
            LOGGER.error("[SpawnControl] 读取配置失败", e);
        }
    }

    public static synchronized void saveToFile() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                JsonObject json = new JsonObject();
                JsonArray arr = new JsonArray();
                for (String entity : disabledEntities) {
                    arr.add(entity);
                }
                json.add("disabled_entities", arr);
                GSON.toJson(json, writer);
                writer.flush();
            }
            LOGGER.info("[SpawnControl] 已保存配置，共 {} 个禁用实体", disabledEntities.size());
        } catch (IOException e) {
            LOGGER.error("[SpawnControl] 保存配置失败", e);
        }
    }

    public static boolean isDisabled(ResourceLocation entityId) {
        return disabledEntities.contains(entityId.toString());
    }

    public static synchronized void disableEntity(ResourceLocation entityId) {
        disabledEntities.add(entityId.toString());
        saveToFile();
    }

    public static synchronized void enableEntity(ResourceLocation entityId) {
        disabledEntities.remove(entityId.toString());
        saveToFile();
    }

    public static synchronized boolean toggle(ResourceLocation entityId) {
        String id = entityId.toString();
        if (disabledEntities.contains(id)) {
            disabledEntities.remove(id);
            saveToFile();
            LOGGER.info("[SpawnControl] 已启用实体: {}", id);
            return false;
        } else {
            disabledEntities.add(id);
            saveToFile();
            LOGGER.info("[SpawnControl] 已禁用实体: {}", id);
            return true;
        }
    }

    public static Set<String> getDisabledEntities() {
        return new HashSet<>(disabledEntities);
    }

    public static synchronized void syncFromServer(Set<String> serverData) {
        disabledEntities.clear();
        disabledEntities.addAll(serverData);
        LOGGER.info("[SpawnControl] 从服务端同步了 {} 个禁用实体", disabledEntities.size());
    }
}