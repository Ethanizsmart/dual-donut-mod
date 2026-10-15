package net.autohomeafk.core;

import net.autohomeafk.config.ConfigManager;
import net.autohomeafk.config.ModConfig;
import net.autohomeafk.gui.AutoHomeConfigScreen;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class AutoHomeEngine {
   private static long delayTicks = 0L;
   private static long repeatTicks = 0L;
   private static boolean isWaitingRepeat = false;

   public static void tick(class_310 client) {
      if (client != null) {
         while (ModKeybinds.configScreenKey != null && ModKeybinds.configScreenKey.method_1436()) {
            client.method_1507(new AutoHomeConfigScreen(client.field_1755));
         }

         for (; ModKeybinds.toggleKey != null && ModKeybinds.toggleKey.method_1436(); ConfigManager.save()) {
            ModConfig config = ConfigManager.get();
            config.enabled = !config.enabled;
            if (config.enabled) {
               if (client.field_1724 != null) {
                  config.anchorX = client.field_1724.method_23317();
                  config.anchorZ = client.field_1724.method_23321();
                  config.hasAnchor = true;
                  resetTimers(config);
                  client.field_1724
                     .method_7353(
                        class_2561.method_43470(String.format("§a[AutoHome] Đã BẬT! Khóa mốc tại [X: %.1f, Z: %.1f]", config.anchorX, config.anchorZ)), true
                     );
               }
            } else {
               isWaitingRepeat = false;
               if (client.field_1724 != null) {
                  client.field_1724.method_7353(class_2561.method_43470("§c[AutoHome] Đã TẮT! Dừng hoạt động."), true);
               }
            }
         }

         ModConfig config = ConfigManager.get();
         if (config.enabled && client.field_1724 != null && client.field_1687 != null) {
            if (!config.hasAnchor) {
               config.anchorX = client.field_1724.method_23317();
               config.anchorZ = client.field_1724.method_23321();
               config.hasAnchor = true;
               resetTimers(config);
               ConfigManager.save();
            }

            double dx = client.field_1724.method_23317() - config.anchorX;
            double dz = client.field_1724.method_23321() - config.anchorZ;
            double distance2D = Math.sqrt(dx * dx + dz * dz);
            if (distance2D <= config.toleranceRadius) {
               resetTimers(config);
               isWaitingRepeat = false;
               if (config.showHotbar) {
                  client.field_1724.method_7353(class_2561.method_43470("§aAn Toàn"), true);
               }
            } else if (!isWaitingRepeat) {
               delayTicks--;
               if (delayTicks > 0L) {
                  long totalSecs = (delayTicks + 19L) / 20L;
                  long mins = totalSecs / 60L;
                  long secs = totalSecs % 60L;
                  if (config.showHotbar) {
                     client.field_1724.method_7353(class_2561.method_43470(String.format("§e%02d:%02d", mins, secs)), true);
                  }
               } else {
                  sendHomeCommand(client, config.command);
                  isWaitingRepeat = true;
                  repeatTicks = Math.max(20L, (long)(config.repeatIntervalMinutes * 60.0 * 20.0));
                  if (config.showHotbar) {
                     client.field_1724.method_7353(class_2561.method_43470("§e00:00"), true);
                  }
               }
            } else {
               repeatTicks--;
               if (repeatTicks > 0L) {
                  long totalSecs = (repeatTicks + 19L) / 20L;
                  long mins = totalSecs / 60L;
                  long secs = totalSecs % 60L;
                  if (config.showHotbar) {
                     client.field_1724.method_7353(class_2561.method_43470(String.format("§e%02d:%02d", mins, secs)), true);
                  }
               } else {
                  sendHomeCommand(client, config.command);
                  repeatTicks = Math.max(20L, (long)(config.repeatIntervalMinutes * 60.0 * 20.0));
                  if (config.showHotbar) {
                     client.field_1724.method_7353(class_2561.method_43470("§e00:00"), true);
                  }
               }
            }
         }
      }
   }

   public static void resetTimers(ModConfig config) {
      delayTicks = Math.max(20L, (long)(config.delayMinutes * 60.0 * 20.0));
      repeatTicks = Math.max(20L, (long)(config.repeatIntervalMinutes * 60.0 * 20.0));
   }

   public static void setAnchorToCurrentPos(class_310 client) {
      if (client != null && client.field_1724 != null) {
         ModConfig config = ConfigManager.get();
         config.anchorX = client.field_1724.method_23317();
         config.anchorZ = client.field_1724.method_23321();
         config.hasAnchor = true;
         resetTimers(config);
         isWaitingRepeat = false;
         ConfigManager.save();
         client.field_1724
            .method_7353(
               class_2561.method_43470(String.format("§a[AutoHome] Đã cập nhật mốc mới tại [X: %.1f, Z: %.1f]", config.anchorX, config.anchorZ)), true
            );
      }
   }

   public static void sendHomeCommand(class_310 client, String rawCommand) {
      if (client != null && client.method_1562() != null && rawCommand != null) {
         String cmd = cleanCommand(rawCommand);
         if (!cmd.isEmpty()) {
            client.method_1562().method_45730(cmd);
         }
      }
   }

   public static String cleanCommand(String raw) {
      if (raw == null) {
         return "";
      } else {
         String trimmed = raw.trim();

         while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1).trim();
         }

         return trimmed;
      }
   }
}
