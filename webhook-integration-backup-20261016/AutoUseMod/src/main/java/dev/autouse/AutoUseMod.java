package dev.autouse;

import dev.autouse.api.AutoUseApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_2561;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AutoUseMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("AutoUseMod");
    private static AutoUseConfig config;
    private static class_304 toggleKey;
    private static class_304 settingsKey;
    private static Object connection;
    private static String lastError = "";

    @Override public void onInitializeClient() {
        config = AutoUseConfig.load();
        var category = class_304.class_11900.method_74698(class_2960.method_60654("autousemod:keys"));
        toggleKey = KeyBindingHelper.registerKeyBinding(new class_304("key.autousemod.toggle", GLFW.GLFW_KEY_F8, category));
        settingsKey = KeyBindingHelper.registerKeyBinding(new class_304("key.autousemod.settings", GLFW.GLFW_KEY_F9, category));
        LOGGER.info("AutoUseMod loaded; F8 toggles automation, F9 opens settings. Master starts OFF.");
    }

    public static AutoUseConfig config() { return config; }
    public static void tick() {
        if (config == null || toggleKey == null) return;
        class_310 client = class_310.method_1551();
        while (toggleKey.method_1436()) MasterAutomation.toggle();
        while (settingsKey.method_1436()) {
            // Do not replace the death screen while the Vex gate is holding it.
            if (!DeathRecoveryRuntime.isDead(client)) {
                SwordRecoveryRuntime.cancel(client);
                client.method_1507(new AutoUseScreen(client.field_1755));
            }
        }
        if (connection != client.method_1562()) { resetRuntime(); connection = client.method_1562(); }
        if (!MasterAutomation.isEnabled()) { HoldUseRuntime.release(client); return; }
        try {
            DeathRecoveryRuntime.tick(client, System.currentTimeMillis());
            SwordRecoveryRuntime.onTick();
            HoldUseRuntime.tick(client);
            lastError = "";
        } catch (RuntimeException ex) {
            HoldUseRuntime.release(client);
            String error = ex.toString();
            if (!error.equals(lastError)) LOGGER.error("Automation tick failed", ex);
            lastError = error;
        }
    }

    public static void resetRuntime() {
        class_310 client = class_310.method_1551();
        HoldUseRuntime.release(client);
        SwordRecoveryRuntime.cancel(client);
        DeathRecoveryRuntime.reset();
        AutoUseApi.reset();
    }

    public static void message(String message) {
        var client = class_310.method_1551();
        if (client.field_1724 != null) client.field_1724.method_7353(class_2561.method_43470(message), true);
    }
}
