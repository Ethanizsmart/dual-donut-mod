package net.minecraft;
public final class class_636 {
    private final class_310 client;
    public int moves, confirms, cargo;
    public class_636(class_310 client) { this.client = client; }
    public void method_2906(int syncId, int slotId, int button, class_1713 action, class_746 player) {
        var menu = ((class_465) client.field_1755).method_17577();
        if (menu != player.field_7512 || syncId != menu.field_7763 || !menu.cursor.method_7960())
            throw new AssertionError("unsafe click");
        var slot = menu.field_7761.stream().filter(s -> s.field_7874 == slotId).findFirst().orElseThrow();
        if (action == class_1713.field_7794) {
            if (slot.field_7871 != player.inventory || !slot.method_7681()) throw new AssertionError("invalid move");
            moves++; cargo++; slot.stack = class_1799.EMPTY;
        } else {
            if (slot.field_7871 == player.inventory || cargo == 0) throw new AssertionError("empty or foreign confirm");
            confirms++; cargo = 0;
        }
    }
}
