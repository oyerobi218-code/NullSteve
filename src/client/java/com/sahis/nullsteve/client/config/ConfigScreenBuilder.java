package com.sahis.nullsteve.client.config;

import com.sahis.nullsteve.config.NullSteveConfig;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class ConfigScreenBuilder {

public static Screen build(Screen parent) {
    NullSteveConfig cfg = NullSteveConfig.get();

    return YetAnotherConfigLib.createBuilder()
        .title(Text.translatable("text.null_steve.config.title"))
        .category(ConfigCategory.createBuilder()
            .name(Text.translatable("text.null_steve.config.category.main"))
            .option(Option.<Boolean>createBuilder()
                .name(Text.translatable("text.null_steve.config.enabled"))
                .binding(true, () -> cfg.modEnabled, v -> cfg.modEnabled = v)
                .controller(BooleanControllerBuilder::create)
                .build())
            .option(Option.<NullSteveConfig.Intensity>createBuilder()
                .name(Text.translatable("text.null_steve.config.intensity"))
                .binding(NullSteveConfig.Intensity.MEDIUM, () -> cfg.intensity, v -> cfg.intensity = v)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(NullSteveConfig.Intensity.class))
                .build())
            .option(Option.<Integer>createBuilder()
                .name(Text.translatable("text.null_steve.config.day_interval"))
                .binding(7, () -> cfg.dayIntervalMinutes, v -> cfg.dayIntervalMinutes = v)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(4, 10).step(1))
                .build())
            .option(Option.<Integer>createBuilder()
                .name(Text.translatable("text.null_steve.config.night_interval"))
                .binding(3, () -> cfg.nightIntervalMinutes, v -> cfg.nightIntervalMinutes = v)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(2, 5).step(1))
                .build())
            .build())
        .category(ConfigCategory.createBuilder()
            .name(Text.translatable("text.null_steve.config.category.toggles"))
            .option(boolOpt("text.null_steve.config.visual_glitches", () -> cfg.visualGlitches, v -> cfg.visualGlitches = v))
            .option(boolOpt("text.null_steve.config.ui_errors", () -> cfg.uiErrors, v -> cfg.uiErrors = v))
            .option(boolOpt("text.null_steve.config.movement_lock", () -> cfg.movementLock, v -> cfg.movementLock = v))
            .option(boolOpt("text.null_steve.config.break_lock", () -> cfg.breakLock, v -> cfg.breakLock = v))
            .option(boolOpt("text.null_steve.config.inventory_shuffle", () -> cfg.inventoryShuffle, v -> cfg.inventoryShuffle = v))
            .option(boolOpt("text.null_steve.config.signs", () -> cfg.signs, v -> cfg.signs = v))
            .option(boolOpt("text.null_steve.config.crosses", () -> cfg.crosses, v -> cfg.crosses = v))
            .option(boolOpt("text.null_steve.config.fake_system_messages", () -> cfg.fakeSystemMessages, v -> cfg.fakeSystemMessages = v))
            .option(boolOpt("text.null_steve.config.behind_appearances", () -> cfg.behindAppearances, v -> cfg.behindAppearances = v))
            .option(boolOpt("text.null_steve.config.world_creation_warning", () -> cfg.worldCreationWarning, v -> cfg.worldCreationWarning = v))
            .option(boolOpt("text.null_steve.config.chat_responses", () -> cfg.chatResponses, v -> cfg.chatResponses = v))
            .build())
        .save(NullSteveConfig::save)
        .build()
        .generateScreen(parent);
}

private static Option<Boolean> boolOpt(String key, java.util.function.Supplier<Boolean> getter, java.util.function.Consumer<Boolean> setter) {
    return Option.<Boolean>createBuilder()
        .name(Text.translatable(key))
        .binding(true, getter::get, setter::accept)
        .controller(BooleanControllerBuilder::create)
        .build();
}
}
