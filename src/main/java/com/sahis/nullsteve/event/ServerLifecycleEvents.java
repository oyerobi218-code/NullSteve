package com.sahis.nullsteve.event;

import com.sahis.nullsteve.config.NullSteveConfig;
import com.sahis.nullsteve.horror.HorrorDirector;
import com.sahis.nullsteve.horror.memory.WorldMemoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class ServerLifecycleEvents {

public static void register() {
    net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register((ServerStarted) server -> {
        NullSteveConfig.load();
        HorrorDirector.get().onServerStart(server);
    });
    net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register((ServerStopping) server -> {
        HorrorDirector.get().onServerStop();
    });
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
        ServerPlayerEntity p = handler.getPlayer();
        HorrorDirector.get().onPlayerJoin(p);
    });
    ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
        HorrorDirector.get().onPlayerLeave(handler.getPlayer());
    });
}
}
