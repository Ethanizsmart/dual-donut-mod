package dev.dualdonut.runtime;

/** Independent copy of the AutoUse Vex gate; the original module is retained. */
public final class VexDeathGate {
    private static final long RECENT_VEX_MS = 3000;
    private static final long CLEAR_STABLE_MS = 1000;
    private static final int MIN_CLEAR_SCANS = 5;
    private long lastVexAt = -1;
    private long clearSince = -1;
    private int clearScans;
    private boolean waiting;

    public boolean update(boolean dead, boolean scanSuccessful, int vexCount, long now) {
        if (scanSuccessful && vexCount > 0) lastVexAt = now;
        if (!dead) { waiting = false; resetClear(); return false; }
        boolean danger = scanSuccessful && vexCount > 0
            || lastVexAt >= 0 && now - lastVexAt <= RECENT_VEX_MS;
        if (danger || !scanSuccessful) waiting = true;
        if (!waiting) return true;
        if (!scanSuccessful || vexCount > 0) { resetClear(); return false; }
        if (clearSince < 0 || now < clearSince) { clearSince = now; clearScans = 0; }
        clearScans++;
        return clearScans >= MIN_CLEAR_SCANS && now - clearSince >= CLEAR_STABLE_MS;
    }

    public void reset() { waiting = false; lastVexAt = -1; resetClear(); }
    private void resetClear() { clearSince = -1; clearScans = 0; }
}
