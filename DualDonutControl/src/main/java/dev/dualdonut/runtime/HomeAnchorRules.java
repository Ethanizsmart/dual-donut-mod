package dev.dualdonut.runtime;

/** Position and facing comparisons, independent of the Minecraft client. */
public final class HomeAnchorRules {
    private HomeAnchorRules() {}
    public static boolean withinPosition(double x, double z, double anchorX, double anchorZ, double tolerance) {
        double dx = x - anchorX, dz = z - anchorZ;
        return dx * dx + dz * dz <= tolerance * tolerance;
    }
    public static double wrapYaw(double yaw) {
        return ((yaw % 360 + 540) % 360) - 180;
    }
    public static boolean withinFacing(double yaw, double pitch, double anchorYaw, double anchorPitch, double tolerance) {
        return Math.abs(wrapYaw(yaw - anchorYaw)) <= tolerance && Math.abs(pitch - anchorPitch) <= tolerance;
    }
}
