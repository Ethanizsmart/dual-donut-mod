package dev.dualdonut;
import net.minecraft.class_304;
import net.minecraft.class_310;
/** Only the shared policy/config boundary is doubled; SideIntegration is the production source. */
public final class DualDonutControl {
    private static final ControlConfig config = new ControlConfig();
    public static ControlConfig config() { return config; }
    public static boolean consume(class_304 key) { return false; }
    public static boolean canRunSell(class_310 client) { return SideIntegration.screenAllowed(client); }
}
