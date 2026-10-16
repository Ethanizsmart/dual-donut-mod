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
    public static void verifyAccessors() throws Exception {
        var bounty = Class.forName("dev.dualdonut.bounty.BountyRuntime", false, SmokeSide.class.getClassLoader());
        bounty.getDeclaredMethods();
        if (!bounty.getMethod("category").getReturnType().getName().equals("dev.isxander.yacl3.api.ConfigCategory"))
            throw new AssertionError("Sell Auto Bounty GUI is missing");
        var type = Class.forName("hoshin.autosell.AutoSellLogic", false, SmokeSide.class.getClassLoader());
        if (type.getDeclaredMethod("canYieldForHome").getReturnType() != boolean.class)
            throw new AssertionError("AutoSell safe home handoff API is missing");
        if (type.getDeclaredMethod("tickRelease").getReturnType() != void.class)
            throw new AssertionError("AutoSell deferred stop API is missing");
    }
}
