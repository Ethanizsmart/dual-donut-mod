package net.autohomeafk;

import net.autohomeafk.config.ConfigManager;
import net.autohomeafk.core.AutoHomeEngine;
import net.autohomeafk.core.ModKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AutoHomeAfkClient implements ClientModInitializer {
   public static final String MOD_ID = "autohomeafk";

   public void onInitializeClient() {
      ConfigManager.load();
      ModKeybinds.register();
      ClientTickEvents.END_CLIENT_TICK.register(AutoHomeEngine::tick);
   }
}
