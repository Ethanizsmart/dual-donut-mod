package hoshin.autosell;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Locale;

/** Pure payload construction; this class never reads config or sends HTTP. */
final class SwordFundsWebhookPayload {
   private SwordFundsWebhookPayload() {}

   static String create(String playerName, long price) {
      if (playerName == null || playerName.isBlank() || price <= 0) {
         throw new IllegalArgumentException("Missing player or purchase price");
      }
      JsonObject embed = new JsonObject();
      embed.addProperty("title", "Không đủ tiền mua kiếm");
      embed.addProperty("description", "Auto Buy Sword nhận phản hồi thiếu tiền từ server.");
      embed.addProperty("color", 15158332);
      JsonArray fields = new JsonArray();
      fields.add(field("Người chơi", playerName));
      fields.add(field("Kiếm", "Diamond Sword — Looting III"));
      fields.add(field("Giá kiếm đang mua", String.format(Locale.US, "$%,d", price)));
      embed.add("fields", fields);
      JsonArray embeds = new JsonArray();
      embeds.add(embed);
      JsonObject payload = new JsonObject();
      payload.add("embeds", embeds);
      JsonObject mentions = new JsonObject();
      mentions.add("parse", new JsonArray());
      payload.add("allowed_mentions", mentions);
      return payload.toString();
   }

   private static JsonObject field(String name, String value) {
      JsonObject field = new JsonObject();
      field.addProperty("name", name);
      field.addProperty("value", value);
      return field;
   }
}
