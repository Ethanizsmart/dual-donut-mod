package dev.dualdonut.smoke;

import dev.dualdonut.SideIntegration;
import java.util.Arrays;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

/** Stops before the game and original client entrypoints; no authentication or webhook calls. */
public final class MixinSmoke implements PreLaunchEntrypoint {
    @Override public void onPreLaunch() {
        try {
            var loader = getClass().getClassLoader();
            String[][] common = {
                {"net.minecraft.class_412", "dualdonut$remember"},
                {"net.minecraft.class_419", "dualdonut$reconnectButtons"},
                {"net.minecraft.class_634", "dualdonut$protectSell"}
            };
            for (var target : common) verify(loader, target);
            for (var target : SmokeSide.targets()) verify(loader, target);
            Class.forName("dev.dualdonut.DualDonutControl", false, loader).getDeclaredMethods();
            SmokeSide.verifyAccessors();
            for (String absent : new String[] { SmokeSide.absentModClass(), "net.autohomeafk.core.AutoHomeEngine" }) {
                try { Class.forName(absent, false, loader); throw new AssertionError("Unexpected cross-profile dependency: " + absent); }
                catch (ClassNotFoundException expected) { }
            }
            System.out.println("DUAL_DONUT_" + SideIntegration.NAME.toUpperCase() + "_SMOKE_PASS: "
                + (common.length + SmokeSide.targets().length) + " targets transformed; other profile and AutoHome absent.");
            System.exit(0);
        } catch (Throwable ex) { ex.printStackTrace(); System.exit(1); }
    }
    private static void verify(ClassLoader loader, String[] target) throws Exception {
        var type = Class.forName(target[0], false, loader);
        if (Arrays.stream(type.getDeclaredMethods()).noneMatch(method -> method.getName().contains(target[1])))
            throw new AssertionError("Missing transformed hook: " + Arrays.toString(target));
    }
}
