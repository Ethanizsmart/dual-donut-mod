package dev.autouse;

public final class AutomationRulesChecks {
    private static int checks;
    public static void main(String[] args) {
        VexDeathGate gate = new VexDeathGate();
        check(!gate.update(false, true, 2, 0), "Observe living Vex before death");
        check(!gate.update(true, true, 2, 100), "Keep death screen while Vex present");
        check(!gate.update(true, true, 0, 200), "First zero scan cannot release");
        for (long now : new long[]{400, 600, 800, 1000}) {
            check(!gate.update(true, true, 0, now), "Zero must remain stable for a second");
        }
        check(gate.update(true, true, 0, 1200), "Release after stable successful zero scans");
        check(!gate.update(true, true, 1, 1300), "Returning Vex revokes permission before respawn");
        check(!gate.update(true, false, 0, 3000), "Unknown scan cannot release");
        check(!gate.update(true, true, 0, 4000), "Unknown scan restarts stability timer");
        check(!gate.update(true, true, 0, 6000), "Time alone without five scans is insufficient");
        check(!gate.update(true, true, 0, 6100), "Third clear scan");
        check(!gate.update(true, true, 0, 6200), "Fourth clear scan");
        check(gate.update(true, true, 0, 6300), "Fifth successful clear scan releases");
        gate.reset();
        check(gate.update(true, true, 0, 0), "Ordinary non-Vex death does not wait unnecessarily");
        gate.reset();
        gate.update(false, true, 1, 1000);
        check(!gate.update(true, true, 0, 2000), "Recent Vex arms gate even if removed at death");
        gate.reset();
        check(!gate.update(true, false, 0, 0), "Unavailable world is not zero Vex");
        check(!gate.update(true, true, 0, 1000), "Recovery waits for successful scans after unavailable world");
        gate.reset();
        gate.update(false, true, 1, 0);
        check(gate.update(true, true, 0, 4000), "Old sightings expire after three seconds");
        gate.reset();
        gate.update(true, true, 1, 500);
        gate.update(true, true, 0, 600);
        for (long now : new long[]{700, 800, 900, 1000}) gate.update(true, true, 0, now);
        check(!gate.update(true, true, 0, 550), "Clock rollback starts a fresh stable interval");

        check(UseItemRules.priority("minecraft:ominous_bottle") == 3, "Bottle highest priority");
        check(UseItemRules.priority("minecraft:firework_rocket") == 2, "Rocket second");
        for (String sword : new String[]{"wooden", "stone", "copper", "iron", "golden", "diamond", "netherite"}) {
            check(UseItemRules.priority("minecraft:" + sword + "_sword") == 1, "Allow " + sword + " sword");
        }
        for (String item : new String[]{"white_banner", "potion", "glass_bottle", "diamond_axe", "firework_star", "air"}) {
            check(UseItemRules.priority("minecraft:" + item) == 0, "Reject " + item);
        }
        check(UseItemRules.priority(null) == 0, "Null is disallowed");
        check(UseItemRules.bestHotbarSlot(new int[]{1, 2, 3, 0}, 0) == 2, "Bottle wins hotbar scan");
        check(UseItemRules.bestHotbarSlot(new int[]{1, 2, 0, 0}, 0) == 1, "Rocket wins without bottle");
        check(UseItemRules.bestHotbarSlot(new int[]{1, 0, 0, 0}, 2) == 0, "Fall back to sword");
        check(UseItemRules.bestHotbarSlot(new int[]{0, 0, 0, 0}, 2) == -1, "No allowed item means release RMB");
        check(UseItemRules.bestHotbarSlot(new int[]{1, 1, 1, 0}, 2) == 2, "Keep selected slot when priorities tie");
        System.out.println("Automation rules: " + checks + " checks passed.");
    }
    private static void check(boolean value, String description) {
        checks++;
        if (!value) throw new AssertionError(description);
    }
}
