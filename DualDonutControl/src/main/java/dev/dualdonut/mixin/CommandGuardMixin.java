package dev.dualdonut.mixin;

import dev.dualdonut.DualDonutControl;
import java.util.Locale;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_634.class)
public abstract class CommandGuardMixin {
    @Inject(method = "method_45730", at = @At("HEAD"), cancellable = true)
    private void dualdonut$protectSell(String command, CallbackInfo ci) {
        String root = command.strip().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        root = root.substring(root.lastIndexOf(':') + 1);
        if (root.equals("sell") && DualDonutControl.sharedSellPause()) ci.cancel();
    }
}
