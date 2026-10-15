package dev.dualdonut.mixin;

import dev.autouse.AutoUseMod;
import dev.autouse.HoldUseRuntime;
import dev.dualdonut.DualDonutControl;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AutoUseMod.class, remap = false)
public abstract class AutoUseControlMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void dualdonut$beforeUse(CallbackInfo ci) {
        var client = class_310.method_1551();
        DualDonutControl.beforeTick(client);
        if (!DualDonutControl.canRunUse(client)) {
            HoldUseRuntime.release(client);
            ci.cancel();
        }
    }
}
