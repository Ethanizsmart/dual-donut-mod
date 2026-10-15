package dev.dualdonut.mixin;

import dev.dualdonut.DualDonutControl;
import hoshin.autosell.Autosell;
import hoshin.autosell.ConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Autosell.class, remap = false)
public abstract class AutoSellMessagesMixin {
    @Unique private int dualdonut$previousState;
    @Inject(method = "processChatMessage", at = @At("HEAD"))
    private void dualdonut$rememberRemoteState(String text, CallbackInfo ci) { dualdonut$previousState = ConfigManager.state; }
    @Inject(method = "processChatMessage", at = @At("RETURN"))
    private void dualdonut$syncRemoteSelection(String text, CallbackInfo ci) {
        if (ConfigManager.state != dualdonut$previousState) DualDonutControl.onRemoteSellChange(ConfigManager.state != 0);
    }
}
