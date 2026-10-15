package dev.autouse;

import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;

/** Optional integration: neither mod bundles or requires classes from the other. */
final class AutoSellWebhookBridge {
    private static boolean resolved;
    private static boolean warned;
    private static Method send;

    private AutoSellWebhookBridge() {}

    static void insufficientFunds(String playerName, long price) {
        try {
            if (!resolved) {
                resolved = true;
                if (!FabricLoader.getInstance().isModLoaded("autosell_donut")) return;
                send = Class.forName("hoshin.autosell.WebhookManager", false,
                    AutoSellWebhookBridge.class.getClassLoader())
                    .getMethod("notifySwordInsufficientFunds", String.class, long.class);
            }
            if (send != null) send.invoke(null, playerName, price);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            // Never let webhook configuration/transport interrupt the buying state machine.
            if (!warned) {
                warned = true;
                AutoUseMod.LOGGER.warn("Cannot forward sword funds alert; install the webhook-enabled AutoSell build and check its settings.");
            }
        }
    }
}
