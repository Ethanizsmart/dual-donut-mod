package dev.autouse.mixin;

import dev.autouse.AutoUseMod;
import dev.autouse.HoldUseRuntime;
import net.minecraft.class_1268;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_310.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "method_1574", at = @At("HEAD"))
    private void autousemod$tick(CallbackInfo ci) { AutoUseMod.tick(); }

    @Inject(method = "method_1583", at = @At("HEAD"), cancellable = true)
    private void autousemod$guardAutomaticUse(CallbackInfo ci) {
        if (HoldUseRuntime.ownsUseKey() && !HoldUseRuntime.isCurrentUseSafe((class_310) (Object) this)) {
            HoldUseRuntime.release((class_310) (Object) this);
            ci.cancel();
        }
    }

    @Redirect(method = "method_1583", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/class_1268;values()[Lnet/minecraft/class_1268;"))
    private class_1268[] autousemod$onlyAutomaticMainHand() {
        return HoldUseRuntime.ownsUseKey() ? new class_1268[]{class_1268.field_5808} : class_1268.values();
    }
}
