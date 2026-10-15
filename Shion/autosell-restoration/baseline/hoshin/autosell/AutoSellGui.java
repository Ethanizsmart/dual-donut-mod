package hoshin.autosell;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;

public class AutoSellGui {
   public static class_437 createGui(class_437 parent) {
      return YetAnotherConfigLib.createBuilder()
         .title(class_2561.method_43470("Auto Sell Config"))
         .category(
            ConfigCategory.createBuilder()
               .name(class_2561.method_43470("Cài đặt chung"))
               .tooltip(new class_2561[]{class_2561.method_43470("Chỉnh sửa delay, bốc đồ và điều khiển tin nhắn")})
               .group(
                  OptionGroup.createBuilder()
                     .name(class_2561.method_43470("Thông số cơ bản"))
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Delay step (giây)"))
                           .binding(1.0, () -> ConfigManager.delay_step, newVal -> ConfigManager.delay_step = newVal)
                           .controller(DoubleFieldControllerBuilder::create)
                           .build()
                     )
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Delay move (tick)"))
                           .binding(2, () -> ConfigManager.delay_move, newVal -> ConfigManager.delay_move = newVal)
                           .controller(IntegerFieldControllerBuilder::create)
                           .build()
                     )
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Delay sleep (giây)"))
                           .binding(5.0, () -> ConfigManager.delay_sleep, newVal -> ConfigManager.delay_sleep = newVal)
                           .controller(DoubleFieldControllerBuilder::create)
                           .build()
                     )
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Packet size (slot)"))
                           .binding(4, () -> ConfigManager.packet_size, newVal -> ConfigManager.packet_size = Math.max(1, Math.min(36, newVal)))
                           .controller(opt -> ((IntegerFieldControllerBuilder)IntegerFieldControllerBuilder.create(opt).min(1)).max(36))
                           .build()
                     )
                     .build()
               )
               .group(
                  OptionGroup.createBuilder()
                     .name(class_2561.method_43470("Điều khiển qua tin nhắn"))
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Bật/Tắt Msg Control"))
                           .binding(false, () -> ConfigManager.msgctrl, newVal -> ConfigManager.msgctrl = newVal)
                           .controller(TickBoxControllerBuilder::create)
                           .build()
                     )
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Code điều khiển hiện tại"))
                           .binding("000000", () -> ConfigManager.msg_code, newVal -> ConfigManager.msg_code = newVal)
                           .controller(StringControllerBuilder::create)
                           .build()
                     )
                     .option(
                        ButtonOption.createBuilder()
                           .name(class_2561.method_43470("Tạo mã ngẫu nhiên"))
                           .description(
                              OptionDescription.of(
                                 new class_2561[]{class_2561.method_43470("Bấm vào đây để random một mã 6 số mới. (Yêu cầu Save để áp dụng)")}
                              )
                           )
                           .action((screen, opt) -> ConfigManager.msg_code = String.format("%06d", new Random().nextInt(1000000)))
                           .build()
                     )
                     .build()
               )
               .build()
         )
         .category(
            ConfigCategory.createBuilder()
               .name(class_2561.method_43470("Whitelist (DS Trắng)"))
               .tooltip(new class_2561[]{class_2561.method_43470("Cấu hình danh sách đồ được phép bán")})
               .group(
                  OptionGroup.createBuilder()
                     .name(class_2561.method_43470("Kích hoạt màng lọc"))
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Bật Whitelist"))
                           .description(OptionDescription.of(new class_2561[]{class_2561.method_43470("Chỉ bán những item có trong danh sách này.")}))
                           .binding(false, () -> ConfigManager.enable_whitelist, newVal -> ConfigManager.enable_whitelist = newVal)
                           .controller(TickBoxControllerBuilder::create)
                           .build()
                     )
                     .build()
               )
               .group(
                  ListOption.createBuilder()
                     .name(class_2561.method_43470("Danh sách ID Item"))
                     .description(OptionDescription.of(new class_2561[]{class_2561.method_43470("Nhập ID item, ví dụ: dirt, diamond_sword.")}))
                     .binding(new ArrayList(), () -> ConfigManager.whitelist, newVal -> ConfigManager.whitelist = newVal)
                     .controller(StringControllerBuilder::create)
                     .initial("")
                     .build()
               )
               .build()
         )
         .category(
            ConfigCategory.createBuilder()
               .name(class_2561.method_43470("Blacklist (DS Đen)"))
               .tooltip(new class_2561[]{class_2561.method_43470("Cấu hình danh sách đồ CẤM bán")})
               .group(
                  OptionGroup.createBuilder()
                     .name(class_2561.method_43470("Kích hoạt màng lọc"))
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Bật Blacklist"))
                           .description(
                              OptionDescription.of(new class_2561[]{class_2561.method_43470("TUYỆT ĐỐI KHÔNG bán những item có trong danh sách này.")})
                           )
                           .binding(false, () -> ConfigManager.enable_blacklist, newVal -> ConfigManager.enable_blacklist = newVal)
                           .controller(TickBoxControllerBuilder::create)
                           .build()
                     )
                     .build()
               )
               .group(
                  ListOption.createBuilder()
                     .name(class_2561.method_43470("Danh sách ID Item"))
                     .description(OptionDescription.of(new class_2561[]{class_2561.method_43470("Nhập ID item, ví dụ: netherite_pickaxe.")}))
                     .binding(new ArrayList(), () -> ConfigManager.blacklist, newVal -> ConfigManager.blacklist = newVal)
                     .controller(StringControllerBuilder::create)
                     .initial("")
                     .build()
               )
               .build()
         )
         .category(
            ConfigCategory.createBuilder()
               .name(class_2561.method_43470("Webhook & Thống kê"))
               .tooltip(new class_2561[]{class_2561.method_43470("Báo cáo lợi nhuận qua Discord")})
               .group(
                  OptionGroup.createBuilder()
                     .name(class_2561.method_43470("Cấu hình Webhook Discord"))
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("Chu kỳ báo cáo (Phút)"))
                           .description(
                              OptionDescription.of(new class_2561[]{class_2561.method_43470("Thời gian giữa các lần gửi báo cáo (Từ 5 đến 1000 phút).")})
                           )
                           .binding(60, () -> ConfigManager.webhook_interval, newVal -> ConfigManager.webhook_interval = Math.max(5, Math.min(1000, newVal)))
                           .controller(opt -> ((IntegerFieldControllerBuilder)IntegerFieldControllerBuilder.create(opt).min(1)).max(1000))
                           .build()
                     )
                     .option(
                        Option.createBuilder()
                           .name(class_2561.method_43470("URL Webhook"))
                           .description(OptionDescription.of(new class_2561[]{class_2561.method_43470("Dán link Discord Webhook của bạn vào đây.")}))
                           .binding("", () -> ConfigManager.webhook_url, newVal -> ConfigManager.webhook_url = newVal)
                           .controller(StringControllerBuilder::create)
                           .build()
                     )
                     .option(
                        ButtonOption.createBuilder()
                           .name(class_2561.method_43470("Test Webhook"))
                           .description(
                              OptionDescription.of(
                                 new class_2561[]{class_2561.method_43470("Gửi ngay 1 tin nhắn test lên Discord. (Nhớ Save link trước khi test)")}
                              )
                           )
                           .action((screen, opt) -> {
                              if (class_310.method_1551().field_1724 != null) {
                                 class_310.method_1551()
                                    .field_1724
                                    .method_7353(class_2561.method_43470("§a[AutoSell] Đã gửi tin nhắn test qua Webhook!"), false);
                                 WebhookManager.sendTestWebhook(class_310.method_1551().field_1724.method_5820());
                              }
                           })
                           .build()
                     )
                     .build()
               )
               .build()
         )
         .save(ConfigManager::save)
         .build()
         .generateScreen(parent);
   }
}
