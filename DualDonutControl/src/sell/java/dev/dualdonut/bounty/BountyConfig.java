package dev.dualdonut.bounty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.dualdonut.DualDonutControl;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Sell-only settings; no MaLiLib or changes to Farm's configuration. */
public final class BountyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static BountyConfig instance;
    public boolean enabled;
    public String target = "";
    public double intervalMinutes = 7, retrySeconds = 7, commandSeconds = .5, confirmSeconds = 1;
    public BountyEngine.Settings snapshot() { return new BountyEngine.Settings(enabled, target, intervalMinutes, retrySeconds, commandSeconds, confirmSeconds); }
    public void validate() {
        var value = snapshot(); target = value.target(); intervalMinutes = value.intervalMinutes();
        retrySeconds = value.retrySeconds(); commandSeconds = value.commandSeconds(); confirmSeconds = value.confirmSeconds();
    }
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("dualdonutcontrol-sell-bounty.json"); }
    public static BountyConfig get() {
        if (instance == null) {
            if (Files.isRegularFile(path())) {
                try (var reader = Files.newBufferedReader(path(), StandardCharsets.UTF_8)) { instance = GSON.fromJson(reader, BountyConfig.class); }
                catch (Exception ex) { DualDonutControl.LOGGER.error("Cannot load Auto Bounty settings", ex); }
            }
            if (instance == null) instance = new BountyConfig();
            instance.validate();
        }
        return instance;
    }
    public void save() {
        validate();
        try {
            Files.createDirectories(path().getParent());
            try (var writer = Files.newBufferedWriter(path(), StandardCharsets.UTF_8)) { GSON.toJson(this, writer); }
        } catch (Exception ex) { DualDonutControl.LOGGER.error("Cannot save Auto Bounty settings", ex); }
    }
}
