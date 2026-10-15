package dev.autouse.mixin;

import dev.autouse.api.AutoUseApi;
import java.util.Locale;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_634.class)
public abstract class SellCommandGuardMixin {
    @Inject(method = "method_45730", at = @At("HEAD"), cancellable = true)
    private void autousemod$pauseSellCommand(String command, CallbackInfo ci) {
        String root = command.strip().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        int colon = root.lastIndexOf(':');
        if (colon >= 0) root = root.substring(colon + 1);
        if (root.equals("sell") && AutoUseApi.isAutoSellPaused()) ci.cancel();
    }
}
