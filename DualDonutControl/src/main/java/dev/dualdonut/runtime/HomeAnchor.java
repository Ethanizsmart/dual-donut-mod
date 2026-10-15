package dev.dualdonut.runtime;

import dev.dualdonut.ControlConfig;
import dev.dualdonut.DualDonutControl;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_310;

/** Beatrix-style X/Z anchor and repeat command, embedded in both variants. */
public final class HomeAnchor {
    private static long ticksLeft = -1;
    private static boolean repeat;
    private HomeAnchor() {}
    public static void tick(class_310 client, long now) {
        if (!DualDonutControl.canTickHome(client) || client.field_1724 == null || client.field_1687 == null) return;
        var cfg = DualDonutControl.config();
        if (!cfg.hasAnchor) setAnchor(client);
        double dx = client.field_1724.method_23317() - cfg.anchorX, dz = client.field_1724.method_23321() - cfg.anchorZ;
        if (dx * dx + dz * dz <= cfg.homeTolerance * cfg.homeTolerance) {
            reset();
            if (cfg.showHotbar) client.field_1724.method_7353(class_2561.method_43470("§a[Donut Home] An toàn"), true);
            return;
        }
        if (ticksLeft < 0) ticksLeft = delayTicks(cfg.homeDelayMinutes);
        if (ticksLeft > 0) ticksLeft--;
        if (ticksLeft == 0 && DualDonutControl.canRunHome(client) && client.method_1562() != null) {
            DualDonutControl.homeCommandStarting(client);
            client.method_1562().method_45730(ControlConfig.cleanCommand(cfg.homeCommand));
            repeat = true;
            ticksLeft = delayTicks(cfg.homeRepeatMinutes);
        }
        if (cfg.showHotbar) {
            long seconds = (ticksLeft + 19) / 20;
            client.field_1724.method_7353(class_2561.method_43470(String.format(Locale.ROOT, "§e[Donut Home] %02d:%02d%s", seconds / 60, seconds % 60, repeat ? " ↻" : "")), true);
        }
    }
    private static long delayTicks(double minutes) { return Math.max(20, (long) (minutes * 1200)); }
    public static void setAnchor(class_310 client) {
        if (client.field_1724 == null) return;
        var cfg = DualDonutControl.config();
        cfg.anchorX = client.field_1724.method_23317(); cfg.anchorZ = client.field_1724.method_23321(); cfg.hasAnchor = true;
        reset(); cfg.save();
        DualDonutControl.message(String.format(Locale.ROOT, "[Donut Home] Mốc: X %.1f, Z %.1f", cfg.anchorX, cfg.anchorZ));
    }
    public static void reset() { ticksLeft = -1; repeat = false; }
}
