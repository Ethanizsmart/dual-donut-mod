package dev.dualdonut.runtime;

/** Pure state machine: death -> Vex clearance -> respawn -> home -> settle. */
public final class RecoveryState {
    public enum Action { NONE, RESPAWN, HOME, COMPLETE }
    private final VexDeathGate gate = new VexDeathGate();
    private boolean pending, wasDead, waitingForVex, respawnRequested, homeSent;
    private long deathAt, respawnAt, homeAt;
    private long readyAt = -1;
    private Object readyPlayer, readyWorld;
    private boolean startedThisTick;

    public Action tick(boolean dead, boolean scanSuccessful, int vexCount, boolean worldReady,
                       Object player, Object world, long now, boolean vexCheck,
                       int respawnDelay, int homeDelay, int settleDelay) {
        startedThisTick = false;
        boolean newDeath = dead && !wasDead;
        wasDead = dead;
        boolean safe = gate.update(dead, scanSuccessful, vexCount, now) || !vexCheck;
        if (dead) {
            if (!pending || newDeath) {
                pending = startedThisTick = true;
                respawnRequested = homeSent = false;
                deathAt = now;
                readyPlayer = readyWorld = null;
            }
            readyAt = -1;
            waitingForVex = !safe;
            if (safe && now - deathAt >= respawnDelay && player != null
                && (!respawnRequested || now - respawnAt >= 5000)) {
                respawnRequested = true;
                respawnAt = now;
                return Action.RESPAWN;
            }
            return Action.NONE;
        }
        waitingForVex = false;
        if (!pending) return Action.NONE;
        if (!worldReady) { readyAt = -1; return Action.NONE; }
        if (homeSent) {
            if (now - homeAt >= settleDelay) { reset(); return Action.COMPLETE; }
            return Action.NONE;
        }
        if (readyAt < 0 || readyPlayer != player || readyWorld != world) {
            readyAt = now;
            readyPlayer = player;
            readyWorld = world;
        }
        if (now - readyAt >= homeDelay) {
            homeSent = true;
            homeAt = now;
            return Action.HOME;
        }
        return Action.NONE;
    }

    public boolean pending() { return pending; }
    public boolean waitingForVex() { return waitingForVex; }
    public boolean startedThisTick() { return startedThisTick; }
    public void reset() {
        pending = wasDead = waitingForVex = respawnRequested = homeSent = startedThisTick = false;
        readyAt = -1;
        readyPlayer = readyWorld = null;
        gate.reset();
    }
}
