package hoshin.autosell;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModExpiration {
   private static final String readme = "hỡi mấy ông dịch ngược, nếu dịch được thì chúc mừng, đây chỉ là code rác ";
   private static final ZonedDateTime EXPIRE_DATE = ZonedDateTime.of(2026, 9, 23, 23, 59, 59, 0, ZoneId.of("Asia/Ho_Chi_Minh"));
   private static final String TIME_API_URL = "https://worldtimeapi.org/api/timezone/Asia/Ho_Chi_Minh";
   private static final Pattern DATETIME_PATTERN = Pattern.compile("\"datetime\":\"([^\"]+)\"");
   private static Boolean cachedResult = null;

   public static boolean isExpired() {
      if (cachedResult != null) {
         return cachedResult;
      } else {
         ZonedDateTime now = getWebTime();
         if (now == null) {
            now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
         }

         cachedResult = now.isAfter(EXPIRE_DATE);
         return cachedResult;
      }
   }

   private static ZonedDateTime getWebTime() {
      try {
         HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4L)).build();
         HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://worldtimeapi.org/api/timezone/Asia/Ho_Chi_Minh"))
            .timeout(Duration.ofSeconds(4L))
            .GET()
            .build();
         HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
         if (response.statusCode() == 200) {
            Matcher matcher = DATETIME_PATTERN.matcher(response.body());
            if (matcher.find()) {
               String iso = matcher.group(1);
               return ZonedDateTime.parse(iso);
            }
         }
      } catch (Exception var5) {
      }

      return null;
   }
}
