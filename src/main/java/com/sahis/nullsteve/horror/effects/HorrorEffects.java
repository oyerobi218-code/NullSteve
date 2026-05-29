package com.sahis.nullsteve.horror.effects;

import com.sahis.nullsteve.NullSteveMod;
import com.sahis.nullsteve.config.NullSteveConfig;
import com.sahis.nullsteve.entity.NullSteveEntity;
import com.sahis.nullsteve.entity.ai.NullSteveAI;
import com.sahis.nullsteve.horror.memory.PlayerMemory;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
* Stage-keyed registry of horror effects. Each effect is safe-by-design:
* - never deletes items
* - never corrupts saves
* - never permanently locks input
* - never plays sound files (uses titles/messages only)
*/
public final class HorrorEffects {

   public static List<HorrorEffect> forStage(int stage, NullSteveConfig cfg) {
       List<HorrorEffect> pool = new ArrayList<>();
       // Stage 1 — passive presence
       if (stage >= 1) {
           if (cfg.fakeSystemMessages) pool.add(new FakeSystemMessageEffect());
           if (cfg.visualGlitches) pool.add(new ParticleFlickerEffect());
       }
       // Stage 2 — observed
       if (stage >= 2) {
           if (cfg.behindAppearances) pool.add(new BehindAppearanceEffect());
           if (cfg.uiErrors) pool.add(new FakeOverlayErrorEffect());
       }
       // Stage 3 — interactive
       if (stage >= 3) {
           if (cfg.behindAppearances) pool.add(new SightDespawnAppearanceEffect());
           if (cfg.fakeSystemMessages) pool.add(new WhisperTitleEffect());
       }
       // Stage 4 — confrontation
       if (stage >= 4) {
           if (cfg.behindAppearances) pool.add(new CloseRangeStareEffect());
           if (cfg.fakeSystemMessages) pool.add(new DoNotStayTitleEffect());
       }
       return pool;
   }

   // ─── Stage 1 ─────────────────────────────────────────────────────────

   public static class FakeSystemMessageEffect implements HorrorEffect {
       private static final String[] KEYS = {
           "text.null_steve.msg.world.chunk_mismatch",
           "text.null_steve.msg.light.unknown",
           "text.null_steve.msg.client.out_of_range",
           "text.null_steve.msg.tile_error"
       };
       @Override public void play(ServerWorld world, ServerPlayerEntity p, PlayerMemory m, Random rng) {
           p.sendMessage(Text.translatable(KEYS[rng.nextInt(KEYS.length)]), false);
       }
   }

   public static class ParticleFlickerEffect implements HorrorEffect {
       @Override public void play(ServerWorld world, ServerPlayerEntity p, PlayerMemory m, Random rng) {
           BlockPos pos = NullSteveAI.pickSpawnNearPlayer(world, p, 6, 12);
           if (pos == null) return;
           Vec3d center = Vec3d.ofCenter(pos);
           world.spawnParticles(p, ParticleTypes.SMOKE,
               false, center.x, center.y + 1.0, center.z, 4, 0.2, 0.4, 0.2, 0.01);
       }
   }

   // ─── Stage 2 ─────────────────────────────────────────────────────────

   public static class BehindAppearanceEffect implements HorrorEffect {
       @Override public void play(ServerWorld world, ServerPlayerEntity p, PlayerMemory m, Random rng) {
           BlockPos pos = NullSteveAI.pickSpawnNearPlayer(world, p, 8, 20);
           if (pos == null) return;
           NullSteveEntity e = NullSteveMod.NULL_STEVE.create(world);
           if (e == null) return;
           e.refreshPositionAndAngles(pos, (float)(rng.nextDouble() * 360.0), 0f);
           e.setDespawnTicks(20 * 5);
           e.setDespawnOnSight(true);
           world.spawnEntity(e);
       }
   }

   public static class FakeOverlayErrorEffect implements HorrorEffect {
       @Override public void play(ServerWorld world, ServerPlayerEntity p, PlayerMemory m, Random rng) {
           p.networkHandler.sendPacket(new OverlayMessageS2CPacket(
               Text.translatable("text.null_steve.msg.signal_lost")
           ));
       }
   }

   // ─── Stage 3 ─────────────────────────────────────────────────────────

   public static class SightDespawnAppearanceEffect implements HorrorEffect {
       @Override public void play(ServerWorld world, ServerPlayerEntity p, PlayerMemory m, Random rng) {
           BlockPos pos = NullSteveAI.pickSpawnNearPlayer(world, p, 5, 10);
           if (pos == null) return;
           NullSteveEntity e = NullSteveMod.NULL_STEVE.create(world);
           if (e == null) return;
           e.refreshPositionAndAngles(pos, (float)(rng.nextDouble() * 360.0), 0f);
           e.setDespawnTicks(20 * 10);
           e.setDespawnOnSight(true);
           world.spawnEntity(e);
       }
   }

   public static class WhisperTitleEffect implements HorrorEffect {
       @Override public void play(ServerWorld world, ServerPlayerEntity p, PlayerMemory m, Random rng) {
           p.networkHandler.sendPacket(new SubtitleS2CPacket(
               Text.translatable("text.null_steve.msg.player_observed")
           ));
           p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("")));
       }
   }

   // ─── Stage 4 ─────────────────────────────────────────────────────────

   public static class CloseRangeStareEffect implements HorrorEffect {
       @Override public void play(ServerWorld world, ServerPlayerEntity p, PlayerMemory m, Random rng) {
           BlockPos pos = NullSteveAI.pickSpawnNearPlayer(world, p, 3, 6);
           if (pos == null) return;
           NullSteveEntity e = NullSteveMod.NULL_STEVE.create(world);
           if (e == null) return;
           // face the entity at the player
           double dx = p.getX() - pos.getX();
           double dz = p.getZ() - pos.getZ();
           float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90);
           e.refreshPositionAndAngles(pos, yaw, 0f);
           e.setDespawnTicks(20 * 6);
           e.setDespawnOnSight(true);
           world.spawnEntity(e);
       }
   }

   public static class DoNotStayTitleEffect implements HorrorEffect {
       @Override public void play(ServerWorld world, ServerPlayerEntity p, PlayerMemory m, Random rng) {
           p.networkHandler.sendPacket(new TitleS2CPacket(
               Text.translatable("text.null_steve.msg.do_not_stay")
           ));
       }
   }
}
