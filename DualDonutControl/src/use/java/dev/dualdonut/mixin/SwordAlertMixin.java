package dev.dualdonut.mixin;

import dev.dualdonut.runtime.Notifications;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.autouse.AutoSellWebhookBridge", remap = false)
public abstract class SwordAlertMixin {
    @Inject(method = "insufficientFunds", at = @At("HEAD"), cancellable = true)
    private static void dualdonut$swordFunds(String playerName, long price, CallbackInfo ci) {
        Notifications.send("Không đủ tiền mua kiếm", "Người chơi: " + playerName + "\nGiá kiếm: $" + price);
        ci.cancel();
    }
}
