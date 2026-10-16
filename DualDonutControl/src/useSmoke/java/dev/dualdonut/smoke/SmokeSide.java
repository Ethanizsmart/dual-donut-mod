package dev.dualdonut.smoke;

import dev.autouse.MasterAutomation;
import dev.dualdonut.mixin.AutoUseKeysAccessor;
import dev.dualdonut.mixin.MasterAccessor;

public final class SmokeSide {
    public static String absentModClass() { return "hoshin.autosell.Autosell"; }
    public static String[][] targets() {
        return new String[][] {
            {"dev.autouse.AutoUseMod", "dualdonut$beforeUse"},
            {"dev.autouse.MasterAutomation", "dualdonut$setEnabled"},
            {"dev.autouse.DeathRecoveryRuntime", "dualdonut$useSharedRecovery"},
            {"dev.autouse.api.AutoUseApi", "dualdonut$sharedPause"},
            {"dev.autouse.AutoSellWebhookBridge", "dualdonut$swordFunds"}
        };
    }
    public static void verifyAccessors() {
        if (SmokeSide.class.getClassLoader().getResource("dev/dualdonut/bounty/BountyRuntime.class") != null)
            throw new AssertionError("Farm must not contain Auto Bounty");
        MasterAccessor.dualdonut$setEnabled(true);
        if (!MasterAutomation.isEnabled()) throw new AssertionError("Master accessor failed");
        MasterAccessor.dualdonut$setEnabled(false);
        if (AutoUseKeysAccessor.dualdonut$getToggleKey() != null || AutoUseKeysAccessor.dualdonut$getSettingsKey() != null)
            throw new AssertionError("Original entrypoint unexpectedly initialized");
    }
}
