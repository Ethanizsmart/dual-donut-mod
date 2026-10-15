package dev.dualdonut.smoke;

public final class SmokeSide {
    public static String absentModClass() { return "dev.autouse.AutoUseMod"; }
    public static String[][] targets() {
        return new String[][] {
            {"net.minecraft.class_310", "dualdonut$beforeClientTick"},
            {"hoshin.autosell.AutoSellLogic", "dualdonut$guardSale"},
            {"hoshin.autosell.Autosell", "dualdonut$syncRemoteSelection"},
            {"hoshin.autosell.AutoSellGui", "dualdonut$sharedGui"}
        };
    }
    public static void verifyAccessors() {}
}
