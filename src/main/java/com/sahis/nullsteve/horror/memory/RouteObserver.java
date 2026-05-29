package com.sahis.nullsteve.horror.memory;

import net.minecraft.block.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
* Samples each player's position once every SAMPLE_INTERVAL ticks. Adds nearby
* doors/windows/chests/bed to memory cheaply by scanning a tiny 5x5x3 box only on sample ticks.
*/
public final class RouteObserver {

   private static final int SAMPLE_INTERVAL = 20 * 3; // 3 seconds
   private long lastSampleTick = 0;

   public void tick(MinecraftServer server) {
       long tick = server.getOverworld().getTime();
       if (tick - lastSampleTick < SAMPLE_INTERVAL) return;
       lastSampleTick = tick;

       for (ServerWorld world : server.getWorlds()) {
           WorldMemoryManager mgr = WorldMemoryManager.get(world);
           List<ServerPlayerEntity> players = world.getPlayers();
           for (ServerPlayerEntity p : players) {
               PlayerMemory mem = mgr.memoryFor(p.getUuid());
               mem.appendRoute(p.getBlockPos());
               if (p.isSleeping()) {
                   p.getSleepingPosition().ifPresent(mem::setBedPos);
               }
               scanNearby(world, p, mem);
           }
       }
   }

   private void scanNearby(ServerWorld world, ServerPlayerEntity p, PlayerMemory mem) {
       BlockPos center = p.getBlockPos();
       for (int dx = -2; dx <= 2; dx++) {
           for (int dz = -2; dz <= 2; dz++) {
               for (int dy = -1; dy <= 1; dy++) {
                   BlockPos pos = center.add(dx, dy, dz);
                   BlockState state = world.getBlockState(pos);
                   Block block = state.getBlock();
                   if (block instanceof DoorBlock) {
                       mem.rememberDoor(pos);
                   } else if (block instanceof GlassBlock || block instanceof PaneBlock) {
                       mem.rememberWindow(pos);
                   } else if (block instanceof BedBlock) {
                       mem.setBedPos(pos);
                   } else if (block instanceof ChestBlock) {
                       mem.rememberChest(pos);
                   }
               }
           }
       }
   }
}
