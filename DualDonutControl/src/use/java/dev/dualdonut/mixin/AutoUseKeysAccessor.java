package dev.dualdonut.mixin;

import dev.autouse.AutoUseMod;
import net.minecraft.class_304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AutoUseMod.class, remap = false)
public interface AutoUseKeysAccessor {
    @Accessor("toggleKey") static class_304 dualdonut$getToggleKey() { throw new AssertionError(); }
    @Accessor("settingsKey") static class_304 dualdonut$getSettingsKey() { throw new AssertionError(); }
}
