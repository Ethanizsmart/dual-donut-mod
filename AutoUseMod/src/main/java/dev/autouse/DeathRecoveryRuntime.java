package dev.autouse;

import net.minecraft.class_310;
import net.minecraft.class_418;

/** Death -> stable zero Vex -> respawn -> world ready -> /home 1 -> settle. */
public final class DeathRecoveryRuntime {
    private static final VexDeathGate GATE = new VexDeathGate();
    private static boolean pending;
    private static boolean waitingForVex;
    private static boolean respawnRequested;
    private static boolean homeSent;
    private static boolean wasDead;
    private static long deathAt;
    private static long respawnAt;
    private static long worldReadyAt = -1;
    private static long homeAt;
    private static Object connection;
    private static Object readyPlayer;
    private static Object readyWorld;
    private static int vexCount;

    private DeathRecoveryRuntime() {}
    public static boolean isRecoveryPending() { return pending; }
    public static boolean isBlockingAutoSell() { return pending || isDead(class_310.method_1551()); }
    public static boolean isWaitingForVex() { return waitingForVex; }
    public static int vexCount() { return vexCount; }
    public static boolean isDead(class_310 client) {
        return client != null && (client.field_1755 instanceof class_418
            || client.field_1724 != null && client.field_1724.method_29504());
    }

    static void tick(class_310 client, long now) {
        if (!MasterAutomation.isEnabled() || !AutoUseMod.config().autoRecovery) { reset(); return; }
        if (client.method_1562() == null) { reset(); return; }
        if (connection != client.method_1562()) { reset(); connection = client.method_1562(); }
        boolean dead = isDead(client);
        boolean newDeath = dead && !wasDead;
        wasDead = dead;
        boolean successful = client.field_1687 != null;
        int count = 0;
        if (successful) {
            try {
                for (var entity : client.field_1687.method_18112()) {
                    if (entity instanceof net.minecraft.class_1634 && entity.method_5805()) count++;
                }
            } catch (RuntimeException ex) { successful = false; }
        }
        vexCount = successful ? count : -1;
        boolean safe = GATE.update(dead, successful, count, now);
        if (!AutoUseMod.config().vexCheck) safe = true;
        if (dead) {
            if (!pending || newDeath) {
                pending = true;
                respawnRequested = false;
                homeSent = false;
                deathAt = now;
                worldReadyAt = -1;
                readyPlayer = readyWorld = null;
                SwordRecoveryRuntime.forceReset();
                HoldUseRuntime.release(client);
                AutoUseMod.LOGGER.info("Death detected: pausing automation and AutoSell");
            }
            waitingForVex = !safe;
            worldReadyAt = -1;
            if (safe && now - deathAt >= AutoUseMod.config().respawnDelayMs
                && client.field_1724 != null && (!respawnRequested || now - respawnAt >= 5000)) {
                client.field_1724.method_7331();
                respawnRequested = true;
                respawnAt = now;
            }
            return;
        }
        waitingForVex = false;
        if (!pending) return;
        if (client.field_1724 == null || client.field_1687 == null || !client.field_1724.method_5805()) {
            worldReadyAt = -1;
            return;
        }
        if (homeSent) {
            if (now - homeAt >= AutoUseMod.config().postHomeGuardMs) {
                reset();
                AutoUseMod.LOGGER.info("Recovery complete after /home 1; resuming automation");
            }
            return;
        }
        if (worldReadyAt < 0 || readyPlayer != client.field_1724 || readyWorld != client.field_1687) {
            worldReadyAt = now;
            readyPlayer = client.field_1724;
            readyWorld = client.field_1687;
        }
        if (now - worldReadyAt >= AutoUseMod.config().homeDelayMs) {
            client.method_1562().method_45730("home 1");
            homeSent = true;
            homeAt = now;
        }
    }

    static void reset() {
        pending = waitingForVex = respawnRequested = homeSent = wasDead = false;
        worldReadyAt = -1;
        connection = null;
        readyPlayer = readyWorld = null;
        vexCount = 0;
        GATE.reset();
    }
}
