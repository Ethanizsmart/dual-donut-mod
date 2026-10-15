package hoshin.autosell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.Chat;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.Game;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_304.class_11900;

public class Autosell implements ClientModInitializer {
   public static class_304 toggleKey;
   public static class_304 openGuiKey;
   private static final Pattern MSG_PATTERN = Pattern.compile("(\\d{6})\\s+atsell\\s+(\\w+)");
   private static final Pattern MONEY_PATTERN = Pattern.compile(".*\\$\\s*([0-9.]+)([kKmM]?).*");

   public void onInitializeClient() {
      HsnAuthModule.start();
      ConfigManager.load();
      ConfigManager.registerCommands();
      class_11900 CATEGORY = class_11900.method_74698(class_2960.method_60655("autosell_donut", "custom_category"));
      toggleKey = KeyBindingHelper.registerKeyBinding(new class_304("Bật/Tắt AutoSell", 296, CATEGORY));
      openGuiKey = KeyBindingHelper.registerKeyBinding(new class_304("Mở Cài Đặt", 344, CATEGORY));
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         if (client.field_1724 != null) {
            while (openGuiKey.method_1436()) {
               if (client.field_1755 == null) {
                  client.method_1507(AutoSellGui.createGui(null));
               }
            }

            for (; toggleKey.method_1436(); ConfigManager.save()) {
               if (ConfigManager.state != 1 && ConfigManager.state != 2) {
                  ConfigManager.state = 1;
                  AutoSellLogic.reset();
                  this.sendFeedback("§a[AutoSell] Đã BẬT");
               } else {
                  ConfigManager.state = 0;
                  this.sendFeedback("§c[AutoSell] Đã TẮT");
               }
            }

            if (ConfigManager.state == 1 || ConfigManager.state == 2) {
               AutoSellLogic.onTick(client);
               WebhookManager.checkAndSend(client);
            }
         }
      });
      ClientReceiveMessageEvents.GAME.register((Game)(message, overlay) -> this.processChatMessage(message.getString()));
      ClientReceiveMessageEvents.CHAT
         .register((Chat)(message, signedMessage, sender, params, receptionTimestamp) -> this.processChatMessage(message.getString()));
   }

   private void processChatMessage(String text) {
      if (ConfigManager.state == 1 && !AutoSellLogic.isPausedForBounty()) {
         Matcher moneyMatcher = MONEY_PATTERN.matcher(text);
         if (moneyMatcher.find()) {
            AutoSellLogic.receivedMoneyThisCycle = true;

            try {
               double amount = Double.parseDouble(moneyMatcher.group(1));
               String suffix = moneyMatcher.group(2).toLowerCase();
               amount *= suffix.equals("k") ? 1000.0 : (suffix.equals("m") ? 1000000 : 1);
               WebhookManager.addProfit(amount);
            } catch (NumberFormatException var7) {
            }
         }
      }

      if (ConfigManager.msgctrl) {
         Matcher matcher = MSG_PATTERN.matcher(text);
         if (matcher.find()) {
            String code = matcher.group(1);
            String command = matcher.group(2).toLowerCase();
            if (!code.equals(ConfigManager.msg_code)) {
               this.sendFeedback("§c[atsell-msgctrl]: code sai");
               return;
            }

            switch (command) {
               case "on":
                  ConfigManager.state = 1;
                  ConfigManager.save();
                  AutoSellLogic.reset();
                  this.sendFeedback("§a[atsell-msgctrl]: đã BẬT qua tin nhắn");
                  break;
               case "off":
                  ConfigManager.state = 0;
                  ConfigManager.save();
                  this.sendFeedback("§c[atsell-msgctrl]: đã TẮT qua tin nhắn");
                  break;
               default:
                  this.sendFeedback("§c[atsell-msgctrl]: lệnh thực thi ko tồn tại");
            }
         }
      }
   }

   private void sendFeedback(String message) {
      if (class_310.method_1551().field_1724 != null) {
         class_310.method_1551().field_1724.method_7353(class_2561.method_43470(message), false);
      }
   }
}
