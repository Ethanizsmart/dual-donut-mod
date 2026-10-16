package dev.dualdonut;

import static dev.dualdonut.GuiOptions.*;
import dev.dualdonut.runtime.SaleScreenRules;
import dev.dualdonut.bounty.BountyRuntime;
import dev.isxander.yacl3.api.*;
import hoshin.autosell.AutoSellLogic;
import hoshin.autosell.Autosell;
import hoshin.autosell.ConfigManager;
import hoshin.autosell.SellCommandPacing;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.class_1703;
import net.minecraft.class_310;
import net.minecraft.class_465;

/** Compiled only into Sell. No AutoUse or AutoHome classes are linked. */
public final class SideIntegration {
    public static final String ID = "sell", NAME = "Sell", AUTOMATION_NAME = "AutoSell";
    private static class_1703 saleHandler;
    private static long awaitUntil;
    private static boolean suspended = true;
    private static boolean releaseRequested;
    private SideIntegration() {}
    public static boolean consumeToggle() { return DualDonutControl.consume(Autosell.toggleKey); }
    public static boolean consumeSettings() { return DualDonutControl.consume(Autosell.openGuiKey); }
    /** Called once by the client tick driver, even when the base engine is disabled. */
    public static void tickRelease(class_310 client) {
        if (releaseRequested) { AutoSellLogic.tickRelease(); cancel(client); }
    }
    public static void reconcile(class_310 client, boolean selected, boolean ready) {
        if (!selected) { ConfigManager.state = 0; cancel(client); }
        else {
            if (ConfigManager.state == 0 && !releaseRequested) { ConfigManager.state = 1; AutoSellLogic.reset(); }
            if (!ready) cancel(client);
        }
    }
    private static boolean owns(class_310 client) { return saleHandler != null && client.field_1755 instanceof class_465 gui && gui.method_17577() == saleHandler; }
    public static boolean screenAllowed(class_310 client) { return !releaseRequested && !BountyRuntime.blocksSell() && (client.field_1755 == null || owns(client)); }
    private static boolean saleBusy() { return saleHandler != null || System.currentTimeMillis() < awaitUntil; }
    public static boolean busy() { return saleBusy() || BountyRuntime.busy(); }
    public static boolean acquireForBounty(class_310 client) {
        yieldForHome(client);
        return !saleBusy() && !releaseRequested && client.field_1724 != null && client.field_1755 == null
            && client.field_1724.field_7512 == client.field_1724.field_7498
            && client.field_1724.field_7512.method_34255().method_7960();
    }
    public static void yieldForHome(class_310 client) {
        // Never interrupt a pending /sell response, item batch, or confirmation.
        if (owns(client) && client.field_1724 != null && client.field_1724.field_7512 == saleHandler
            && awaitUntil == 0 && AutoSellLogic.canYieldForHome()
            && saleHandler.method_34255().method_7960()) cancel(client);
    }
    public static void cancel(class_310 client) {
        if (!suspended || saleBusy()) {
            if (owns(client) && client.field_1724 != null) {
                if (client.field_1724.field_7512 != saleHandler || !AutoSellLogic.canYieldForHome()
                    || !saleHandler.method_34255().method_7960()) {
                    releaseRequested = true;
                    suspended = true;
                    return;
                }
                client.field_1724.method_7346();
            }
            AutoSellLogic.reset();
        }
        saleHandler = null; awaitUntil = 0; suspended = true; releaseRequested = false;
    }
    public static void refresh(class_310 client) {
        if (saleHandler != null && !owns(client)) saleHandler = null;
        if (awaitUntil > System.currentTimeMillis() && client.field_1755 instanceof class_465 gui
            && SaleScreenRules.matches(gui.method_25440().getString(), DualDonutControl.config().saleScreenTitles)) {
            saleHandler = gui.method_17577(); awaitUntil = 0;
        } else if (System.currentTimeMillis() >= awaitUntil) awaitUntil = 0;
    }
    public static void beforeSellTick(class_310 client) {
        if (DualDonutControl.canRunSell(client)) suspended = false;
        else cancel(client);
    }
    public static void saleCommandSent() { awaitUntil = System.currentTimeMillis() + SellCommandPacing.RESPONSE_TIMEOUT_MS; }
    public static void save() { ConfigManager.save(); BountyRuntime.save(); }
    public static String webhookUrl() { return ConfigManager.webhook_url; }
    public static void setWebhookUrl(String value) { ConfigManager.webhook_url = value; }
    public static List<Option<?>> webhookOptions() {
        return List.of(integer("Chu kỳ báo cáo lợi nhuận (phút)",
            "Khoảng thời gian định kỳ gửi thống kê số lượng vật phẩm đã bán và doanh thu lên Discord Webhook (Mặc định: 60 phút).",
            60, 5, 1000, () -> ConfigManager.webhook_interval, value -> ConfigManager.webhook_interval = value));
    }
    public static List<ConfigCategory> categories() {
        var shared = DualDonutControl.config();
        var code = string("Mã điều khiển (6 chữ số)",
            "Mã bảo mật gồm 6 chữ số dùng để xác thực quyền điều khiển AutoSell qua tin nhắn riêng (whisper).",
            "000000", () -> ConfigManager.msg_code, value -> ConfigManager.msg_code = value);
        return List.of(ConfigCategory.createBuilder().name(text("AutoSell / Bán"))
            .group(OptionGroup.createBuilder().name(text("Cấu hình nhịp độ bán hàng"))
                .option(decimal("Delay step (giây)", "Khoảng thời gian chờ giữa các thao tác trong menu bán đồ (Mặc định: 1.0 giây).", 1, 0.05, 300, () -> ConfigManager.delay_step, value -> ConfigManager.delay_step = value))
                .option(integer("Delay move (tick)", "Thời gian chờ khi di chuyển từng slot đồ (20 tick = 1 giây, mặc định: 2 tick).", 2, 0, 1200, () -> ConfigManager.delay_move, value -> ConfigManager.delay_move = value))
                .option(decimal("Delay sleep (giây)", "Thời gian nghỉ giữa các phiên bán đồ hoàn tất trước khi bán tiếp (Mặc định: 5.0 giây).", 5, 0, 3600, () -> ConfigManager.delay_sleep, value -> ConfigManager.delay_sleep = value))
                .option(integer("Packet size (slot)", "Số lượng slot vật phẩm xử lý trong mỗi đợt gửi gói tin (Mặc định: 4 slot).", 4, 1, 36, () -> ConfigManager.packet_size, value -> ConfigManager.packet_size = value))
                .option(string("Tên GUI bán (từ khóa)", "Từ khóa để nhận diện tiêu đề giao diện bán hàng sau khi gõ /sell. Phân cách bằng dấu |. Không phân biệt hoa/thường.", "sell|bán", () -> shared.saleScreenTitles, value -> shared.saleScreenTitles = value))
                .build())
            .group(OptionGroup.createBuilder().name(text("Điều khiển qua tin nhắn (Msg Control)"))
                .option(bool("Bật Msg Control", "Cho phép nhận tin nhắn riêng để bật/tắt AutoSell từ xa.\n§eCú pháp:§r <mã> atsell on/off\n§7Lưu ý: Không thể vượt qua công tắc tổng Master (F8).", false, () -> ConfigManager.msgctrl, value -> ConfigManager.msgctrl = value))
                .option(code)
                .option(button("Tạo mã ngẫu nhiên", "Tạo mã", "Bấm để tự động tạo một mã bảo mật 6 số ngẫu nhiên mới.\n§7Sau khi tạo, hãy bấm [Lưu & Thoát] để lưu lại.", () -> code.requestSet(String.format(Locale.ROOT, "%06d", new Random().nextInt(1000000)))))
                .build()).build(),
            ConfigCategory.createBuilder().name(text("Whitelist item"))
                .group(OptionGroup.createBuilder().name(text("Chế độ Whitelist"))
                    .option(bool("Bật Whitelist item", "Khi BẬT: Bot CHỈ BÁN những vật phẩm có ID nằm trong danh sách Whitelist bên dưới.\n§7Các vật phẩm khác trong túi đồ sẽ được giữ nguyên.", false, () -> ConfigManager.enable_whitelist, value -> ConfigManager.enable_whitelist = value)).build())
                .group(strings("ID item được bán", "Nhập ID vật phẩm Minecraft được phép bán (Ví dụ: dirt, diamond, minecraft:gold_ingot).\n§7Bấm [+] để thêm dòng mới, [x] để xóa.", () -> ConfigManager.whitelist, value -> ConfigManager.whitelist = value)).build(),
            ConfigCategory.createBuilder().name(text("Blacklist item"))
                .group(OptionGroup.createBuilder().name(text("Chế độ Blacklist"))
                    .option(bool("Bật Blacklist item", "Khi BẬT: Bot TUYỆT ĐỐI KHÔNG BÁN những vật phẩm có ID nằm trong danh sách Blacklist này.\n§7Dùng để bảo vệ đồ giá trị, vũ khí, giáp, shulker box.", false, () -> ConfigManager.enable_blacklist, value -> ConfigManager.enable_blacklist = value)).build())
                .group(strings("ID item cấm bán", "Nhập ID vật phẩm không muốn bán (Ví dụ: diamond_sword, netherite_pickaxe, shulker_box).\n§7Bấm [+] để thêm dòng mới, [x] để xóa.", () -> ConfigManager.blacklist, value -> ConfigManager.blacklist = value)).build(),
            BountyRuntime.category());
    }
}
