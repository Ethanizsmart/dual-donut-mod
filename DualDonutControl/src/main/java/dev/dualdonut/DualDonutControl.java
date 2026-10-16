package dev.dualdonut;

import dev.dualdonut.runtime.AutomationPolicy;
import dev.dualdonut.runtime.HomeAnchor;
import dev.dualdonut.runtime.PlayerProtection;
import dev.dualdonut.runtime.SharedRecovery;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_418;
import net.minecraft.class_437;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DualDonutControl implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("DualDonutControl");
    private static ControlConfig config;
    private static class_304 masterKey, settingsKey;
    private static boolean master;
    private static Object connection;
    private static class_437 settingsScreen;
    private static long homeGuardUntil;
    private static String lastError = "";

    @Override public void onInitializeClient() {
        config = ControlConfig.load();
        var category = class_304.class_11900.method_74698(class_2960.method_60654("dualdonutcontrol:controls"));
        masterKey = KeyBindingHelper.registerKeyBinding(new class_304("key.dualdonutcontrol.master", GLFW.GLFW_KEY_F8, category));
        settingsKey = KeyBindingHelper.registerKeyBinding(new class_304("key.dualdonutcontrol.settings", GLFW.GLFW_KEY_F9, category));
        LOGGER.info("Dual Donut Control {} loaded. F8 master, F9 settings; master starts OFF.", SideIntegration.NAME);
    }
    public static ControlConfig config() { return config; }
    public static boolean isMasterEnabled() { return master; }
    public static boolean isDead(class_310 client) {
        return client.field_1755 instanceof class_418 || client.field_1724 != null && client.field_1724.method_29504();
    }
    public static boolean isSettingsOpen() { return settingsScreen != null && class_310.method_1551().field_1755 == settingsScreen; }

    /** Each variant installs exactly one driver before its own automation engine. */
    public static void beforeTick(class_310 client) {
        if (config == null) return;
        try {
            if (connection != client.method_1562()) {
                master = false;
                connection = client.method_1562();
                if (connection != null) PlayerProtection.connected();
                resetRuntime(client);
            }
            boolean toggle = consume(masterKey) | SideIntegration.consumeToggle();
            boolean open = consume(settingsKey) | SideIntegration.consumeSettings();
            if (toggle) setMaster(!master);
            if (open && !isDead(client)) openSettings(client.field_1755);
            long now = System.currentTimeMillis();
            SideIntegration.refresh(client);
            PlayerProtection.tick(client, now);
            SharedRecovery.tick(client, now);
            reconcile(client);
            HomeAnchor.tick(client, now);
            SharedConfigScreen.refreshMasterStatus();
            lastError = "";
        } catch (RuntimeException ex) {
            master = false;
            resetRuntime(client);
            if (!ex.toString().equals(lastError)) LOGGER.error("Automation stopped after a tick failure", ex);
            lastError = ex.toString();
        }
    }
    public static boolean consume(class_304 key) {
        boolean pressed = false;
        while (key != null && key.method_1436()) pressed = true;
        return pressed;
    }
    public static void setMaster(boolean enabled) {
        var client = class_310.method_1551();
        master = enabled && client.method_1562() != null && client.field_1724 != null && client.field_1687 != null;
        resetRuntime(client);
        reconcile(client);
        saveAll();
        message("[Donut " + SideIntegration.NAME + "] Master: " + (master ? "§aON" : "§cOFF"));
    }
    private static void reconcile(class_310 client) { SideIntegration.reconcile(client, master && config.automationEnabled, policy(client).ready()); }
    private static AutomationPolicy policy(class_310 client) {
        return new AutomationPolicy(master, SharedRecovery.pending(), isDead(client),
            System.currentTimeMillis() < homeGuardUntil, isSettingsOpen(), PlayerProtection.isBlocking());
    }
    public static boolean canRunUse(class_310 client) { return config != null && policy(client).use(config.automationEnabled); }
    public static boolean canRunSell(class_310 client) {
        return config != null && client.field_1724 != null && client.field_1687 != null
            && policy(client).sell(config.automationEnabled, false, !SideIntegration.screenAllowed(client));
    }
    public static boolean canTickHome(class_310 client) { return config != null && policy(client).homeTimer(config.homeEnabled); }
    public static boolean canRunHome(class_310 client) {
        if (config == null || !canTickHome(client) || client.method_1562() == null) return false;
        if (HomeAnchor.isDue()) SideIntegration.yieldForHome(client);
        return config != null && policy(client).home(config.homeEnabled, SideIntegration.busy(), false, client.field_1755 != null);
    }
    public static boolean sharedSellPause() { return config != null && master && !policy(class_310.method_1551()).ready(); }
    public static String sharedPauseReason() {
        if (!master) return "";
        if (PlayerProtection.isBlocking()) return "PLAYER_PROTECTION";
        if (SharedRecovery.waitingForVex()) return "WAIT_VEX_CLEAR";
        if (SharedRecovery.pending() || isDead(class_310.method_1551())) return "DEATH_RECOVERY";
        if (System.currentTimeMillis() < homeGuardUntil) return "HOME_SETTLE";
        return isSettingsOpen() ? "SHARED_SETTINGS" : "";
    }
    public static void pauseForProtection(class_310 client) { SideIntegration.cancel(client); }
    public static void homeCommandStarting(class_310 client) {
        homeGuardUntil = System.currentTimeMillis() + config.postHomeGuardMs;
        SideIntegration.cancel(client);
    }
    private static void resetRuntime(class_310 client) {
        SideIntegration.cancel(client);
        SharedRecovery.reset();
        PlayerProtection.resetSession();
        HomeAnchor.reset();
        homeGuardUntil = 0;
    }
    public static void saveAll() {
        config.save();
        HomeAnchor.reset();
        reconcile(class_310.method_1551());
        SideIntegration.save();
    }
    public static void onRemoteSellChange(boolean selected) {
        if (config == null) return;
        config.automationEnabled = selected;
        config.save();
        reconcile(class_310.method_1551());
        SideIntegration.save();
    }
    public static void beforeSellTick(class_310 client) { SideIntegration.beforeSellTick(client); }
    public static void saleCommandSent() { SideIntegration.saleCommandSent(); }
    public static void afterSellTick(class_310 client) { SideIntegration.refresh(client); }
    public static void openSettings(class_437 parent) {
        if (!isDead(class_310.method_1551()) && !isSettingsOpen()) class_310.method_1551().method_1507(createSettings(parent));
    }
    public static class_437 createSettings(class_437 parent) {
        SideIntegration.cancel(class_310.method_1551());
        if (parent instanceof net.minecraft.class_465 || parent instanceof net.minecraft.class_11473<?>) parent = null;
        settingsScreen = SharedConfigScreen.create(parent);
        return settingsScreen;
    }
    public static void message(String value) {
        var player = class_310.method_1551().field_1724;
        if (player != null) player.method_7353(class_2561.method_43470(value), false);
    }
}
