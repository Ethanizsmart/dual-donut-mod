package dev.dualdonut;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/** Each profile has its own configuration; master state is intentionally not persisted. */
public final class ControlConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean automationEnabled = true;
    public boolean recoveryEnabled = true, vexCheck = true;
    public int respawnDelayMs = 2000, homeDelayMs = 1500, postHomeGuardMs = 8000;
    public String recoveryHomeCommand = "home 1";
    public String saleScreenTitles = "sell|bán";
    public boolean homeEnabled = false, showHotbar = true, hasAnchor = false;
    public String homeCommand = "home";
    public double homeDelayMinutes = 1.0, homeRepeatMinutes = 0.5, homeTolerance = 2.0;
    public double anchorX, anchorZ;
    public boolean autoDisconnect = false, antiPlayer = false;
    public double disconnectRadius = 30.0, antiPlayerRadius = 5.0;
    public int clearDelayMs = 1000;
    public List<String> trustedPlayers = new ArrayList<>();
    public List<String> detectionLog = new ArrayList<>();
    public String killConfirmTitles = "kill|suicide|tự sát";
    public boolean protectionAlerts = true;
    public String webhookUrl = "";

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("dualdonutcontrol-" + SideIntegration.ID + ".json");
    }
    public static ControlConfig load() {
        if (Files.isRegularFile(path())) {
            try (Reader reader = Files.newBufferedReader(path(), StandardCharsets.UTF_8)) {
                var value = GSON.fromJson(reader, ControlConfig.class);
                if (value != null) { value.validate(); return value; }
            } catch (Exception ex) { DualDonutControl.LOGGER.error("Cannot read {}", path(), ex); }
        }
        return new ControlConfig();
    }
    public void save() {
        validate();
        try {
            Files.createDirectories(path().getParent());
            try (Writer writer = Files.newBufferedWriter(path(), StandardCharsets.UTF_8)) { GSON.toJson(this, writer); }
        } catch (Exception ex) { DualDonutControl.LOGGER.error("Cannot save {}", path(), ex); }
    }
    private void validate() {
        recoveryHomeCommand = commandOr(recoveryHomeCommand, "home 1");
        homeCommand = commandOr(homeCommand, "home");
        saleScreenTitles = textOr(saleScreenTitles, "sell|bán");
        killConfirmTitles = textOr(killConfirmTitles, "kill|suicide|tự sát");
        respawnDelayMs = Math.clamp(respawnDelayMs, 0, 30000);
        homeDelayMs = Math.clamp(homeDelayMs, 0, 30000);
        postHomeGuardMs = Math.clamp(postHomeGuardMs, 0, 30000);
        clearDelayMs = Math.clamp(clearDelayMs, 0, 30000);
        disconnectRadius = bounded(disconnectRadius, 30, 1, 128);
        antiPlayerRadius = bounded(antiPlayerRadius, 5, 1, 128);
        homeDelayMinutes = bounded(homeDelayMinutes, 1, 0.05, 1440);
        homeRepeatMinutes = bounded(homeRepeatMinutes, 0.5, 0.05, 1440);
        homeTolerance = bounded(homeTolerance, 2, 0.5, 100);
        if (!Double.isFinite(anchorX) || !Double.isFinite(anchorZ)) { anchorX = anchorZ = 0; hasAnchor = false; }
        trustedPlayers = trustedPlayers == null ? new ArrayList<>() : new ArrayList<>(trustedPlayers.stream().filter(v -> v != null && !v.isBlank()).map(String::strip).distinct().toList());
        detectionLog = detectionLog == null ? new ArrayList<>() : new ArrayList<>(detectionLog.stream().filter(v -> v != null && !v.isBlank()).limit(50).toList());
        webhookUrl = webhookUrl == null ? "" : webhookUrl.strip();
    }
    private static double bounded(double value, double fallback, double min, double max) { return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback; }
    private static String textOr(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.strip(); }
    private static String commandOr(String value, String fallback) { String result = cleanCommand(value); return result.isEmpty() ? fallback : result; }
    public static String cleanCommand(String value) {
        if (value == null) return "";
        value = value.strip();
        while (value.startsWith("/")) value = value.substring(1).strip();
        return value.replace('\n', ' ').replace('\r', ' ');
    }
}
