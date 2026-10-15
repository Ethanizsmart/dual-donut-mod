package dev.autouse;

/** Fixed priority: Ominous Bottle > Firework Rocket > any vanilla sword. */
final class UseItemRules {
    static int priority(String id) {
        if (id == null) return 0;
        return switch (id) {
            case "minecraft:ominous_bottle" -> 3;
            case "minecraft:firework_rocket" -> 2;
            case "minecraft:wooden_sword", "minecraft:stone_sword", "minecraft:copper_sword",
                 "minecraft:iron_sword", "minecraft:golden_sword", "minecraft:diamond_sword",
                 "minecraft:netherite_sword" -> 1;
            default -> 0;
        };
    }
    static int bestHotbarSlot(int[] priorities, int selected) {
        int best = -1;
        for (int slot = 0; slot < Math.min(9, priorities.length); slot++) {
            if (priorities[slot] > 0 && (best < 0 || priorities[slot] > priorities[best])) best = slot;
        }
        if (best >= 0 && selected >= 0 && selected < Math.min(9, priorities.length)
            && priorities[selected] == priorities[best]) return selected;
        return best;
    }
}
