package com.sahis.nullsteve.mixin;

import com.sahis.nullsteve.config.NullSteveConfig;
import com.sahis.nullsteve.horror.memory.PlayerMemory;
import com.sahis.nullsteve.horror.memory.WorldMemoryManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
* Records block interactions to player memory. Does NOT cancel breaking; mod is safe-by-design.
*/
@Mixin(Block.class)
public class BlockMixin {

   @Inject(method = "onBroken", at = @At("HEAD"))
   private void null_steve$onBroken(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
       if (!NullSteveConfig.get().modEnabled) return;
       if (world.isClient) return;
       if (!(world instanceof ServerWorld sw)) return;
       // try to credit the closest player
       PlayerEntity nearest = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8.0, false);
       if (!(nearest instanceof ServerPlayerEntity sp)) return;
       PlayerMemory mem = WorldMemoryManager.get(sw).memoryFor(sp.getUuid());
       mem.appendRoute(pos);
   }
}
