package dev.dualdonut.mixin;

import dev.dualdonut.runtime.PlayerProtection;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_419;
import net.minecraft.class_437;
import net.minecraft.class_8667;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_419.class)
public abstract class DisconnectedScreenMixin extends class_437 {
    @Shadow @Final private class_8667 field_44552;
    protected DisconnectedScreenMixin(class_2561 title) { super(title); }
    @Inject(method = "method_25426", at = @At("TAIL"))
    private void dualdonut$reconnectButtons(CallbackInfo ci) {
        if (!PlayerProtection.disconnectedByProtection() || !PlayerProtection.canReconnect() || field_44552 == null) return;
        field_44552.method_52736(class_4185.method_46430(class_2561.method_43470("Reconnect"), button -> PlayerProtection.reconnect(false)).method_46431());
        field_44552.method_52736(class_4185.method_46430(class_2561.method_43470("Tin cậy " + PlayerProtection.lastDetectedPlayer() + " & Reconnect"), button -> PlayerProtection.reconnect(true)).method_46431());
        field_44552.method_48222();
        field_44552.method_48206(this::method_37063);
    }
}
