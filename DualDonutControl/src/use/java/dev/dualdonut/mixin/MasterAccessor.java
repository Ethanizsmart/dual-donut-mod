package dev.dualdonut.mixin;

import dev.autouse.MasterAutomation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MasterAutomation.class, remap = false)
public interface MasterAccessor {
    @Accessor("enabled") static void dualdonut$setEnabled(boolean value) { throw new AssertionError(); }
}
