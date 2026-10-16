package dev.dualdonut.bounty;

import static dev.dualdonut.GuiOptions.*;
import dev.dualdonut.DualDonutControl;
import dev.dualdonut.SideIntegration;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.OptionDescription;
import hoshin.autosell.AutoSellLogic;
import java.text.NumberFormat;
import java.util.Locale;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_2561;
import net.minecraft.class_5250;

/** Sell-only adapter. Owns only the response to its own /bounty add request. */
public final class BountyRuntime {
    private static final BountyEngine ENGINE = new BountyEngine();
    private static final MinecraftPort PORT = new MinecraftPort();
    private static boolean registered, pausedSell;
    private static class_310 client;
    private static long now, claimUntil, closeNotBefore;
    private static class_1703 priorHandler, bountyHandler;
    private static class_465 bountyScreen;
    private static int confirmSlot = -1;
    private static class_5250 statusDescription;
    private BountyRuntime() {}

    public static void tick(class_310 value) {
        if (DualDonutControl.config() == null) return;
        client = value; now = System.currentTimeMillis();
        if (!registered) {
            registered = true;
            // Only server GAME messages, never player chat or actionbar text.
            ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
                if (!overlay && PORT.ready() && BountyConfig.get().enabled) ENGINE.message(message.getString(), System.currentTimeMillis());
            });
        }
        try { ENGINE.tick(BountyConfig.get().snapshot(), now, PORT); }
        catch (RuntimeException ex) {
            DualDonutControl.LOGGER.error("Auto Bounty action failed; waiting before retry", ex);
            ENGINE.retry("Thao tác Minecraft thất bại", now);
        }
        if (!busy() && pausedSell) { AutoSellLogic.resumeAfterBounty(); pausedSell = false; }
        refreshStatus();
    }
    public static boolean busy() { return ENGINE.blocking() || bountyHandler != null; }
    public static boolean blocksSell() { return busy(); }
    public static void save() { BountyConfig.get().save(); }
    private static boolean owns() {
        return client != null && client.field_1724 != null && bountyHandler != null
            && client.field_1755 == bountyScreen && bountyScreen.method_17577() == bountyHandler
            && client.field_1724.field_7512 == bountyHandler;
    }
    private static void clearLease() { bountyHandler = null; bountyScreen = null; confirmSlot = -1; claimUntil = closeNotBefore = 0; }

    private static int findButton(class_465 screen) {
        if (!BountyRules.confirmTitle(screen.method_25440().getString()) || client.field_1724 == null) return -1;
        var handler = screen.method_17577();
        if (client.field_1724.field_7512 != handler || !handler.method_34255().method_7960()) return -1;
        for (var slot : handler.field_7761) {
            if (slot.field_7871 == client.field_1724.method_31548() || !slot.method_7681()) continue;
            var stack = slot.method_7677();
            // Fail closed when tooltip lookup fails; no slot-number or item-type fallback.
            var tooltip = class_437.method_25408(client, stack).stream().map(line -> line.getString()).toList();
            if (BountyRules.confirmButton(stack.method_7964().getString(), tooltip)) return slot.field_7874;
        }
        return -1;
    }
    private static final class MinecraftPort implements BountyEngine.Port {
        @Override public boolean ready() {
            return client != null && DualDonutControl.isMasterEnabled() && client.field_1724 != null && client.field_1687 != null
                && client.method_1562() != null && !DualDonutControl.sharedSellPause();
        }
        @Override public boolean acquire() { return ready() && SideIntegration.acquireForBounty(client); }
        @Override public void command(String command) {
            if (!ready() || client.field_1755 != null) throw new IllegalStateException("Bounty lost safe command access");
            AutoSellLogic.pauseForBounty(); pausedSell = true;
            if (command.startsWith("bounty add ")) {
                priorHandler = client.field_1724.field_7512;
                claimUntil = now + 5000;
            }
            client.method_1562().method_45730(command);
        }
        @Override public boolean findConfirm() {
            if (!ready()) return false;
            if (bountyHandler != null) return owns() && findButton(bountyScreen) == confirmSlot;
            if (now >= claimUntil || !(client.field_1755 instanceof class_465 screen) || screen.method_17577() == priorHandler) return false;
            int slot = findButton(screen);
            if (slot < 0) return false;
            bountyHandler = screen.method_17577(); bountyScreen = screen; confirmSlot = slot;
            return true;
        }
        @Override public boolean clickConfirm() {
            if (!ready() || !owns() || client.field_1761 == null || findButton(bountyScreen) != confirmSlot) return false;
            client.field_1761.method_2906(bountyHandler.field_7763, confirmSlot, 0, class_1713.field_7790, client.field_1724);
            closeNotBefore = now + 250;
            return true;
        }
        @Override public boolean release() {
            if (!owns()) { clearLease(); return true; }
            if (now < closeNotBefore || !bountyHandler.method_34255().method_7960()) return false;
            client.field_1724.method_7346();
            clearLease();
            return true;
        }
    }

    public static String statusLine() {
        String balance = ENGINE.balance() < 0 ? "-" : "$" + NumberFormat.getIntegerInstance(Locale.US).format(ENGINE.balance());
        long seconds = ENGINE.remainingSeconds(System.currentTimeMillis());
        return String.format(Locale.ROOT, "[Auto Bounty] %s | Số dư %s | Lần gần nhất %s | Chờ %02d:%02d",
            ENGINE.status(), balance, ENGINE.lastAdded(), seconds / 60, seconds % 60);
    }
    private static void refreshStatus() {
        if (statusDescription == null) return;
        statusDescription.method_10855().clear();
        statusDescription.method_10852(text(statusLine().replace(" | ", "\n")
            + "\nĐang đặt: " + ENGINE.amount() + " -> " + ENGINE.target()));
    }
    public static ConfigCategory category() {
        var cfg = BountyConfig.get();
        statusDescription = class_2561.method_43470(""); refreshStatus();
        return ConfigCategory.createBuilder().name(text("Auto Bounty"))
            .group(OptionGroup.createBuilder().name(text("Tự động đặt bounty"))
                .option(bool("Bật Auto Bounty", "Đọc /balance rồi đặt bounty cho người nhận.\n§eDùng toàn bộ số dư được làm tròn xuống theo 0,1 triệu; chỉ chạy từ $1M.\n§7Ví dụ $1.299.999 -> 1.2m. Mặc định TẮT, chỉ chạy khi Master ON.\nHoạt động độc lập với công tắc AutoSell; không có trong bản Farm.", false, () -> cfg.enabled, v -> cfg.enabled = v))
                .option(string("Tên người nhận bounty", "Tên tài khoản đích cho /bounty add <tên> <số tiền>.\nChỉ chấp nhận chữ, số và dấu gạch dưới; không gửi lệnh nếu tên trống hoặc không hợp lệ.", "", () -> cfg.target, v -> cfg.target = v))
                .option(decimal("Chu kỳ bounty (phút)", "Khoảng chờ sau khi thành công hoặc số dư dưới $1M.\nLần đầu chạy ngay khi bật Master và đủ điều kiện. Mặc định 7 phút.", 7, .1, 1440, () -> cfg.intervalMinutes, v -> cfg.intervalMinutes = v))
                .option(decimal("Chờ thử lại (giây)", "Thời gian nghỉ khi timeout /balance, thiếu GUI hoặc chưa nhận thông báo thành công.\nMỗi lần thử lại đều đọc số dư mới; trong lúc nghỉ có thể tiếp tục bán. Mặc định 7 giây.", 7, .1, 3600, () -> cfg.retrySeconds, v -> cfg.retrySeconds = v))
                .option(decimal("Chờ trước lệnh bounty (giây)", "Chờ sau khi nhận số dư trước lệnh /bounty add. Mặc định 0,5 giây.", .5, 0, 60, () -> cfg.commandSeconds, v -> cfg.commandSeconds = v))
                .option(decimal("Chờ trước xác nhận (giây)", "Chờ sau khi tìm thấy nút Confirm trong GUI Confirm Bounty.\nKiểm tra lại đúng handler, slot, tooltip và cursor ngay trước khi click. Mặc định 1 giây.", 1, 0, 60, () -> cfg.confirmSeconds, v -> cfg.confirmSeconds = v))
                .build())
            .group(OptionGroup.createBuilder().name(text("Trạng thái & phối hợp"))
                .option(ButtonOption.createBuilder().name(text("Trạng thái Auto Bounty")).text(text("Xem trạng thái"))
                    .description(OptionDescription.of(statusDescription))
                    .action((screen, option) -> { refreshStatus(); DualDonutControl.message(statusLine()); }).build())
                .option(button("An toàn GUI", "Thông tin", "Chờ AutoSell hoàn tất chuyển/xác nhận item rồi mới nhường menu.\nChỉ xác nhận đúng phản hồi Confirm Bounty của yêu cầu đang chờ.\nMaster OFF, recovery, phát hiện người lạ, menu cài đặt và Home đều được tôn trọng.\nClick Confirm chỉ là lần thử; chờ thông báo thành công đúng tên và số tiền.", () -> {}))
                .build()).build();
    }
}
