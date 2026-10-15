package dev.autouse.api;

import dev.autouse.DeathRecoveryRuntime;
import dev.autouse.MasterAutomation;
import dev.autouse.SwordRecoveryRuntime;

/** Stable, optional integration point for the separate AutoSell mod. Do not bundle this class there. */
public final class AutoUseApi {
    private static volatile boolean autoSellBusy;
    private static volatile long autoSellHeartbeat;
    private AutoUseApi() {}

    /** Call before starting/continuing a sale. Reset/cancel its transaction while true. */
    public static boolean isAutoSellPaused() {
        return MasterAutomation.isEnabled() && (DeathRecoveryRuntime.isBlockingAutoSell()
            || SwordRecoveryRuntime.isRecoveryBlockingAutoSell());
    }

    /** AutoSell publishes its current container/transaction ownership each client tick. */
    public static void setAutoSellBusy(boolean busy) {
        autoSellBusy = busy;
        autoSellHeartbeat = System.nanoTime();
    }

    public static boolean isAutoSellBusy() {
        return autoSellBusy && System.nanoTime() - autoSellHeartbeat < 2_000_000_000L;
    }

    /** A full inventory is blocking sword purchase; the separate AutoSell may free space. */
    public static boolean needsInventorySpace() {
        return MasterAutomation.isEnabled() && SwordRecoveryRuntime.allowAutoSellDuringRecovery()
            && !DeathRecoveryRuntime.isBlockingAutoSell();
    }

    public static String pauseReason() {
        if (!MasterAutomation.isEnabled()) return "";
        if (DeathRecoveryRuntime.isWaitingForVex()) return "WAIT_VEX_CLEAR";
        if (DeathRecoveryRuntime.isBlockingAutoSell()) return "DEATH_RECOVERY";
        if (SwordRecoveryRuntime.isRecoveryBlockingAutoSell()) return "BUYING_SWORD";
        return "";
    }

    public static void reset() { autoSellBusy = false; autoSellHeartbeat = 0; }
}
