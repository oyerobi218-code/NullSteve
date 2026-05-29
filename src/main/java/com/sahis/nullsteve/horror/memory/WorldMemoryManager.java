package com.sahis.nullsteve.horror.memory;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent NullSteve state attached to a ServerWorld (overworld).
 * Stored in saves/{world}/data/null_steve.dat
 */
public final class WorldMemoryManager extends PersistentState {

    public static final String KEY = "null_steve";

    private final Map<UUID, PlayerMemory> byPlayer = new HashMap<>();

    public PlayerMemory memoryFor(UUID playerId) {
        return byPlayer.computeIfAbsent(playerId, k -> {
            markDirty();
            return new PlayerMemory();
        });
    }

    public void resetAll() {
        byPlayer.clear();
        markDirty();
    }

    public void resetFor(UUID playerId) {
        byPlayer.remove(playerId);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Map.Entry<UUID, PlayerMemory> e : byPlayer.entrySet()) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("id", e.getKey());
            entry.put("memory", e.getValue().toNbt());
            list.add(entry);
        }
        nbt.put("players", list);
        return nbt;
    }

    private static WorldMemoryManager readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        WorldMemoryManager mgr = new WorldMemoryManager();
        if (nbt.contains("players")) {
            NbtList list = nbt.getList("players", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound entry = list.getCompound(i);
                UUID id = entry.getUuid("id");
                PlayerMemory m = PlayerMemory.fromNbt(entry.getCompound("memory"));
                mgr.byPlayer.put(id, m);
            }
        }
        return mgr;
    }

    public static PersistentState.Type<WorldMemoryManager> TYPE = new PersistentState.Type<>(
        WorldMemoryManager::new,
        WorldMemoryManager::readFromNbt,
        null
    );

    /**
     * Always operate on the overworld so memory is per-save and not per-dimension.
     */
    public static WorldMemoryManager get(ServerWorld world) {
        ServerWorld overworld = world.getServer().getOverworld();
        PersistentStateManager mgr = overworld.getPersistentStateManager();
        return mgr.getOrCreate(TYPE, KEY);
    }
}
