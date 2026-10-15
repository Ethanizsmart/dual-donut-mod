package hoshin.autosell;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class ConfigManager {
   public static double delay_step = 1.0;
   public static int delay_move = 2;
   public static double delay_sleep = 5.0;
   public static int packet_size = 4;
   public static int state = 0;
   public static String msg_code = "000000";
   public static boolean msgctrl = false;
   public static boolean enable_whitelist = false;
   public static boolean enable_blacklist = false;
   public static List<String> whitelist = new ArrayList<>();
   public static List<String> blacklist = new ArrayList<>();
   public static int webhook_interval = 60;
   public static String webhook_url = "";
   private static final File configFile = new File(class_310.method_1551().field_1697, "config/hsnautosell.json");
   private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

   public static void load() {
      if (configFile.exists()) {
         try (FileReader reader = new FileReader(configFile)) {
            ConfigManager.ConfigData data = (ConfigManager.ConfigData)gson.fromJson(reader, ConfigManager.ConfigData.class);
            if (data != null) {
               delay_step = data.delay_step;
               delay_move = data.delay_move;
               delay_sleep = data.delay_sleep;
               packet_size = Math.max(1, Math.min(36, data.packet_size == 0 ? 4 : data.packet_size));
               state = data.state;
               if (data.msg_code != null) {
                  msg_code = data.msg_code;
               }

               msgctrl = data.msgctrl;
               enable_whitelist = data.enable_whitelist;
               enable_blacklist = data.enable_blacklist;
               if (data.whitelist != null) {
                  whitelist = data.whitelist;
               }

               if (data.blacklist != null) {
                  blacklist = data.blacklist;
               }

               if (data.webhook_interval >= 5) {
                  webhook_interval = data.webhook_interval;
               }

               if (data.webhook_url != null) {
                  webhook_url = data.webhook_url;
               }
            }
         } catch (Exception var5) {
            var5.printStackTrace();
         }
      } else {
         save();
      }
   }

   public static void save() {
      try {
         configFile.getParentFile().mkdirs();

         try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(
               new ConfigManager.ConfigData(
                  delay_step,
                  delay_move,
                  delay_sleep,
                  packet_size,
                  state,
                  msg_code,
                  msgctrl,
                  enable_whitelist,
                  enable_blacklist,
                  whitelist,
                  blacklist,
                  webhook_interval,
                  webhook_url
               ),
               writer
            );
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static void registerCommands() {
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> dispatcher.register(
               (LiteralArgumentBuilder)ClientCommandManager.literal("atsell")
                  .then(
                     ClientCommandManager.literal("generate_code")
                        .executes(
                           context -> {
                              msg_code = String.format("%06d", new Random().nextInt(1000000));
                              save();
                              ((FabricClientCommandSource)context.getSource())
                                 .sendFeedback(class_2561.method_43470("§a[AutoSell] Mã điều khiển mới của bạn là: §e" + msg_code + " ĐỪNG ĐỂ LỘ"));
                              return 1;
                           }
                        )
                  )
            )
         );
   }

   private static class ConfigData {
      double delay_step;
      int delay_move;
      double delay_sleep;
      int packet_size;
      int state;
      String msg_code;
      boolean msgctrl;
      boolean enable_whitelist;
      boolean enable_blacklist;
      List<String> whitelist;
      List<String> blacklist;
      int webhook_interval;
      String webhook_url;

      ConfigData(
         double delay_step,
         int delay_move,
         double delay_sleep,
         int packet_size,
         int state,
         String msg_code,
         boolean msgctrl,
         boolean enable_whitelist,
         boolean enable_blacklist,
         List<String> whitelist,
         List<String> blacklist,
         int webhook_interval,
         String webhook_url
      ) {
         this.delay_step = delay_step;
         this.delay_move = delay_move;
         this.delay_sleep = delay_sleep;
         this.packet_size = packet_size;
         this.state = state;
         this.msg_code = msg_code;
         this.msgctrl = msgctrl;
         this.enable_whitelist = enable_whitelist;
         this.enable_blacklist = enable_blacklist;
         this.whitelist = whitelist;
         this.blacklist = blacklist;
         this.webhook_interval = webhook_interval;
         this.webhook_url = webhook_url;
      }
   }
}
