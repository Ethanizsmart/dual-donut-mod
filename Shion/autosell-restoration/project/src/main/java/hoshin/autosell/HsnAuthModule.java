package hoshin.autosell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.class_310;

public class HsnAuthModule {
   private static final String SECRET_KEY = "HsnAuthSecret159";
   private static final String GITHUB_CONFIG_URL = "https://raw.githubusercontent.com/huy15912/hsn-auth-ip/refs/heads/main/api.txt";
   private static String SERVER_URL = "";

   public static void start() {
      Thread authThread = new Thread(() -> {
         try {
            while (class_310.method_1551() == null || class_310.method_1551().method_1548() == null) {
               Thread.sleep(2000L);
            }

            String igName = class_310.method_1551().method_1548().method_1676();
            String uuid = class_310.method_1551().method_1548().method_44717().toString();
            String modName = "auto sell";
            String hwid = getOrCreateHWID();

            while (true) {
               try {
                  if (SERVER_URL == null || SERVER_URL.isEmpty()) {
                     boolean isSuccess = fetchServerConfig();
                     if (!isSuccess) {
                        Thread.sleep(5000L);
                        continue;
                     }
                  }

                  String jsonPayload = String.format("{\"uuid\":\"%s\",\"ig\":\"%s\",\"hwid\":\"%s\",\"mod\":\"%s\"}", uuid, igName, hwid, modName);
                  String encryptedData = encryptAES(jsonPayload);
                  String response = sendPostRequest(encryptedData);
                  if (response != null && response.contains("BLACKLIST")) {
                     System.exit(0);
                  }

                  Thread.sleep(30000L);
               } catch (Throwable var7) {
                  Thread.sleep(30000L);
               }
            }
         } catch (Throwable var8) {
         }
      });
      authThread.setDaemon(true);
      authThread.start();
   }

   private static boolean fetchServerConfig() {
      try {
         URL url = URI.create("https://raw.githubusercontent.com/huy15912/hsn-auth-ip/refs/heads/main/api.txt").toURL();
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("GET");
         conn.setConnectTimeout(5000);
         conn.setReadTimeout(5000);
         if (conn.getResponseCode() == 200) {
            boolean var3;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
               SERVER_URL = br.readLine().trim();
               var3 = true;
            }

            return var3;
         }
      } catch (Exception var7) {
      }

      return false;
   }

   private static String encryptAES(String data) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      SecretKeySpec keySpec = new SecretKeySpec("HsnAuthSecret159".getBytes(StandardCharsets.UTF_8), "AES");
      cipher.init(1, keySpec);
      return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
   }

   private static String sendPostRequest(String encryptedPayload) {
      try {
         URL url = URI.create(SERVER_URL).toURL();
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Content-Type", "application/json");
         conn.setDoOutput(true);
         String jsonBody = "{\"payload\":\"" + encryptedPayload + "\"}";

         try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
         }

         if (conn.getResponseCode() == 200) {
            String var7;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
               StringBuilder response = new StringBuilder();

               String line;
               while ((line = br.readLine()) != null) {
                  response.append(line.trim());
               }

               var7 = response.toString();
            }

            return var7;
         }
      } catch (Exception var12) {
      }

      return null;
   }

   private static String getOrCreateHWID() {
      File hwidFile = new File(".hsn_hwid");

      try {
         if (hwidFile.exists()) {
            String var12;
            try (BufferedReader reader = new BufferedReader(new FileReader(hwidFile))) {
               var12 = reader.readLine();
            }

            return var12;
         } else {
            String newHwid = "HSN-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();

            try (FileWriter writer = new FileWriter(hwidFile)) {
               writer.write(newHwid);
            }

            try {
               Runtime.getRuntime().exec("attrib +h .hsn_hwid");
            } catch (Exception var8) {
            }

            return newHwid;
         }
      } catch (Exception var10) {
         return "HWID-ERROR";
      }
   }
}
