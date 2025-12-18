package com.hexsbm.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("hexsbm.json");

    private static HexSBMConfig savedConfig = new HexSBMConfig();
    private static boolean loaded = false;

    public static HexSBMConfig getSavedConfig() {
        if (!loaded) {
            load();
        }
        return savedConfig.copy(); // ✅ теперь работает
    }

    public static void saveConfig(HexSBMConfig config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(config);
            Files.writeString(CONFIG_PATH, json);
            savedConfig = config.copy(); // ✅ обновляем кэш
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                HexSBMConfig loadedConfig = new Gson().fromJson(json, HexSBMConfig.class);
                if (loadedConfig != null) {
                    savedConfig = loadedConfig;
                } else {
                    savedConfig = new HexSBMConfig();
                }
            } else {
                saveConfig(new HexSBMConfig());
            }
            loaded = true;
        } catch (Exception e) {
            e.printStackTrace();
            savedConfig = new HexSBMConfig();
            loaded = true;
        }
    }
}