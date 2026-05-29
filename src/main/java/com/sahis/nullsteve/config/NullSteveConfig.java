package com.sahis.nullsteve.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NullSteveConfig {

private static final Logger LOGGER = LoggerFactory.getLogger("NullSteveConfig");
private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

public enum Intensity { LOW, MEDIUM, HIGH }

public boolean modEnabled = true;
public Intensity intensity = Intensity.MEDIUM;

public int dayIntervalMinutes = 7;   // 4-10
public int nightIntervalMinutes = 3; // 2-5

public boolean visualGlitches = true;
public boolean uiErrors = true;
public boolean movementLock = true;
public boolean breakLock = true;
public boolean inventoryShuffle = true;
public boolean signs = true;
public boolean crosses = true;
public boolean fakeSystemMessages = true;
public boolean behindAppearances = true;
public boolean worldCreationWarning = true;

public boolean chatResponses = true;

private static NullSteveConfig INSTANCE = new NullSteveConfig();

public static NullSteveConfig get() {
    return INSTANCE;
}

public static void load() {
    Path file = path();
    try {
        if (Files.exists(file)) {
            String json = Files.readString(file);
            NullSteveConfig loaded = GSON.fromJson(json, NullSteveConfig.class);
            if (loaded != null) {
                INSTANCE = loaded;
            }
        } else {
            save();
        }
    } catch (IOException | RuntimeException e) {
        LOGGER.error("Failed to load config, using defaults", e);
        INSTANCE = new NullSteveConfig();
    }
}

public static void save() {
    try {
        Path file = path();
        Files.createDirectories(file.getParent());
        Files.writeString(file, GSON.toJson(INSTANCE));
    } catch (IOException e) {
        LOGGER.error("Failed to save config", e);
    }
}

private static Path path() {
    return FabricLoader.getInstance().getConfigDir().resolve("null_steve.json");
}

public int dayIntervalTicks() {
    return clampMinutes(dayIntervalMinutes, 4, 10) * 60 * 20;
}

public int nightIntervalTicks() {
    return clampMinutes(nightIntervalMinutes, 2, 5) * 60 * 20;
}

public float intensityMultiplier() {
    return switch (intensity) {
        case LOW -> 0.5f;
        case MEDIUM -> 1.0f;
        case HIGH -> 1.6f;
    };
}

private int clampMinutes(int v, int lo, int hi) {
    return Math.max(lo, Math.min(hi, v));
}
}
