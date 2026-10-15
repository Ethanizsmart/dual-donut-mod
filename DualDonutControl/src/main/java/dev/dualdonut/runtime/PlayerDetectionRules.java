package dev.dualdonut.runtime;

import java.util.List;

public final class PlayerDetectionRules {
    private PlayerDetectionRules() {}
    public static boolean trusted(String name, List<String> names) {
        return name != null && names != null && names.stream().anyMatch(v -> v != null && name.strip().equalsIgnoreCase(v.strip()));
    }
    public static boolean threat(String name, boolean self, boolean alive, boolean spectator, double distanceSquared,
                                 double radius, List<String> whitelist) {
        return !self && alive && !spectator && name != null && !name.isBlank() && !trusted(name, whitelist)
            && Double.isFinite(distanceSquared) && distanceSquared >= 0 && Double.isFinite(radius) && radius > 0
            && distanceSquared <= radius * radius;
    }
}
