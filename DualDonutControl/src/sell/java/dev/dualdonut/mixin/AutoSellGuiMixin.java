package dev.dualdonut.mixin;

import dev.dualdonut.DualDonutControl;
import hoshin.autosell.AutoSellGui;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AutoSellGui.class, remap = false)
public abstract class AutoSellGuiMixin {
    @Inject(method = "createGui", at = @At("HEAD"), cancellable = true)
    private static void dualdonut$sharedGui(class_437 parent, CallbackInfoReturnable<class_437> cir) {
        cir.setReturnValue(DualDonutControl.createSettings(parent));
    }
}
