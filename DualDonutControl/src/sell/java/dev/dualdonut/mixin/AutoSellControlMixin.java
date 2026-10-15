package dev.dualdonut.mixin;

import dev.dualdonut.DualDonutControl;
import hoshin.autosell.AutoSellLogic;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AutoSellLogic.class, remap = false)
public abstract class AutoSellControlMixin {
    @Inject(method = "onTick", at = @At("HEAD"), cancellable = true)
    private static void dualdonut$guardSale(class_310 client, CallbackInfo ci) {
        DualDonutControl.beforeSellTick(client);
        if (!DualDonutControl.canRunSell(client)) ci.cancel();
    }
    @Inject(method = "onTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_634;method_45730(Ljava/lang/String;)V"))
    private static void dualdonut$claimSale(class_310 client, CallbackInfo ci) { DualDonutControl.saleCommandSent(); }
    @Inject(method = "onTick", at = @At("RETURN"))
    private static void dualdonut$publishSale(class_310 client, CallbackInfo ci) { DualDonutControl.afterSellTick(client); }
}
