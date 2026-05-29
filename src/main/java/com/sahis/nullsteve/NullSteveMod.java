package com.sahis.nullsteve;

import com.sahis.nullsteve.config.NullSteveConfig;
import com.sahis.nullsteve.entity.NullSteveEntity;
import com.sahis.nullsteve.event.ServerLifecycleEvents;
import com.sahis.nullsteve.horror.HorrorDirector;
import com.sahis.nullsteve.horror.memory.WorldMemoryManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistration;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.dimensions.EntityDimensions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NullSteveMod implements ModInitializer {

public static final String MOD_ID = "null_steve";
public static final Logger LOGGER = LoggerFactory.getLogger("NullSteve");

public static EntityType<NullSteveEntity> NULL_STEVE;

@Override
public void onInitialize() {
    NullSteveConfig.load();

    NULL_STEVE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "null_steve"),
        EntityType.Builder.create(NullSteveEntity::new, SpawnGroup.MISC)
            .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
            .maxTrackingRange(80)
            .trackingTickInterval(3)
            .disableSummon()
            .build()
    );

    FabricDefaultAttributeRegistration.register(NULL_STEVE, NullSteveEntity.createAttributes());

    ServerLifecycleEvents.register();
    ServerTickEvents.END_SERVER_TICK.register(server -> HorrorDirector.get().tick(server));

    LOGGER.info("NullSteve initialized. mod_id={}", MOD_ID);
}

public static Identifier id(String path) {
    return Identifier.of(MOD_ID, path);
}
}
