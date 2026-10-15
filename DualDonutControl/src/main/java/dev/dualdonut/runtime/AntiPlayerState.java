package dev.dualdonut.runtime;

/** Two pearl attempts -> wait two seconds -> /kill once -> bounded confirm -> wait clear. */
public final class AntiPlayerState {
    public enum Action { NONE, THROW_PEARL, KILL, CONFIRM_TIMEOUT }
    public enum Phase { IDLE, PEARL_FIRST, PEARL_SECOND, WAIT_KILL, WAIT_CONFIRM, WAIT_CLEAR }
    private Phase phase = Phase.IDLE;
    private long nextAt, confirmDeadline, clearSince = -1;
    private int pearlFailures;
    public void start(long now) { reset(); phase = Phase.PEARL_FIRST; nextAt = now; }
    public Action tick(long now, boolean dead, boolean freshScan, boolean scanSuccessful, boolean enemyNearby, int clearDelayMs) {
        if (phase == Phase.IDLE) return Action.NONE;
        if (dead && phase != Phase.WAIT_CLEAR) waitClear();
        if (phase == Phase.WAIT_CLEAR) {
            if (freshScan) {
                if (!scanSuccessful || enemyNearby || dead) clearSince = -1;
                else {
                    if (clearSince < 0 || now < clearSince) clearSince = now;
                    if (now - clearSince >= clearDelayMs) reset();
                }
            }
            return Action.NONE;
        }
        if (phase == Phase.WAIT_CONFIRM) {
            if (now >= confirmDeadline) { waitClear(); return Action.CONFIRM_TIMEOUT; }
            return Action.NONE;
        }
        if (now < nextAt) return Action.NONE;
        switch (phase) {
            case PEARL_FIRST -> { phase = Phase.PEARL_SECOND; nextAt = now + 250; return Action.THROW_PEARL; }
            case PEARL_SECOND -> { phase = Phase.WAIT_KILL; nextAt = now + 2250; return Action.THROW_PEARL; }
            case WAIT_KILL -> { phase = Phase.WAIT_CONFIRM; nextAt = now + 1000; confirmDeadline = now + 5000; return Action.KILL; }
            default -> { return Action.NONE; }
        }
    }
    public void pearlResult(boolean success) { if (!success && ++pearlFailures >= 2) waitClear(); }
    public boolean canConfirm(long now) { return phase == Phase.WAIT_CONFIRM && now >= nextAt && now < confirmDeadline; }
    public void confirmed() { if (phase == Phase.WAIT_CONFIRM) waitClear(); }
    private void waitClear() { phase = Phase.WAIT_CLEAR; clearSince = -1; }
    public boolean blocking() { return phase != Phase.IDLE; }
    public Phase phase() { return phase; }
    public void reset() { phase = Phase.IDLE; pearlFailures = 0; nextAt = confirmDeadline = 0; clearSince = -1; }
}
