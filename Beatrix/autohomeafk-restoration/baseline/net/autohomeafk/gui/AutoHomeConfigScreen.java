package net.autohomeafk.gui;

import java.util.Locale;
import net.autohomeafk.config.ConfigManager;
import net.autohomeafk.config.ModConfig;
import net.autohomeafk.core.AutoHomeEngine;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public class AutoHomeConfigScreen extends class_437 {
   private final class_437 parent;
   private class_342 commandField;
   private class_342 delayField;
   private class_342 repeatField;
   private class_342 radiusField;
   private class_4185 statusToggleBtn;
   private class_4185 hotbarToggleBtn;
   private class_4185 setAnchorBtn;

   public AutoHomeConfigScreen(class_437 parent) {
      super(class_2561.method_43470("Cài Đặt Auto Home AFK"));
      this.parent = parent;
   }

   protected void method_25426() {
      ModConfig config = ConfigManager.get();
      int centerX = this.field_22789 / 2;
      int startY = 24;
      int rowHeight = 23;
      this.statusToggleBtn = class_4185.method_46430(this.getStatusText(config.enabled), btn -> {
         config.enabled = !config.enabled;
         btn.method_25355(this.getStatusText(config.enabled));
         if (config.enabled && this.field_22787 != null && this.field_22787.field_1724 != null) {
            config.anchorX = this.field_22787.field_1724.method_23317();
            config.anchorZ = this.field_22787.field_1724.method_23321();
            config.hasAnchor = true;
            AutoHomeEngine.resetTimers(config);
         }

         ConfigManager.save();
      }).method_46434(centerX - 100, startY, 200, 20).method_46431();
      this.method_37063(this.statusToggleBtn);
      this.commandField = new class_342(this.field_22793, centerX + 10, startY + rowHeight, 130, 20, class_2561.method_43470("Lệnh"));
      this.commandField.method_1852(config.command != null ? config.command : "home");
      this.method_37063(this.commandField);
      this.delayField = new class_342(this.field_22793, centerX + 10, startY + rowHeight * 2, 130, 20, class_2561.method_43470("Chờ ban đầu"));
      this.delayField.method_1852(String.format(Locale.US, "%.1f", config.delayMinutes));
      this.method_37063(this.delayField);
      this.repeatField = new class_342(this.field_22793, centerX + 10, startY + rowHeight * 3, 130, 20, class_2561.method_43470("Chu kỳ lặp"));
      this.repeatField.method_1852(String.format(Locale.US, "%.1f", config.repeatIntervalMinutes));
      this.method_37063(this.repeatField);
      this.radiusField = new class_342(this.field_22793, centerX + 10, startY + rowHeight * 4, 130, 20, class_2561.method_43470("Bán kính"));
      this.radiusField.method_1852(String.format(Locale.US, "%.1f", config.toleranceRadius));
      this.method_37063(this.radiusField);
      this.hotbarToggleBtn = class_4185.method_46430(this.getHotbarText(config.showHotbar), btn -> {
         config.showHotbar = !config.showHotbar;
         btn.method_25355(this.getHotbarText(config.showHotbar));
         ConfigManager.save();
      }).method_46434(centerX - 120, startY + rowHeight * 5, 240, 20).method_46431();
      this.method_37063(this.hotbarToggleBtn);
      this.setAnchorBtn = class_4185.method_46430(class_2561.method_43470("§e[+] Đặt vị trí hiện tại làm mốc"), btn -> {
         if (this.field_22787 != null) {
            AutoHomeEngine.setAnchorToCurrentPos(this.field_22787);
         }
      }).method_46434(centerX - 120, startY + rowHeight * 6, 240, 20).method_46431();
      this.method_37063(this.setAnchorBtn);
      this.method_37063(class_4185.method_46430(class_2561.method_43470("Lưu & Đóng"), btn -> {
         this.saveInputs();
         this.method_25419();
      }).method_46434(centerX - 75, this.field_22790 - 26, 150, 20).method_46431());
   }

   private void saveInputs() {
      ModConfig config = ConfigManager.get();
      if (this.commandField != null) {
         String cmd = this.commandField.method_1882().trim();
         if (!cmd.isEmpty()) {
            config.command = cmd;
         }
      }

      if (this.delayField != null) {
         try {
            double val = Double.parseDouble(this.delayField.method_1882().trim().replace(',', '.'));
            config.delayMinutes = Math.max(0.05, val);
         } catch (NumberFormatException var6) {
         }
      }

      if (this.repeatField != null) {
         try {
            double val = Double.parseDouble(this.repeatField.method_1882().trim().replace(',', '.'));
            config.repeatIntervalMinutes = Math.max(0.05, val);
         } catch (NumberFormatException var5) {
         }
      }

      if (this.radiusField != null) {
         try {
            double val = Double.parseDouble(this.radiusField.method_1882().trim().replace(',', '.'));
            config.toleranceRadius = Math.max(0.5, Math.min(100.0, val));
         } catch (NumberFormatException var4) {
         }
      }

      AutoHomeEngine.resetTimers(config);
      ConfigManager.save();
   }

   private class_2561 getStatusText(boolean enabled) {
      return class_2561.method_43470("Trạng Thái Mod: " + (enabled ? "§a[ ĐANG BẬT ]" : "§c[ ĐANG TẮT ]"));
   }

   private class_2561 getHotbarText(boolean show) {
      return class_2561.method_43470("Thông Báo Hotbar: " + (show ? "§a[ ĐANG BẬT ]" : "§c[ ĐANG TẮT ]"));
   }

   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      super.method_25394(context, mouseX, mouseY, delta);
      int centerX = this.field_22789 / 2;
      int startY = 24;
      int rowHeight = 23;
      context.method_27534(this.field_22793, this.field_22785, centerX, 8, -1);
      context.method_25303(this.field_22793, "Lệnh cần gửi:", centerX - 140, startY + rowHeight + 6, -2039584);
      context.method_25303(this.field_22793, "Thời gian chờ (Phút):", centerX - 140, startY + rowHeight * 2 + 6, -2039584);
      context.method_25303(this.field_22793, "Chu kỳ gửi lại (Phút):", centerX - 140, startY + rowHeight * 3 + 6, -2039584);
      context.method_25303(this.field_22793, "Bán kính cho phép (m):", centerX - 140, startY + rowHeight * 4 + 6, -2039584);
      ModConfig config = ConfigManager.get();
      int infoY = startY + rowHeight * 7 + 4;
      if (config.hasAnchor) {
         String anchorStr = String.format(Locale.US, "§7Mốc hiện tại: §aX: %.1f §7| §aZ: %.1f", config.anchorX, config.anchorZ);
         context.method_27534(this.field_22793, class_2561.method_43470(anchorStr), centerX, infoY, -1);
      } else {
         context.method_27534(this.field_22793, class_2561.method_43470("§7Mốc hiện tại: §cChưa đặt (Sẽ tự lấy khi BẬT mod)"), centerX, infoY, -1);
      }

      context.method_27534(
         this.field_22793,
         class_2561.method_43470("§e► Cơ chế: Ra khỏi mốc -> Đợi delay -> Gửi lệnh liên tục mỗi chu kỳ tới khi về"),
         centerX,
         infoY + 12,
         16777130
      );
      context.method_27534(
         this.field_22793, class_2561.method_43470("§8Phím tắt mặc định: H (Bật/Tắt) | O (Mở Menu) - Đổi được trong Controls"), centerX, infoY + 24, -5592406
      );
   }

   public boolean method_25404(class_11908 input) {
      if (this.commandField != null && this.commandField.method_25404(input)) {
         return true;
      } else if (this.delayField != null && this.delayField.method_25404(input)) {
         return true;
      } else if (this.repeatField != null && this.repeatField.method_25404(input)) {
         return true;
      } else {
         return this.radiusField != null && this.radiusField.method_25404(input) ? true : super.method_25404(input);
      }
   }

   public boolean method_25400(class_11905 input) {
      if (this.commandField != null && this.commandField.method_25400(input)) {
         return true;
      } else if (this.delayField != null && this.delayField.method_25400(input)) {
         return true;
      } else if (this.repeatField != null && this.repeatField.method_25400(input)) {
         return true;
      } else {
         return this.radiusField != null && this.radiusField.method_25400(input) ? true : super.method_25400(input);
      }
   }

   public void method_25419() {
      this.saveInputs();
      if (this.field_22787 != null) {
         this.field_22787.method_1507(this.parent);
      }
   }
}
