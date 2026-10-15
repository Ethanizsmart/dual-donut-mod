package hoshin.autosell;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.class_310;

public class WebhookManager {
   private static double sessionProfit = 0.0;
   private static LocalDateTime startTime = LocalDateTime.now();
   private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

   public static void addProfit(double amount) {
      sessionProfit += amount;
   }

   public static void resetCycle() {
      sessionProfit = 0.0;
      startTime = LocalDateTime.now();
   }

   public static void checkAndSend(class_310 client) {
      if (ConfigManager.webhook_url != null && !ConfigManager.webhook_url.isEmpty()) {
         LocalDateTime now = LocalDateTime.now();
         long minutesPassed = Duration.between(startTime, now).toMinutes();
         if (minutesPassed >= ConfigManager.webhook_interval) {
            if (client.field_1724 != null) {
               sendDiscordReport(client.field_1724.method_5820(), sessionProfit, startTime, now, ConfigManager.webhook_interval);
            }

            resetCycle();
         }
      }
   }

   private static void sendDiscordReport(String playerName, double profit, LocalDateTime start, LocalDateTime end, int interval) {
      double profitPerHour = profit / interval * 60.0;
      String startStr = start.format(timeFormat);
      String endStr = end.format(timeFormat);
      String profitStr = String.format("%,.2f", profit);
      String profitPerHourStr = String.format("%,.2f", profitPerHour);
      String jsonPayload = "{\n  \"embeds\": [{\n    \"title\": \"Báo cáo lợi nhuận sau %d phút\",\n    \"color\": 65280,\n    \"description\": \"**Người chơi:** %s\\n**Thời gian bắt đầu:** %s\\n**Thời gian kết thúc:** %s\\n**Tổng tiền thu được:** $%s\\n**Lợi nhuận 1 giờ:** $%s\"\n  }]\n}\n"
         .formatted(interval, playerName, startStr, endStr, profitStr, profitPerHourStr);
      sendPostAsync(ConfigManager.webhook_url, jsonPayload);
   }

   public static void sendTestWebhook(String playerName) {
      if (ConfigManager.webhook_url != null && !ConfigManager.webhook_url.isEmpty()) {
         String jsonPayload = "{\n  \"embeds\": [{\n    \"title\": \"Test Webhook AutoSell\",\n    \"color\": 65280,\n    \"description\": \"Xin chào **%s**, kết nối Webhook từ mod đã thành công!\"\n  }]\n}\n"
            .formatted(playerName);
         sendPostAsync(ConfigManager.webhook_url, jsonPayload);
      }
   }

   private static void sendPostAsync(String url, String json) {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(url))
         .header("Content-Type", "application/json")
         .POST(BodyPublishers.ofString(json))
         .build();
      client.sendAsync(request, BodyHandlers.ofString()).exceptionally(e -> {
         System.out.println("[AutoSell] Lỗi gửi Webhook: " + e.getMessage());
         return null;
      });
   }
}
