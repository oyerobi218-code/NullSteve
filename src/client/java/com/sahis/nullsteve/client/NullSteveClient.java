package com.sahis.nullsteve.client;

import com.sahis.nullsteve.NullSteveMod;
import com.sahis.nullsteve.client.render.NullSteveRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class NullSteveClient implements ClientModInitializer {
@Override
public void onInitializeClient() {
    EntityRendererRegistry.register(NullSteveMod.NULL_STEVE, NullSteveRenderer::new);
}
}
