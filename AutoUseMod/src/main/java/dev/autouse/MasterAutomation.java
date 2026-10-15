package dev.autouse;

public final class MasterAutomation {
    private static boolean enabled;
    private MasterAutomation() {}
    public static boolean isEnabled() { return enabled; }
    public static void toggle() {
        enabled = !enabled;
        if (!enabled) AutoUseMod.resetRuntime();
        AutoUseMod.message("AutoUseMod: " + (enabled ? "ON" : "OFF"));
    }
}
