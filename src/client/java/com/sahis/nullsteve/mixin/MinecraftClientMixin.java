package com.sahis.nullsteve.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
* Client tick hook for NullSteve client-side effects.
* Reserved for future visual glitch effects; presently a no-op stub
* so the mixin entry in null_steve.mixins.json resolves cleanly.
*/
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

   @Inject(method = "tick", at = @At("HEAD"))
   private void null_steve$onTick(CallbackInfo ci) {
       // intentionally empty — client-side glitch hooks live here in future iterations.
   }
}
