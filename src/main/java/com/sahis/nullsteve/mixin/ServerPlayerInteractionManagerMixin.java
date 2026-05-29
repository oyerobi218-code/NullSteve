package com.sahis.nullsteve.mixin;

import com.sahis.nullsteve.config.NullSteveConfig;
import com.sahis.nullsteve.horror.memory.PlayerMemory;
import com.sahis.nullsteve.horror.memory.WorldMemoryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
* Records interaction positions to player memory.
* Read-only: never blocks or modifies interactions.
*/
@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

   @Shadow protected ServerPlayerEntity player;
   @Shadow protected ServerWorld world;

   @Inject(method = "tryBreakBlock", at = @At("HEAD"))
   private void null_steve$tryBreak(BlockPos pos, CallbackInfo ci) {
       if (!NullSteveConfig.get().modEnabled) return;
       if (this.player == null || this.world == null) return;
       PlayerMemory mem = WorldMemoryManager.get(this.world).memoryFor(this.player.getUuid());
       mem.appendRoute(pos);
   }
}
