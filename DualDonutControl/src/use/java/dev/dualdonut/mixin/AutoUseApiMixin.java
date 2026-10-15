package dev.dualdonut.mixin;

import dev.autouse.api.AutoUseApi;
import dev.dualdonut.DualDonutControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AutoUseApi.class, remap = false)
public abstract class AutoUseApiMixin {
    @Inject(method = "isAutoSellPaused", at = @At("HEAD"), cancellable = true)
    private static void dualdonut$sharedPause(CallbackInfoReturnable<Boolean> cir) {
        if (DualDonutControl.sharedSellPause()) cir.setReturnValue(true);
    }
    @Inject(method = "needsInventorySpace", at = @At("HEAD"), cancellable = true)
    private static void dualdonut$noSellingDuringDeath(CallbackInfoReturnable<Boolean> cir) {
        if (DualDonutControl.sharedSellPause()) cir.setReturnValue(false);
    }
    @Inject(method = "pauseReason", at = @At("HEAD"), cancellable = true)
    private static void dualdonut$pauseReason(CallbackInfoReturnable<String> cir) {
        String reason = DualDonutControl.sharedPauseReason();
        if (!reason.isEmpty()) cir.setReturnValue(reason);
    }
}
