package com.sahis.nullsteve.horror.effects;

import com.sahis.nullsteve.horror.memory.PlayerMemory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

/**
 * Base interface for all NullSteve horror effects.
 * Each effect must be idempotent and reversible — no permanent state changes.
 */
public interface HorrorEffect {
    void play(ServerWorld world, ServerPlayerEntity player, PlayerMemory memory, Random rng);
}
