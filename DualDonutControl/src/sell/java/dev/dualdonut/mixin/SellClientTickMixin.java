package dev.dualdonut.mixin;

import dev.dualdonut.DualDonutControl;
import dev.dualdonut.SideIntegration;
import dev.dualdonut.bounty.BountyRuntime;
import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_310.class)
public abstract class SellClientTickMixin {
    @Inject(method = "method_1574", at = @At("HEAD"))
    private void dualdonut$beforeClientTick(CallbackInfo ci) {
        var client = (class_310) (Object) this;
        DualDonutControl.beforeTick(client);
        SideIntegration.tickRelease(client);
        BountyRuntime.tick(client);
    }
}
