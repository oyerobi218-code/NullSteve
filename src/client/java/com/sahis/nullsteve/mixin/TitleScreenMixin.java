package com.sahis.nullsteve.mixin;

import com.sahis.nullsteve.config.NullSteveConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
* Adds a one-time warning when entering the title screen, if enabled in config.
* The warning informs the player that NullSteve is installed.
*/
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

   private static boolean null_steve$warningShown = false;

   protected TitleScreenMixin(Text title) {
       super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void null_steve$init(CallbackInfo ci) {
       if (null_steve$warningShown) return;
       if (!NullSteveConfig.get().modEnabled) return;
       if (!NullSteveConfig.get().worldCreationWarning) return;
       null_steve$warningShown = true;

       MinecraftClient client = MinecraftClient.getInstance();
       if (client == null) return;
       Screen current = this;
       client.setScreen(new net.minecraft.client.gui.screen.NoticeScreen(
           () -> client.setScreen(current),
           Text.translatable("text.null_steve.warning.title"),
           Text.translatable("text.null_steve.warning.body"),
           Text.translatable("text.null_steve.warning.ok"),
           true
       ));
   }
}
