package dev.autouse;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** Small vanilla settings screen; no MaLiLib dependency. */
public final class AutoUseScreen extends class_437 {
    private final class_437 parent;
    public AutoUseScreen(class_437 parent) { super(class_2561.method_43470("AutoUseMod")); this.parent = parent; }

    @Override protected void method_25426() {
        var cfg = AutoUseMod.config();
        int y = Math.max(45, field_22790 / 2 - 80);
        toggle("Automation (F8)", MasterAutomation::isEnabled, ignored -> MasterAutomation.toggle(), y);
        toggle("Hold RMB: Bottle > Rocket > Sword", () -> cfg.holdRmb, value -> cfg.holdRmb = value, y + 26);
        toggle("AutoSword: missing for 10 seconds", () -> cfg.autoSword, value -> cfg.autoSword = value, y + 52);
        toggle("AutoRecovery: respawn + /home 1", () -> cfg.autoRecovery, value -> cfg.autoRecovery = value, y + 78);
        toggle("Vex entity check before respawn", () -> cfg.vexCheck, value -> cfg.vexCheck = value, y + 104);
        method_37063(class_4185.method_46430(class_2561.method_43470("Done"), button -> method_25419())
            .method_46434(field_22789 / 2 - 150, y + 144, 300, 20).method_46431());
    }

    private void toggle(String title, BooleanSupplier get, Consumer<Boolean> set, int y) {
        method_37063(class_4185.method_46430(label(title, get.getAsBoolean()), button -> {
            set.accept(!get.getAsBoolean());
            AutoUseMod.config().save();
            button.method_25355(label(title, get.getAsBoolean()));
            HoldUseRuntime.release(field_22787);
        }).method_46434(field_22789 / 2 - 150, y, 300, 20).method_46431());
    }

    private class_2561 label(String title, boolean value) { return class_2561.method_43470(title + ": " + (value ? "ON" : "OFF")); }
    @Override public void method_25394(class_332 graphics, int mouseX, int mouseY, float delta) {
        super.method_25394(graphics, mouseX, mouseY, delta);
        graphics.method_27534(field_22793, field_22785, field_22789 / 2, 20, 0xFFFFFFFF);
    }
    @Override public boolean method_25421() { return false; }
    @Override public void method_25419() { AutoUseMod.config().save(); field_22787.method_1507(parent); }
}
