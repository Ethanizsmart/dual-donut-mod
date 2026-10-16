package dev.dualdonut;

import static dev.dualdonut.GuiOptions.*;
import dev.autouse.AutoUseMod;
import dev.autouse.MasterAutomation;
import dev.autouse.SwordRecoveryRuntime;
import dev.dualdonut.mixin.AutoUseKeysAccessor;
import dev.dualdonut.mixin.MasterAccessor;
import dev.isxander.yacl3.api.*;
import java.util.List;
import net.minecraft.class_310;

/** Compiled only into Use. No AutoSell or AutoHome classes are linked. */
public final class SideIntegration {
    public static final String ID = "use", NAME = "Use", AUTOMATION_NAME = "AutoUse";
    private SideIntegration() {}
    public static boolean consumeToggle() { return DualDonutControl.consume(AutoUseKeysAccessor.dualdonut$getToggleKey()); }
    public static boolean consumeSettings() { return DualDonutControl.consume(AutoUseKeysAccessor.dualdonut$getSettingsKey()); }
    public static void reconcile(class_310 client, boolean selected, boolean ready) {
        boolean enabled = selected && ready;
        if (MasterAutomation.isEnabled() != enabled) {
            MasterAccessor.dualdonut$setEnabled(enabled);
            if (!enabled) AutoUseMod.resetRuntime();
        }
    }
    public static void cancel(class_310 client) { MasterAccessor.dualdonut$setEnabled(false); AutoUseMod.resetRuntime(); }
    public static void refresh(class_310 client) {}
    public static boolean screenAllowed(class_310 client) { return false; }
    public static boolean busy() { return SwordRecoveryRuntime.isRecoveryActive(); }
    public static void yieldForHome(class_310 client) {}
    public static void beforeSellTick(class_310 client) {}
    public static void saleCommandSent() {}
    public static void save() { AutoUseMod.config().save(); }
    public static String webhookUrl() { return DualDonutControl.config().webhookUrl; }
    public static void setWebhookUrl(String value) { DualDonutControl.config().webhookUrl = value; }
    public static List<Option<?>> webhookOptions() { return List.of(); }
    public static List<ConfigCategory> categories() {
        var cfg = AutoUseMod.config();
        return List.of(ConfigCategory.createBuilder().name(text("AutoUse / Farm"))
            .group(OptionGroup.createBuilder().name(text("Tính năng Farm tự động"))
                .option(bool("Hold RMB (Giữ chuột phải)", "§6Tự động giữ chuột phải liên tục để farm§r\n\n§eThứ tự ưu tiên vật phẩm cầm trên tay:§r\nOminous Bottle > Firework Rocket > Kiếm.\n\n§7Tự động nhả chuột khi mở menu/rương, khi đang hồi phục hoặc khi gặp người lạ.", true, () -> cfg.holdRmb, value -> cfg.holdRmb = value))
                .option(bool("AutoSword (Tự mua kiếm)", "§6Tự động tìm mua kiếm Looting III§r\n\nKhi trong túi đồ thiếu kiếm kim cương Looting III quá 10 giây, bot sẽ tự mở chợ /ah và tìm mua kiếm phù hợp.\n§7Giới hạn giá mua tối đa: 5,000,000$.", true, () -> cfg.autoSword, value -> cfg.autoSword = value))
                .build()).build());
    }
}
