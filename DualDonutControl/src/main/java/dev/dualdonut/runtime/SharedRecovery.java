package dev.dualdonut.runtime;

import dev.dualdonut.ControlConfig;
import dev.dualdonut.DualDonutControl;
import net.minecraft.class_1634;
import net.minecraft.class_310;

/** Independent of AutoUse/AutoSell: both variants have the same Vex recovery. */
public final class SharedRecovery {
    private static final RecoveryState STATE = new RecoveryState();
    private static int vexCount;
    private SharedRecovery() {}
    public static void tick(class_310 client, long now) {
        var cfg = DualDonutControl.config();
        if (!DualDonutControl.isMasterEnabled() || !cfg.recoveryEnabled || client.method_1562() == null) { reset(); return; }
        boolean successful = client.field_1687 != null;
        int count = 0;
        if (successful) {
            try { for (var entity : client.field_1687.method_18112()) if (entity instanceof class_1634 && entity.method_5805()) count++; }
            catch (RuntimeException ex) { successful = false; }
        }
        vexCount = successful ? count : -1;
        boolean ready = client.field_1724 != null && client.field_1687 != null && client.field_1724.method_5805();
        var action = STATE.tick(DualDonutControl.isDead(client), successful, count, ready, client.field_1724, client.field_1687,
            now, cfg.vexCheck, cfg.respawnDelayMs, cfg.homeDelayMs, cfg.postHomeGuardMs);
        if (STATE.startedThisTick()) DualDonutControl.pauseForProtection(client);
        switch (action) {
            case RESPAWN -> client.field_1724.method_7331();
            case HOME -> client.method_1562().method_45730(ControlConfig.cleanCommand(cfg.recoveryHomeCommand));
            case COMPLETE -> DualDonutControl.LOGGER.info("Recovery complete; resuming selected automation");
            default -> { }
        }
    }
    public static boolean pending() { return STATE.pending(); }
    public static boolean waitingForVex() { return STATE.waitingForVex(); }
    public static int vexCount() { return vexCount; }
    public static void reset() { STATE.reset(); vexCount = 0; }
}
