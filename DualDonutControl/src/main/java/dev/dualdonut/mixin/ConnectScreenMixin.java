package dev.dualdonut.mixin;

import dev.dualdonut.runtime.PlayerProtection;
import net.minecraft.class_310;
import net.minecraft.class_412;
import net.minecraft.class_437;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_9112;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_412.class)
public abstract class ConnectScreenMixin {
    @Inject(method = "method_36877", at = @At("HEAD"))
    private static void dualdonut$remember(class_437 parent, class_310 client, class_639 address, class_642 info,
                                          boolean quickPlay, class_9112 cookie, CallbackInfo ci) { PlayerProtection.rememberServer(address, info); }
}
