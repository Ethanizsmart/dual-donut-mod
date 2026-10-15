package hoshin.autosell;

import com.google.gson.JsonParser;

public final class SwordFundsWebhookPayloadChecks {
   private static int checks;

   public static void main(String[] args) {
      String player = "Tên\"\\\nngười chơi @everyone";
      var payload = JsonParser.parseString(SwordFundsWebhookPayload.create(player, 1_500_000L)).getAsJsonObject();
      var embeds = payload.getAsJsonArray("embeds");
      check(embeds.size() == 1, "One alert embed");
      var embed = embeds.get(0).getAsJsonObject();
      check(embed.get("title").getAsString().equals("Không đủ tiền mua kiếm"), "Vietnamese alert text");
      var fields = embed.getAsJsonArray("fields");
      check(fields.get(0).getAsJsonObject().get("value").getAsString().equals(player), "Quotes, Unicode, slashes and newline survive JSON encoding");
      check(fields.get(1).getAsJsonObject().get("value").getAsString().contains("Looting III"), "Identify the target sword");
      check(fields.get(2).getAsJsonObject().get("value").getAsString().equals("$1,500,000"), "Send selected listing price, not invented account balance");
      check(payload.getAsJsonObject("allowed_mentions").getAsJsonArray("parse").isEmpty(), "Disable Discord mention parsing");
      for (long price : new long[]{1L, 5_000_000L}) {
         check(JsonParser.parseString(SwordFundsWebhookPayload.create("Steve", price)).isJsonObject(), "Valid price boundary: " + price);
      }
      rejects(null, 1L);
      rejects(" ", 1L);
      rejects("Steve", 0L);
      rejects("Steve", -1L);
      System.out.println("Sword webhook payload: " + checks + " checks passed; no HTTP requests.");
   }

   private static void rejects(String player, long price) {
      try {
         SwordFundsWebhookPayload.create(player, price);
         throw new AssertionError("Invalid alert accepted");
      } catch (IllegalArgumentException expected) { checks++; }
   }

   private static void check(boolean value, String message) {
      checks++;
      if (!value) throw new AssertionError(message);
   }
}
