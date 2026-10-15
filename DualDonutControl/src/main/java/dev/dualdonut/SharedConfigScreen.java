package dev.dualdonut;

import static dev.dualdonut.GuiOptions.*;
import dev.dualdonut.runtime.HomeAnchor;
import dev.dualdonut.runtime.Notifications;
import dev.isxander.yacl3.api.*;
import java.util.Locale;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_5250;

public final class SharedConfigScreen {
    private static class_5250 masterStatus;
    private SharedConfigScreen() {}
    public static void refreshMasterStatus() {
        if (masterStatus == null) return;
        masterStatus.method_10855().clear();
        masterStatus.method_10852(text(DualDonutControl.isMasterEnabled() ? "ON" : "OFF"));
    }
    public static class_437 create(class_437 parent) {
        var cfg = DualDonutControl.config();
        masterStatus = class_2561.method_43470(""); refreshMasterStatus();
        var builder = YetAnotherConfigLib.createBuilder().title(text("Dual Donut Control – " + SideIntegration.NAME))
            .category(ConfigCategory.createBuilder().name(text(SideIntegration.NAME.equals("Use") ? "Phía farm / Use" : "Phía bán / Sell"))
                .group(OptionGroup.createBuilder().name(text("Điều khiển " + SideIntegration.AUTOMATION_NAME))
                    .option(ButtonOption.createBuilder().name(text("Master (F8)")).text(masterStatus)
                        .description(OptionDescription.of(text("Áp dụng ngay. Master OFF khi mở game hoặc ngắt kết nối. Các lựa chọn khác cần Save.")))
                        .action((screen, option) -> { DualDonutControl.setMaster(!DualDonutControl.isMasterEnabled()); refreshMasterStatus(); }).build())
                    .option(bool("Kích hoạt " + SideIntegration.AUTOMATION_NAME, "Chạy khi master ON và các bộ bảo vệ đã cho phép.", true,
                        () -> cfg.automationEnabled, value -> cfg.automationEnabled = value))
                    .build()).build());
        for (var category : SideIntegration.categories()) builder.category(category);
        builder.category(ConfigCategory.createBuilder().name(text("Hồi phục / Vex"))
            .group(OptionGroup.createBuilder().name(text("Recovery của Control " + SideIntegration.NAME))
                .option(bool("Tự hồi sinh và về home", "Chạy độc lập với switch " + SideIntegration.AUTOMATION_NAME + ".", true, () -> cfg.recoveryEnabled, value -> cfg.recoveryEnabled = value))
                .option(bool("Chờ Vex biến mất", "Quét thành công ít nhất 5 lần, không còn Vex ổn định ít nhất 1 giây trước hồi sinh.", true, () -> cfg.vexCheck, value -> cfg.vexCheck = value))
                .option(integer("Chờ hồi sinh (ms)", 2000, 0, 30000, () -> cfg.respawnDelayMs, value -> cfg.respawnDelayMs = value))
                .option(integer("Chờ sau hồi sinh (ms)", 1500, 0, 30000, () -> cfg.homeDelayMs, value -> cfg.homeDelayMs = value))
                .option(string("Lệnh home hồi sinh", "Không cần dấu /.", "home 1", () -> cfg.recoveryHomeCommand, value -> cfg.recoveryHomeCommand = value))
                .option(integer("Chờ ổn định sau home (ms)", 8000, 0, 30000, () -> cfg.postHomeGuardMs, value -> cfg.postHomeGuardMs = value))
                .build()).build());
        builder.category(ConfigCategory.createBuilder().name(text("Giữ mốc / Home"))
            .group(OptionGroup.createBuilder().name(text("Home tích hợp – không cần AutoHome riêng"))
                .option(bool("Bật giữ mốc X/Z", "Giữ mốc đã lưu. Khi lệch mốc, đợi rồi gửi lệnh theo chu kỳ.", false, () -> cfg.homeEnabled, value -> cfg.homeEnabled = value))
                .option(string("Lệnh cần gửi", "Gửi khi lệch mốc quá bán kính cho phép.", "home", () -> cfg.homeCommand, value -> cfg.homeCommand = value))
                .option(decimal("Chờ khi lệch mốc (phút)", 1, 0.05, 1440, () -> cfg.homeDelayMinutes, value -> cfg.homeDelayMinutes = value))
                .option(decimal("Chu kỳ gửi lại (phút)", 0.5, 0.05, 1440, () -> cfg.homeRepeatMinutes, value -> cfg.homeRepeatMinutes = value))
                .option(decimal("Bán kính cho phép (block)", 2, 0.5, 100, () -> cfg.homeTolerance, value -> cfg.homeTolerance = value))
                .option(bool("Hiển thị hotbar", "Hiện trạng thái an toàn hoặc thời gian chờ home.", true, () -> cfg.showHotbar, value -> cfg.showHotbar = value))
                .option(button("Đặt vị trí hiện tại làm mốc", cfg.hasAnchor ? String.format(Locale.ROOT, "Mốc X %.1f, Z %.1f. Nút áp dụng ngay.", cfg.anchorX, cfg.anchorZ) : "Chưa có mốc; tự lấy khi bật giữ mốc lần đầu.", () -> HomeAnchor.setAnchor(class_310.method_1551())))
                .build()).build());
        builder.category(ConfigCategory.createBuilder().name(text("Phát hiện người chơi"))
            .group(OptionGroup.createBuilder().name(text("Auto Disconnect"))
                .option(bool("Tự ngắt kết nối", "Gặp người lạ: dừng automation, ghi lịch sử và thoát server. Ưu tiên cao hơn AntiPlayer.", false, () -> cfg.autoDisconnect, value -> cfg.autoDisconnect = value))
                .option(decimal("Phạm vi ngắt kết nối (block)", 30, 1, 128, () -> cfg.disconnectRadius, value -> cfg.disconnectRadius = value))
                .build())
            .group(OptionGroup.createBuilder().name(text("AntiPlayer – Chống người lạ"))
                .option(bool("Bật AntiPlayer", "Gặp người lạ: dừng automation, thử ném Ender Pearl tay trái 2 lần, chờ rồi /kill và xác nhận. Chỉ tiếp tục khi không còn người lạ.", false, () -> cfg.antiPlayer, value -> cfg.antiPlayer = value))
                .option(decimal("Phạm vi AntiPlayer (block)", 5, 1, 128, () -> cfg.antiPlayerRadius, value -> cfg.antiPlayerRadius = value))
                .option(integer("Chờ vùng an toàn trước tiếp tục (ms)", 1000, 0, 30000, () -> cfg.clearDelayMs, value -> cfg.clearDelayMs = value))
                .option(string("Tên GUI xác nhận /kill", "Chỉ click nút xác nhận trong GUI có tên chứa từ khóa. Ngăn cách bằng |; đổi theo server.", "kill|suicide|tự sát", () -> cfg.killConfirmTitles, value -> cfg.killConfirmTitles = value))
                .build())
            .group(strings("Người tin cậy / Whitelist", "Bỏ qua các tên này ở cả hai chế độ. Không phân biệt chữ hoa/thường.", () -> cfg.trustedPlayers, value -> cfg.trustedPlayers = value))
            .group(OptionGroup.createBuilder().name(text("Lịch sử"))
                .option(button("Người đã phát hiện (mới nhất trước)", cfg.detectionLog.isEmpty() ? "Chưa có người chơi được phát hiện." : String.join(", ", cfg.detectionLog), () -> {}))
                .option(button("Xóa lịch sử", "Xóa ngay danh sách tối đa 50 tên đã lưu.", () -> { cfg.detectionLog.clear(); cfg.save(); }))
                .build()).build());
        var webhook = OptionGroup.createBuilder().name(text("Webhook " + SideIntegration.NAME))
            .option(string("URL Webhook", "Dùng cho cảnh báo người chơi" + (SideIntegration.ID.equals("sell") ? " và báo cáo AutoSell." : " và cảnh báo thiếu tiền mua kiếm."), "", SideIntegration::webhookUrl, SideIntegration::setWebhookUrl))
            .option(bool("Gửi cảnh báo người chơi", "Gửi khi bắt đầu bảo vệ; URL trống thì không gửi.", true, () -> cfg.protectionAlerts, value -> cfg.protectionAlerts = value));
        for (var option : SideIntegration.webhookOptions()) webhook.option(option);
        webhook.option(button("Test Webhook", "Gửi test bằng URL đã Save.", () -> Notifications.send("Test Donut " + SideIntegration.NAME, "Kết nối webhook hoạt động.")));
        return builder.category(ConfigCategory.createBuilder().name(text("Webhook")).group(webhook.build()).build())
            .save(DualDonutControl::saveAll).build().generateScreen(parent);
    }
}
