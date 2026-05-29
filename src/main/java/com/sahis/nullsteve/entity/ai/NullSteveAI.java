package com.sahis.nullsteve.entity.ai;

import com.sahis.nullsteve.entity.NullSteveEntity;
import com.sahis.nullsteve.horror.memory.PlayerMemory;
import com.sahis.nullsteve.horror.memory.WorldMemoryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Chooses spawn positions for NullSteve given a player.
 * Never picks a location inside solid blocks. Never spawns where the player is currently looking.
 */
public final class NullSteveAI {

    private static final Random RNG = new Random();

    public static class SpawnCandidate {
        public final BlockPos pos;
        public final float score;
        public SpawnCandidate(BlockPos pos, float score) {
            this.pos = pos;
            this.score = score;
        }
    }

    /**
     * Pick the most "frightening" position near a player. Returns null if nothing safe is found.
     */
    public static BlockPos pickSpawnNearPlayer(ServerWorld world, ServerPlayerEntity player, int minDist, int maxDist) {
        List<SpawnCandidate> candidates = new ArrayList<>();
        Vec3d look = player.getRotationVec(1.0f);

        // candidate 1: behind the player
        candidates.add(makeCandidate(world, player, look.multiply(-1).normalize(), minDist, maxDist, 1.0f));
        // candidate 2: hard left
        candidates.add(makeCandidate(world, player, perpendicular(look, 1).normalize(), minDist, maxDist, 0.6f));
        // candidate 3: hard right
        candidates.add(makeCandidate(world, player, perpendicular(look, -1).normalize(), minDist, maxDist, 0.6f));

        // candidate 4: near remembered base / bed
        PlayerMemory mem = WorldMemoryManager.get(world).memoryFor(player.getUuid());
        if (mem.getBedPos() != null) {
            candidates.add(new SpawnCandidate(safeSurface(world, mem.getBedPos().add(rngOffset(2), 0, rngOffset(2))), 0.9f));
        }
        if (!mem.getDoorPositions().isEmpty()) {
            BlockPos doorPos = mem.randomDoor(RNG);
            if (doorPos != null) {
                candidates.add(new SpawnCandidate(safeSurface(world, doorPos.add(rngOffset(1), 0, rngOffset(1))), 1.2f));
            }
        }

        // filter
        candidates.removeIf(c -> c == null || c.pos == null);
        candidates.removeIf(c -> !isSafeSpawn(world, c.pos));
        candidates.removeIf(c -> playerIsLookingAt(player, c.pos));

        if (candidates.isEmpty()) return null;

        // weighted random by score
        float total = 0;
        for (SpawnCandidate c : candidates) total += c.score;
        float pick = RNG.nextFloat() * total;
        float acc = 0;
        for (SpawnCandidate c : candidates) {
            acc += c.score;
            if (pick <= acc) return c.pos;
        }
        return candidates.get(0).pos;
    }

    private static SpawnCandidate makeCandidate(ServerWorld world, ServerPlayerEntity player, Vec3d dir, int minDist, int maxDist, float score) {
        int dist = minDist + RNG.nextInt(Math.max(1, maxDist - minDist));
        BlockPos around = BlockPos.ofFloored(
            player.getX() + dir.x * dist,
            player.getY(),
            player.getZ() + dir.z * dist
        );
        BlockPos surface = safeSurface(world, around);
        if (surface == null) return null;
        return new SpawnCandidate(surface, score);
    }

    private static Vec3d perpendicular(Vec3d v, int sign) {
        return new Vec3d(-v.z * sign, 0, v.x * sign);
    }

    private static int rngOffset(int range) {
        return RNG.nextInt(range * 2 + 1) - range;
    }

    public static BlockPos safeSurface(ServerWorld world, BlockPos around) {
        if (around == null) return null;
        int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, around.getX(), around.getZ());
        BlockPos surface = new BlockPos(around.getX(), top, around.getZ());
        // bring above ground a bit
        if (!world.getBlockState(surface).isAir()) {
            surface = surface.up();
        }
        return surface;
    }

    public static boolean isSafeSpawn(ServerWorld world, BlockPos pos) {
        if (pos == null) return false;
        if (pos.getY() < world.getBottomY() + 1 || pos.getY() > world.getTopY() - 2) return false;
        return world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir();
    }

    public static boolean playerIsLookingAt(ServerPlayerEntity player, BlockPos pos) {
        Vec3d look = player.getRotationVec(1.0f).normalize();
        Vec3d toPos = Vec3d.ofCenter(pos).subtract(player.getEyePos()).normalize();
        return look.dotProduct(toPos) > 0.65;
    }
}
