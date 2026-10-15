package dev.dualdonut;

import static dev.dualdonut.GuiOptions.*;
import dev.dualdonut.runtime.SaleScreenRules;
import dev.isxander.yacl3.api.*;
import hoshin.autosell.AutoSellLogic;
import hoshin.autosell.Autosell;
import hoshin.autosell.ConfigManager;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.class_1703;
import net.minecraft.class_310;
import net.minecraft.class_465;

/** Compiled only into Sell. No AutoUse or AutoHome classes are linked. */
public final class SideIntegration {
    public static final String ID = "sell", NAME = "Sell", AUTOMATION_NAME = "AutoSell Shion";
    private static class_1703 saleHandler;
    private static long awaitUntil;
    private static boolean suspended = true;
    private SideIntegration() {}
    public static boolean consumeToggle() { return DualDonutControl.consume(Autosell.toggleKey); }
    public static boolean consumeSettings() { return DualDonutControl.consume(Autosell.openGuiKey); }
    public static void reconcile(class_310 client, boolean selected, boolean ready) {
        if (!selected) { ConfigManager.state = 0; cancel(client); }
        else {
            if (ConfigManager.state == 0) { ConfigManager.state = 1; AutoSellLogic.reset(); }
            if (!ready) cancel(client);
        }
    }
    private static boolean owns(class_310 client) { return saleHandler != null && client.field_1755 instanceof class_465 gui && gui.method_17577() == saleHandler; }
    public static boolean screenAllowed(class_310 client) { return client.field_1755 == null || owns(client); }
    public static boolean busy() { return saleHandler != null || System.currentTimeMillis() < awaitUntil; }
    public static void cancel(class_310 client) {
        if (!suspended || busy()) {
            if (owns(client) && client.field_1724 != null) client.field_1724.method_7346();
            AutoSellLogic.reset();
        }
        saleHandler = null; awaitUntil = 0; suspended = true;
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
    public static void saleCommandSent() { awaitUntil = System.currentTimeMillis() + 3000; }
    public static void save() { ConfigManager.save(); }
    public static String webhookUrl() { return ConfigManager.webhook_url; }
    public static void setWebhookUrl(String value) { ConfigManager.webhook_url = value; }
    public static List<Option<?>> webhookOptions() {
        return List.of(integer("Chu kỳ báo cáo lợi nhuận (phút)", 60, 5, 1000, () -> ConfigManager.webhook_interval, value -> ConfigManager.webhook_interval = value));
    }
    public static List<ConfigCategory> categories() {
        var shared = DualDonutControl.config();
        var code = string("Mã điều khiển (6 chữ số)", "Giữ riêng cho profile Sell.", "000000", () -> ConfigManager.msg_code, value -> ConfigManager.msg_code = value);
        return List.of(ConfigCategory.createBuilder().name(text("AutoSell / Bán"))
            .group(OptionGroup.createBuilder().name(text("Phía sell – cài AutoSell + Control Sell"))
                .option(decimal("Delay step (giây)", 1, 0.05, 300, () -> ConfigManager.delay_step, value -> ConfigManager.delay_step = value))
                .option(integer("Delay move (tick)", 2, 0, 1200, () -> ConfigManager.delay_move, value -> ConfigManager.delay_move = value))
                .option(decimal("Delay sleep (giây)", 5, 0, 3600, () -> ConfigManager.delay_sleep, value -> ConfigManager.delay_sleep = value))
                .option(integer("Packet size (slot)", 4, 1, 36, () -> ConfigManager.packet_size, value -> ConfigManager.packet_size = value))
                .option(string("Tên GUI bán (từ khóa)", "Nhận diện GUI phản hồi sau /sell, ngăn cách bằng |.", "sell|bán", () -> shared.saleScreenTitles, value -> shared.saleScreenTitles = value))
                .build())
            .group(OptionGroup.createBuilder().name(text("Điều khiển tin nhắn"))
                .option(bool("Bật Msg Control", "Cú pháp <mã> atsell on/off. Không vượt qua master F8.", false, () -> ConfigManager.msgctrl, value -> ConfigManager.msgctrl = value))
                .option(code)
                .option(button("Tạo mã ngẫu nhiên", "Bấm Save để lưu.", () -> code.requestSet(String.format(Locale.ROOT, "%06d", new Random().nextInt(1000000)))))
                .build()).build(),
            ConfigCategory.createBuilder().name(text("Whitelist item"))
                .group(OptionGroup.createBuilder().name(text("Chỉ bán item được phép"))
                    .option(bool("Bật whitelist item", "Chỉ bán ID item có trong danh sách.", false, () -> ConfigManager.enable_whitelist, value -> ConfigManager.enable_whitelist = value)).build())
                .group(strings("ID item được bán", "Ví dụ dirt, minecraft:stone.", () -> ConfigManager.whitelist, value -> ConfigManager.whitelist = value)).build(),
            ConfigCategory.createBuilder().name(text("Blacklist item"))
                .group(OptionGroup.createBuilder().name(text("Không bán item bị cấm"))
                    .option(bool("Bật blacklist item", "Không bán ID item có trong danh sách.", false, () -> ConfigManager.enable_blacklist, value -> ConfigManager.enable_blacklist = value)).build())
                .group(strings("ID item cấm bán", "Ví dụ diamond_sword.", () -> ConfigManager.blacklist, value -> ConfigManager.blacklist = value)).build());
    }
}
