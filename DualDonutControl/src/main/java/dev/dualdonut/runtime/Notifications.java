package dev.dualdonut.runtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.dualdonut.DualDonutControl;
import dev.dualdonut.SideIntegration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class Notifications {
    private Notifications() {}
    public static String payload(String title, String message) {
        var embed = new JsonObject(); embed.addProperty("title", title); embed.addProperty("description", message); embed.addProperty("color", 16753920);
        var embeds = new JsonArray(); embeds.add(embed);
        var mentions = new JsonObject(); mentions.add("parse", new JsonArray());
        var root = new JsonObject(); root.add("embeds", embeds); root.add("allowed_mentions", mentions);
        return root.toString();
    }
    public static void protection(String message) {
        if (DualDonutControl.config().protectionAlerts) send("Donut " + SideIntegration.NAME + " – bảo vệ người chơi", message);
    }
    public static void send(String title, String message) {
        String url = SideIntegration.webhookUrl();
        if (url == null || url.isBlank()) return;
        try {
            var request = HttpRequest.newBuilder(URI.create(url.strip())).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload(title, message))).build();
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
                .sendAsync(request, HttpResponse.BodyHandlers.discarding()).exceptionally(ex -> {
                    DualDonutControl.LOGGER.warn("Cannot deliver protection webhook"); return null;
                });
        } catch (RuntimeException ex) { DualDonutControl.LOGGER.warn("Invalid protection webhook configuration"); }
    }
}
