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
        masterStatus.method_10852(text(DualDonutControl.isMasterEnabled() ? "§a§lON" : "§c§lOFF"));
    }

    public static class_437 create(class_437 parent) {
        var cfg = DualDonutControl.config();
        masterStatus = class_2561.method_43470(""); refreshMasterStatus();
        var builder = YetAnotherConfigLib.createBuilder()
            .title(text("§6§lDual Donut Control §7– " + (SideIntegration.ID.equals("use") ? "§ePhía Farm (Use)" : "§aPhía Bán (Sell)")))
            .category(ConfigCategory.createBuilder().name(text(SideIntegration.NAME.equals("Use") ? "Phía farm / Use" : "Phía bán / Sell"))
                .group(OptionGroup.createBuilder().name(text("Điều khiển " + SideIntegration.AUTOMATION_NAME))
                    .option(ButtonOption.createBuilder().name(text("Master (F8)")).text(masterStatus)
                        .description(OptionDescription.of(text("§6Công tắc tổng Master (F8)§r\n\nBật/Tắt toàn bộ hệ thống tự động.\n§c• TẮT:§r Ngắt toàn bộ hoạt động automation ngay lập tức để bảo vệ tài khoản.\n§a• BẬT:§r Cho phép các tính năng đã bật hoạt động bình thường.\n\n§7Lưu ý: Nút này có tác dụng ngay lập tức khi bấm. Master luôn tự động TẮT khi vào server hoặc mất kết nối.")))
                        .action((screen, option) -> { DualDonutControl.setMaster(!DualDonutControl.isMasterEnabled()); refreshMasterStatus(); }).build())
                    .option(bool("Kích hoạt " + SideIntegration.AUTOMATION_NAME,
                        "§6Kích hoạt " + SideIntegration.AUTOMATION_NAME + "§r\n\nCho phép tiến trình " + (SideIntegration.ID.equals("sell") ? "tự động bán hàng" : "tự động dùng farm") + " hoạt động khi Master đang BẬT và không có nguy hiểm xung quanh.",
                        true, () -> cfg.automationEnabled, value -> cfg.automationEnabled = value))
                    .build())
                .group(OptionGroup.createBuilder().name(text("Thông tin & Hướng dẫn"))
                    .option(button("Phím tắt thao tác nhanh", "F8 / F9",
                        "§6Danh sách phím tắt tiện ích:§r\n\n§e• F8:§r Bật / Tắt nhanh công tắc tổng Master trong khi đang chơi.\n§e• F9:§r Mở bảng menu cấu hình này bất cứ lúc nào.\n\n§aMẹo:§r Sau khi chỉnh sửa cài đặt, hãy bấm nút §f[Lưu & Thoát]§r ở góc dưới để lưu vào file.", () -> {}))
                    .option(button("Hồ sơ hiện tại", SideIntegration.NAME,
                        "§6Hồ sơ hoạt động: " + (SideIntegration.ID.equals("sell") ? "Phía Bán Hàng (Sell)" : "Phía Dùng Farm (Use)") + "§r\n\n"
                        + (SideIntegration.ID.equals("sell")
                            ? "Tích hợp sẵn: AutoSell, Vex Recovery, AutoHome, Auto Disconnect, AntiPlayer và Discord Webhook."
                            : "Tích hợp sẵn: AutoUse (Hold RMB + AutoSword), Vex Recovery, AutoHome, Auto Disconnect, AntiPlayer và Discord Webhook."), () -> {}))
                    .build())
                .build());

        for (var category : SideIntegration.categories()) builder.category(category);

        builder.category(ConfigCategory.createBuilder().name(text("Hồi phục / Vex"))
            .group(OptionGroup.createBuilder().name(text("Vex Recovery"))
                .option(bool("Tự hồi sinh và về home", "§6Tự động hồi sinh & về home§r\n\nKhi nhân vật bị chết, bot sẽ tự bấm Hồi Sinh và sau đó gửi lệnh dịch chuyển về home an toàn.\n§7Hoạt động độc lập với trạng thái " + SideIntegration.AUTOMATION_NAME + ".", true, () -> cfg.recoveryEnabled, value -> cfg.recoveryEnabled = value))
                .option(bool("Chờ Vex biến mất", "§6Chờ an toàn không còn Vex§r\n\nQuét an toàn ít nhất 5 lần liên tục và không còn Vex xung quanh trong 1 giây trước khi bấm Hồi Sinh.\n§7Giúp tránh tình trạng vừa hồi sinh đã bị Vex tấn công chết lặp lại.", true, () -> cfg.vexCheck, value -> cfg.vexCheck = value))
                .option(integer("Chờ hồi sinh (ms)", "Thời gian chờ (mili-giây) tại màn hình chết trước khi thực hiện bấm Hồi Sinh (Mặc định: 2000 ms = 2 giây).", 2000, 0, 30000, () -> cfg.respawnDelayMs, value -> cfg.respawnDelayMs = value))
                .option(integer("Chờ sau hồi sinh (ms)", "Thời gian chờ (mili-giây) sau khi vừa hồi sinh xong để nạp thế giới trước khi gõ lệnh home (Mặc định: 1500 ms).", 1500, 0, 30000, () -> cfg.homeDelayMs, value -> cfg.homeDelayMs = value))
                .option(string("Lệnh home hồi sinh", "Lệnh dịch chuyển về căn cứ an toàn sau khi hồi sinh (Ví dụ: home 1, home an_toan).\n§7Không cần gõ dấu gạch chéo / ở đầu.", "home 1", () -> cfg.recoveryHomeCommand, value -> cfg.recoveryHomeCommand = value))
                .option(integer("Chờ ổn định sau home (ms)", "Thời gian bảo vệ (mili-giây) sau khi về home, tạm hoãn các hoạt động khác để vị trí đứng ổn định (Mặc định: 8000 ms).", 8000, 0, 30000, () -> cfg.postHomeGuardMs, value -> cfg.postHomeGuardMs = value))
                .build()).build());

        builder.category(ConfigCategory.createBuilder().name(text("Giữ mốc / Home"))
            .group(OptionGroup.createBuilder().name(text("AutoHome"))
                .option(bool("Bật AutoHome", "§6Tự động giữ mốc và hướng nhìn (AutoHome)§r\n\nTheo dõi tọa độ X/Z và hướng nhìn đã lưu. Nếu lệch vị trí hoặc lệch hướng quá dung sai, hệ thống sẽ đếm ngược và gửi lệnh home.\n§7Hướng nhìn chỉ được kiểm tra sau khi bấm [Lưu hướng nhìn].", false, () -> cfg.homeEnabled, value -> cfg.homeEnabled = value))
                .option(string("Lệnh cần gửi", "Lệnh home được gửi khi nhân vật lệch vị trí hoặc hướng nhìn đã lưu (Ví dụ: home 2, home farm).\n§7Không cần gõ dấu gạch chéo / ở đầu.", "home", () -> cfg.homeCommand, value -> cfg.homeCommand = value))
                .option(decimal("Chờ khi lệch mốc (phút)", "Thời gian chờ (tính bằng phút) sau khi phát hiện lệch mốc trước khi bắt đầu gửi lệnh về home lần đầu (Mặc định: 1.0 phút).", 1, 0.05, 1440, () -> cfg.homeDelayMinutes, value -> cfg.homeDelayMinutes = value))
                .option(decimal("Chu kỳ gửi lại (phút)", "Khoảng thời gian (phút) lặp lại gửi lệnh home nếu vị trí hoặc hướng nhìn vẫn lệch mốc (Mặc định: 0.5 phút).", 0.5, 0.05, 1440, () -> cfg.homeRepeatMinutes, value -> cfg.homeRepeatMinutes = value))
                .option(decimal("Bán kính cho phép (block)", "Bán kính dung sai (block trên mặt phẳng X/Z). Chỉ kích hoạt home khi khoảng cách lớn hơn giá trị này (Mặc định: 2.0 block).", 2, 0.5, 100, () -> cfg.homeTolerance, value -> cfg.homeTolerance = value))
                .option(decimal("Dung sai hướng nhìn (độ)", "Độ lệch tối đa cho mỗi góc nhìn ngang (yaw) và dọc (pitch). Khi một góc vượt dung sai, AutoHome đếm ngược dù nhân vật vẫn đứng đúng X/Z (Mặc định: 5 độ).", 5, 0.1, 180, () -> cfg.homeFacingTolerance, value -> cfg.homeFacingTolerance = value))
                .option(bool("Hiển thị hotbar", "Hiển thị đếm ngược thời gian gửi lệnh home hoặc trạng thái an toàn ngay trên thanh Hotbar giữa màn hình.", true, () -> cfg.showHotbar, value -> cfg.showHotbar = value))
                .option(button("Đặt vị trí hiện tại làm mốc", "Lưu mốc",
                    cfg.hasAnchor ? String.format(Locale.ROOT, "§6Mốc tọa độ đã lưu:§r\nX = %.1f, Z = %.1f\n\n§aBấm nút để cập nhật vị trí đang đứng hiện tại làm mốc mới.", cfg.anchorX, cfg.anchorZ) : "§cChưa có mốc tọa độ nào được lưu.§r\n\n§aBấm nút để đặt vị trí đang đứng hiện tại làm mốc an toàn.",
                    () -> HomeAnchor.setAnchor(class_310.method_1551())))
                .option(button("Đặt hướng nhìn hiện tại làm mốc", "Lưu hướng nhìn",
                    cfg.hasFacingAnchor ? String.format(Locale.ROOT, "§6Hướng nhìn đã lưu:§r\nYaw = %.1f°, Pitch = %.1f°\n\n§aBấm để cập nhật hướng nhìn. Mốc X/Z được lưu bằng nút [Lưu mốc] riêng.", cfg.anchorYaw, cfg.anchorPitch) : "§7Chưa lưu hướng nhìn, AutoHome hiện chỉ kiểm tra X/Z.§r\n\n§aNhìn theo hướng farm mong muốn rồi bấm để lưu cả góc ngang và dọc. Mốc X/Z được lưu bằng nút [Lưu mốc] riêng.",
                    () -> HomeAnchor.setFacingAnchor(class_310.method_1551())))
                .build()).build());

        builder.category(ConfigCategory.createBuilder().name(text("Phát hiện người chơi"))
            .group(OptionGroup.createBuilder().name(text("Auto Disconnect (Tự ngắt kết nối)"))
                .option(bool("Tự ngắt kết nối", "§6Tự động ngắt kết nối khi có người lạ§r\n\nKhi phát hiện người chơi khác không nằm trong Whitelist: Dừng ngay automation, ghi tên vào lịch sử và thoát khỏi server.\n§eƯu tiên cao hơn AntiPlayer nếu bật đồng thời cả hai.", false, () -> cfg.autoDisconnect, value -> cfg.autoDisconnect = value))
                .option(decimal("Phạm vi ngắt kết nối (block)", "Khoảng cách 3D (block) để phát hiện người chơi khác và kích hoạt tự ngắt kết nối (1 - 128 block, mặc định: 30.0).", 30, 1, 128, () -> cfg.disconnectRadius, value -> cfg.disconnectRadius = value))
                .build())
            .group(OptionGroup.createBuilder().name(text("AntiPlayer – Tự vệ & Tự sát"))
                .option(bool("Bật AntiPlayer", "§6Chống người lạ (AntiPlayer)§r\n\nKhi phát hiện người lạ: Dừng automation, thử ném Ender Pearl tay trái 2 lần để tẩu thoát, sau đó gõ /kill và tự động bấm xác nhận trong menu.\n§7Sau khi chết, Vex Recovery sẽ tiếp quản để đưa về home an toàn.", false, () -> cfg.antiPlayer, value -> cfg.antiPlayer = value))
                .option(decimal("Phạm vi AntiPlayer (block)", "Khoảng cách 3D (block) để phát hiện người lạ và kích hoạt chuỗi hành động AntiPlayer (1 - 128 block, mặc định: 5.0).", 5, 1, 128, () -> cfg.antiPlayerRadius, value -> cfg.antiPlayerRadius = value))
                .option(integer("Chờ vùng an toàn trước tiếp tục (ms)", "Thời gian (mili-giây) khu vực phải hoàn toàn sạch bóng người lạ trước khi cho phép hệ thống tiếp tục hoạt động (Mặc định: 1000 ms).", 1000, 0, 30000, () -> cfg.clearDelayMs, value -> cfg.clearDelayMs = value))
                .option(string("Tên GUI xác nhận /kill", "Từ khóa nhận diện tiêu đề menu xác nhận tự sát của server (phân cách bằng dấu |).\n§7Bot chỉ bấm nút xác nhận nếu GUI có tên chứa từ khóa này.", "kill|suicide|tự sát", () -> cfg.killConfirmTitles, value -> cfg.killConfirmTitles = value))
                .build())
            .group(strings("Người tin cậy / Whitelist", "Danh sách tên người chơi tin cậy (bạn bè, tài khoản farm phụ cùng đứng gần).\n§7Cả Auto Disconnect và AntiPlayer đều sẽ bỏ qua các người chơi này (Không phân biệt hoa/thường).\n§7Bấm [+] để thêm tên, [x] để xóa.", () -> cfg.trustedPlayers, value -> cfg.trustedPlayers = value))
            .group(OptionGroup.createBuilder().name(text("Lịch sử phát hiện người chơi"))
                .option(button("Người đã phát hiện (mới nhất trước)", "Xem", cfg.detectionLog.isEmpty() ? "§7Chưa có người chơi nào được phát hiện trong phiên này." : "§6Danh sách người chơi đã phát hiện gần nhất:§r\n" + String.join(", ", cfg.detectionLog), () -> {}))
                .option(button("Xóa lịch sử", "Xóa", "Bấm để xóa sạch toàn bộ danh sách lịch sử người chơi đã phát hiện (tối đa 50 tên gần nhất).", () -> { cfg.detectionLog.clear(); cfg.save(); }))
                .build()).build());

        var webhook = OptionGroup.createBuilder().name(text("Discord Webhook Cảnh báo"))
            .option(string("URL Webhook", "Đường dẫn Discord Webhook để nhận thông báo cảnh báo người lạ" + (SideIntegration.ID.equals("sell") ? " và báo cáo lợi nhuận bán hàng định kỳ." : " và cảnh báo thiếu tiền mua kiếm."), "", SideIntegration::webhookUrl, SideIntegration::setWebhookUrl))
            .option(bool("Gửi cảnh báo người chơi", "Tự động gửi thông báo khẩn cấp lên Discord ngay khi phát hiện người lạ tiếp cận (Nếu URL trống sẽ không gửi).", true, () -> cfg.protectionAlerts, value -> cfg.protectionAlerts = value));
        for (var option : SideIntegration.webhookOptions()) webhook.option(option);
        webhook.option(button("Test Webhook", "Gửi thử", "Bấm để gửi ngay một tin nhắn thử nghiệm lên Discord Webhook bằng URL đã lưu để kiểm tra kết nối.", () -> Notifications.send("Test Donut " + SideIntegration.NAME, "Kết nối webhook của Dual Donut Control " + SideIntegration.NAME + " hoạt động tốt!")));

        return builder.category(ConfigCategory.createBuilder().name(text("Webhook")).group(webhook.build()).build())
            .save(DualDonutControl::saveAll).build().generateScreen(parent);
    }
}
