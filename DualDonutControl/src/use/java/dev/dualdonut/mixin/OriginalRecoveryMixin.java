package dev.dualdonut.mixin;

import dev.autouse.DeathRecoveryRuntime;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The original class stays in AutoUse and works normally without this add-on. */
@Mixin(value = DeathRecoveryRuntime.class, remap = false)
public abstract class OriginalRecoveryMixin {
    @Shadow static void reset() { throw new AssertionError(); }
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void dualdonut$useSharedRecovery(class_310 client, long now, CallbackInfo ci) {
        reset();
        ci.cancel();
    }
}
