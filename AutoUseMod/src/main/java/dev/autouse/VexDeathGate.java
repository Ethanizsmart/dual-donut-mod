package dev.autouse;

/** A failed/missing entity scan is UNKNOWN, never evidence that Vex have gone. */
final class VexDeathGate {
    static final long RECENT_VEX_MS = 3000;
    static final long CLEAR_STABLE_MS = 1000;
    static final int MIN_CLEAR_SCANS = 5;
    private long lastVexAt = -1;
    private long clearSince = -1;
    private int clearScans;
    private boolean waiting;

    boolean update(boolean dead, boolean scanSuccessful, int vexCount, long now) {
        if (scanSuccessful && vexCount > 0) lastVexAt = now;
        if (!dead) { waiting = false; resetClear(); return false; }
        boolean danger = scanSuccessful && vexCount > 0
            || lastVexAt >= 0 && now - lastVexAt <= RECENT_VEX_MS;
        if (danger || !scanSuccessful) waiting = true;
        if (!waiting) return true; // ordinary death without Vex nearby
        if (!scanSuccessful || vexCount > 0) { resetClear(); return false; }
        if (clearSince < 0 || now < clearSince) { clearSince = now; clearScans = 0; }
        clearScans++;
        return clearScans >= MIN_CLEAR_SCANS && now - clearSince >= CLEAR_STABLE_MS;
    }

    boolean isWaiting() { return waiting; }
    void reset() { waiting = false; lastVexAt = -1; resetClear(); }
    private void resetClear() { clearSince = -1; clearScans = 0; }
}
