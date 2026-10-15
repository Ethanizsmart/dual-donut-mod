package dev.dualdonut.runtime;

import dev.dualdonut.DualDonutControl;
import java.util.Locale;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_412;
import net.minecraft.class_437;
import net.minecraft.class_442;
import net.minecraft.class_465;
import net.minecraft.class_639;
import net.minecraft.class_642;

/** Both old DonutUtility protection modes, independent of the automation side. */
public final class PlayerProtection {
    private static final AntiPlayerState ANTI = new AntiPlayerState();
    private static int scanTicks;
    private static boolean disconnectTriggered;
    private static String lastDetectedPlayer = "";
    private static class_639 serverAddress;
    private static class_642 serverInfo;
    private PlayerProtection() {}
    public static void rememberServer(class_639 address, class_642 info) { serverAddress = address; serverInfo = info; }
    public static boolean disconnectedByProtection() { return disconnectTriggered; }
    public static boolean canReconnect() { return serverAddress != null; }
    public static String lastDetectedPlayer() { return lastDetectedPlayer; }
    public static boolean isBlocking() { return disconnectTriggered || ANTI.blocking(); }
    public static void connected() { disconnectTriggered = false; }
    public static void resetSession() { ANTI.reset(); scanTicks = 0; }
    public static void reconnect(boolean addTrusted) {
        if (serverAddress == null) return;
        if (addTrusted && !lastDetectedPlayer.isBlank()) {
            var cfg = DualDonutControl.config();
            if (!PlayerDetectionRules.trusted(lastDetectedPlayer, cfg.trustedPlayers)) cfg.trustedPlayers.add(lastDetectedPlayer);
            cfg.save();
        }
        disconnectTriggered = false;
        class_412.method_36877(new class_442(), class_310.method_1551(), serverAddress, serverInfo, false, null);
    }

    public static void tick(class_310 client, long now) {
        if (!DualDonutControl.isMasterEnabled() || client.method_1562() == null) { ANTI.reset(); return; }
        var cfg = DualDonutControl.config();
        if (!cfg.antiPlayer) ANTI.reset();
        boolean fresh = ++scanTicks >= 10;
        boolean scanSuccessful = false, enemyNearby = true;
        String antiName = null, disconnectName = null;
        double antiDistance = Double.POSITIVE_INFINITY, disconnectDistance = Double.POSITIVE_INFINITY;
        if (fresh) {
            scanTicks = 0;
            if (client.field_1724 != null && client.field_1687 != null && !DualDonutControl.isDead(client)) {
                try {
                    var self = client.field_1724;
                    for (var player : client.field_1687.method_18456()) {
                        if (player == null) continue;
                        String name = player.method_5820();
                        double dx = player.method_23317() - self.method_23317(), dy = player.method_23318() - self.method_23318(), dz = player.method_23321() - self.method_23321();
                        double distance = dx * dx + dy * dy + dz * dz;
                        if (PlayerDetectionRules.threat(name, player == self, player.method_5805(), player.method_7325(), distance, cfg.disconnectRadius, cfg.trustedPlayers)
                            && distance < disconnectDistance) { disconnectName = name; disconnectDistance = distance; }
                        if (PlayerDetectionRules.threat(name, player == self, player.method_5805(), player.method_7325(), distance, cfg.antiPlayerRadius, cfg.trustedPlayers)
                            && distance < antiDistance) { antiName = name; antiDistance = distance; }
                    }
                    scanSuccessful = true;
                    enemyNearby = antiName != null;
                } catch (RuntimeException ex) { scanSuccessful = false; }
            }
            // Disconnect has priority over pearl/kill, including an already-running AntiPlayer sequence.
            if (scanSuccessful && cfg.autoDisconnect && disconnectName != null && !disconnectTriggered) {
                record(disconnectName, Math.sqrt(disconnectDistance), "Auto Disconnect");
                disconnectTriggered = true;
                DualDonutControl.setMaster(false);
                client.method_1562().method_48296().method_10747(class_2561.method_43470(
                    String.format(Locale.ROOT, "Donut: %s detected at %.1f blocks", disconnectName, Math.sqrt(disconnectDistance))));
                return;
            }
            if (scanSuccessful && cfg.antiPlayer && antiName != null && !ANTI.blocking() && !DualDonutControl.isSettingsOpen()) {
                record(antiName, Math.sqrt(antiDistance), "AntiPlayer");
                ANTI.start(now);
                DualDonutControl.pauseForProtection(client);
                if (client.field_1755 instanceof class_465 && client.field_1724 != null) client.field_1724.method_7346();
            }
        }
        if (client.field_1724 == null || client.field_1687 == null) return;
        var action = ANTI.tick(now, DualDonutControl.isDead(client), fresh, scanSuccessful, enemyNearby, cfg.clearDelayMs);
        switch (action) {
            case THROW_PEARL -> {
                boolean success = client.field_1724 != null && client.field_1761 != null
                    && client.field_1724.method_31548().method_5438(40).method_31574(class_1802.field_8634);
                if (success) client.field_1761.method_2919(client.field_1724, class_1268.field_5810);
                else DualDonutControl.message("[Donut AntiPlayer] Không có Ender Pearl ở tay trái.");
                ANTI.pearlResult(success);
            }
            case KILL -> client.method_1562().method_45730("kill");
            case CONFIRM_TIMEOUT -> DualDonutControl.message("[Donut AntiPlayer] Hết thời gian chờ xác nhận /kill; tiếp tục chờ người lạ rời đi.");
            default -> { }
        }
        if (ANTI.canConfirm(now) && client.field_1755 instanceof class_465 gui && client.field_1724 != null && client.field_1761 != null
            && SaleScreenRules.matches(gui.method_25440().getString(), cfg.killConfirmTitles)) {
            var handler = gui.method_17577();
            for (var slot : handler.field_7761) {
                if (slot.field_7871 != client.field_1724.method_31548() && slot.method_7677().method_31574(class_1802.field_8581)) {
                    client.field_1761.method_2906(handler.field_7763, slot.field_7874, 0, class_1713.field_7790, client.field_1724);
                    ANTI.confirmed();
                    break;
                }
            }
        }
    }
    private static void record(String name, double distance, String mode) {
        lastDetectedPlayer = name;
        var cfg = DualDonutControl.config();
        cfg.detectionLog.removeIf(v -> v.equalsIgnoreCase(name));
        cfg.detectionLog.add(0, name);
        while (cfg.detectionLog.size() > 50) cfg.detectionLog.removeLast();
        cfg.save();
        String message = String.format(Locale.ROOT, "%s: phát hiện %s cách %.1f block", mode, name, distance);
        DualDonutControl.message("[Donut] " + message);
        Notifications.protection(message);
    }
}
