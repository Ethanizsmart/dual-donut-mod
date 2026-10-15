package dev.autouse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Feature preferences are persistent; the master switch starts OFF each launch. */
public final class AutoUseConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("autousemod.json");
    public boolean holdRmb = true;
    public boolean autoSword = true;
    public boolean autoRecovery = true;
    public boolean vexCheck = true;
    public int respawnDelayMs = 2000;
    public int homeDelayMs = 1500;
    public int postHomeGuardMs = 8000;

    public static AutoUseConfig load() {
        if (Files.isRegularFile(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                AutoUseConfig value = GSON.fromJson(reader, AutoUseConfig.class);
                if (value != null) { value.validate(); return value; }
            } catch (Exception ex) { AutoUseMod.LOGGER.error("Cannot read {}", PATH, ex); }
        }
        return new AutoUseConfig();
    }

    public void save() {
        validate();
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) { GSON.toJson(this, writer); }
        } catch (Exception ex) { AutoUseMod.LOGGER.error("Cannot save {}", PATH, ex); }
    }

    private void validate() {
        respawnDelayMs = Math.clamp(respawnDelayMs, 0, 30000);
        homeDelayMs = Math.clamp(homeDelayMs, 0, 30000);
        postHomeGuardMs = Math.clamp(postHomeGuardMs, 0, 30000);
    }
}
