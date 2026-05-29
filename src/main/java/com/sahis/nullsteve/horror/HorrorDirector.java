package com.sahis.nullsteve.horror;

import com.sahis.nullsteve.config.NullSteveConfig;
import com.sahis.nullsteve.horror.effects.HorrorEffect;
import com.sahis.nullsteve.horror.effects.HorrorEffects;
import com.sahis.nullsteve.horror.memory.PlayerMemory;
import com.sahis.nullsteve.horror.memory.RouteObserver;
import com.sahis.nullsteve.horror.memory.WorldMemoryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

import java.util.*;

/**
* Central scheduler. Each tick, decides if a player is "due" for an event and which
* stage-appropriate effect to play. Strictly respects config flags and safety limits.
*/
public final class HorrorDirector {

   private static final HorrorDirector INSTANCE = new HorrorDirector();

   public static HorrorDirector get() { return INSTANCE; }

   private final Random rng = new Random();
   private final RouteObserver routeObserver = new RouteObserver();
   private final Map<UUID, Long> nextEventTick = new HashMap<>();
   private final Map<UUID, Long> joinTick = new HashMap<>();

   private HorrorDirector() {}

   public void onServerStart(MinecraftServer server) {
       nextEventTick.clear();
       joinTick.clear();
   }

   public void onServerStop() {
       nextEventTick.clear();
       joinTick.clear();
   }

   public void onPlayerJoin(ServerPlayerEntity p) {
       long t = p.getServer() == null ? 0 : p.getServer().getOverworld().getTime();
       joinTick.put(p.getUuid(), t);
       nextEventTick.put(p.getUuid(), t + 20L * 60L * 8L); // 8-minute grace
   }

   public void onPlayerLeave(ServerPlayerEntity p) {
       nextEventTick.remove(p.getUuid());
       joinTick.remove(p.getUuid());
   }

   public void tick(MinecraftServer server) {
       NullSteveConfig cfg = NullSteveConfig.get();
       if (!cfg.modEnabled) return;

       routeObserver.tick(server);

       long now = server.getOverworld().getTime();
       for (ServerWorld world : server.getWorlds()) {
           WorldMemoryManager mgr = WorldMemoryManager.get(world);
           for (ServerPlayerEntity p : world.getPlayers()) {
               Long due = nextEventTick.get(p.getUuid());
               if (due == null) {
                   nextEventTick.put(p.getUuid(), now + scheduleDelay(world, cfg));
                   continue;
               }
               if (now < due) continue;

               PlayerMemory mem = mgr.memoryFor(p.getUuid());
               advanceStageIfNeeded(p, mem, now);

               List<HorrorEffect> pool = HorrorEffects.forStage(mem.getHorrorStage(), cfg);
               if (!pool.isEmpty()) {
                   HorrorEffect effect = pool.get(rng.nextInt(pool.size()));
                   try {
                       effect.play(world, p, mem, rng);
                   } catch (Throwable t) {
                       com.sahis.nullsteve.NullSteveMod.LOGGER.warn("Effect {} failed safely", effect.getClass().getSimpleName(), t);
                   }
               }

               nextEventTick.put(p.getUuid(), now + scheduleDelay(world, cfg));
               mgr.markDirty();
           }
       }
   }

   private long scheduleDelay(ServerWorld world, NullSteveConfig cfg) {
       long timeOfDay = world.getTimeOfDay() % 24000L;
       boolean night = timeOfDay >= 13000L && timeOfDay <= 23000L;
       int base = night ? cfg.nightIntervalTicks() : cfg.dayIntervalTicks();
       float jitter = 0.7f + rng.nextFloat() * 0.6f;
       int adjusted = (int) (base * jitter / Math.max(0.3f, cfg.intensityMultiplier()));
       return MathHelper.clamp(adjusted, 20 * 30, 20 * 60 * 20);
   }

   private void advanceStageIfNeeded(ServerPlayerEntity p, PlayerMemory mem, long now) {
       Long join = joinTick.get(p.getUuid());
       if (join == null) return;
       long minutesPlayed = (now - join) / (20L * 60L);
       int target = 1;
       if (minutesPlayed >= 20) target = 2;
       if (minutesPlayed >= 50) target = 3;
       if (minutesPlayed >= 100) target = 4;
       if (target > mem.getHorrorStage()) mem.setHorrorStage(target);
   }
}
