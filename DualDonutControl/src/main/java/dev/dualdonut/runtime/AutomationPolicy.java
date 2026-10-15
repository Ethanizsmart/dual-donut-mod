package dev.dualdonut.runtime;

/** Shared interlocks are independent of each module's selected feature switches. */
public record AutomationPolicy(boolean master, boolean recovering, boolean dead,
                               boolean teleporting, boolean settingsOpen, boolean playerProtection) {
    public AutomationPolicy(boolean master, boolean recovering, boolean dead, boolean teleporting, boolean settingsOpen) {
        this(master, recovering, dead, teleporting, settingsOpen, false);
    }
    public boolean ready() { return master && !recovering && !dead && !teleporting && !settingsOpen && !playerProtection; }
    public boolean use(boolean selected) { return ready() && selected; }
    public boolean homeTimer(boolean selected) { return ready() && selected; }
    public boolean sell(boolean selected, boolean swordBlocking, boolean foreignScreen) {
        return ready() && selected && !swordBlocking && !foreignScreen;
    }
    public boolean home(boolean selected, boolean swordActive, boolean saleBusy, boolean screenOpen) {
        return ready() && selected && !swordActive && !saleBusy && !screenOpen;
    }
}
