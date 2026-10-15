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
    public static final String ID = "use", NAME = "Use", AUTOMATION_NAME = "AutoUse farm";
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
    public static void beforeSellTick(class_310 client) {}
    public static void saleCommandSent() {}
    public static void save() { AutoUseMod.config().save(); }
    public static String webhookUrl() { return DualDonutControl.config().webhookUrl; }
    public static void setWebhookUrl(String value) { DualDonutControl.config().webhookUrl = value; }
    public static List<Option<?>> webhookOptions() { return List.of(); }
    public static List<ConfigCategory> categories() {
        var cfg = AutoUseMod.config();
        return List.of(ConfigCategory.createBuilder().name(text("AutoUse / Farm"))
            .group(OptionGroup.createBuilder().name(text("Phía dùng farm – cài AutoUse + Control Use"))
                .option(bool("Hold RMB", "Ominous Bottle > Firework Rocket > Sword. Nhả RMB khi GUI, hồi phục hoặc bảo vệ người chơi.", true, () -> cfg.holdRmb, value -> cfg.holdRmb = value))
                .option(bool("AutoSword", "Thiếu kiếm kim cương Looting III trong 10 giây: tìm /ah, giới hạn 5 triệu.", true, () -> cfg.autoSword, value -> cfg.autoSword = value))
                .build()).build());
    }
}
