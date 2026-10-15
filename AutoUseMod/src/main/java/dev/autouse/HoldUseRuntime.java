package dev.autouse;

import dev.autouse.api.AutoUseApi;
import dev.autouse.mixin.MinecraftClientInvoker;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_7923;

public final class HoldUseRuntime {
    private static boolean pressedByUs;
    private static int lastSlot = -1;
    private static int lastPriority;
    private static int settleTicks;
    private static int repeatTicks;
    private static Object player;
    private static Object world;
    private HoldUseRuntime() {}

    public static boolean ownsUseKey() { return pressedByUs; }
    public static boolean isCurrentUseSafe(class_310 client) {
        return MasterAutomation.isEnabled() && AutoUseMod.config().holdRmb
            && client.field_1724 != null && client.field_1687 != null && client.field_1761 != null
            && client.field_1724.method_5805() && client.field_1755 == null
            && !DeathRecoveryRuntime.isRecoveryPending() && !SwordRecoveryRuntime.isRecoveryActive()
            && !AutoUseApi.isAutoSellBusy() && settleTicks == 0
            && client.field_1724.method_31548().method_67532() == lastSlot
            && lastPriority > 0 && priority(client.field_1724.method_6047()) == lastPriority;
    }
    public static int priority(class_1799 stack) {
        return stack == null || stack.method_7960() ? 0
            : UseItemRules.priority(class_7923.field_41178.method_10221(stack.method_7909()).toString());
    }

    static void tick(class_310 client) {
        if (client.field_1724 != player || client.field_1687 != world) {
            release(client); player = client.field_1724; world = client.field_1687;
        }
        if (!MasterAutomation.isEnabled() || !AutoUseMod.config().holdRmb
            || client.field_1724 == null || client.field_1687 == null || client.field_1761 == null
            || !client.field_1724.method_5805() || client.field_1755 != null
            || client.field_1724.field_7512 != client.field_1724.field_7498
            || DeathRecoveryRuntime.isRecoveryPending() || SwordRecoveryRuntime.isRecoveryActive()
            || AutoUseApi.isAutoSellBusy()) { release(client); return; }
        var inventory = client.field_1724.method_31548();
        int[] priorities = new int[9];
        for (int i = 0; i < 9; i++) priorities[i] = priority(inventory.method_5438(i));
        int selected = inventory.method_67532();
        int best = UseItemRules.bestHotbarSlot(priorities, selected);

        // Pull higher-priority allowed items from the bag. SWAP preserves the displaced item.
        int bagSlot = -1;
        int bestPriority = best < 0 ? 0 : priorities[best];
        for (int i = 9; i < 36; i++) {
            int value = priority(inventory.method_5438(i));
            if (value > bestPriority) { bestPriority = value; bagSlot = i; }
        }
        if (bagSlot >= 0) {
            release(client);
            var handler = client.field_1724.field_7512;
            if (handler != client.field_1724.field_7498 || !handler.method_34255().method_7960()) return;
            int target = switch (bestPriority) { case 3 -> 1; case 2 -> 2; default -> 0; };
            for (var slot : handler.field_7761) {
                if (slot.field_7871 == inventory && slot.method_34266() == bagSlot) {
                    client.field_1761.method_2906(handler.field_7763, slot.field_7874, target,
                        class_1713.field_7791, client.field_1724);
                    settleTicks = 3;
                    return;
                }
            }
            return;
        }
        if (best < 0) { release(client); return; }
        if (selected != best || best != lastSlot || priorities[best] != lastPriority) {
            release(client);
            inventory.method_61496(best);
            lastSlot = best;
            lastPriority = priorities[best];
            settleTicks = 3;
        }
        if (settleTicks > 0) { settleTicks--; return; }
        // Read the actual main hand again; never retain a stale decision after inventory updates.
        if (priority(client.field_1724.method_6047()) != lastPriority
            || inventory.method_67532() != best) { release(client); return; }
        client.field_1690.field_1904.method_23481(true);
        pressedByUs = true;
        if (repeatTicks > 0) repeatTicks--;
        if (!client.field_1724.method_6115() && repeatTicks == 0) {
            // Preserve vanilla targeted use (including firing rockets against a block).
            // The mixin restricts automatic use to the validated MAIN_HAND.
            ((MinecraftClientInvoker) client).autousemod$useItem();
            repeatTicks = 4;
        }
    }

    public static void release(class_310 client) {
        if (pressedByUs && client != null) client.field_1690.field_1904.method_23481(false);
        pressedByUs = false;
        lastSlot = -1;
        lastPriority = settleTicks = repeatTicks = 0;
    }
}
