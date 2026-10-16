package hoshin.autosell;

import java.lang.reflect.Field;
import dev.dualdonut.SideIntegration;
import net.minecraft.*;

/** Runs the production state machine with a small in-memory menu/server. No game/auth/network. */
public final class SellSessionChecks {
    private static int checks;
    public static void main(String[] args) throws Exception {
        pacing();
        repeatedSales();
        idleMenu();
        delayedMenu();
        handoff();
        coordinatorRelease();
        bountyHandoff();
        System.out.println("Sell session regression checks passed: " + checks);
    }

    private static void pacing() {
        var gate = new SellCommandPacing();
        check(gate.trySend(100), "initial request");
        check(!gate.trySend(101) && gate.waiting(5099), "one outstanding request");
        check(!gate.waiting(5100) && !gate.trySend(10099), "timeout then backoff");
        check(gate.trySend(10100), "first retry after five-second backoff");
        check(!gate.trySend(25099) && gate.trySend(25100), "second retry after ten-second backoff");
        check(gate.exhausted(30100) && !gate.trySend(1000000), "three attempts is a hard limit");
        gate.menuReceived(1000001);
        check(!gate.trySend(1005000) && gate.trySend(1005001), "received menu starts close/reopen cooldown");
        gate.reset();
        check(gate.trySend(0), "explicit restart permits another attempt");
    }

    private static class_310 client(boolean menu) {
        AutoSellLogic.reset();
        ConfigManager.state = 1;
        ConfigManager.enable_whitelist = ConfigManager.enable_blacklist = false;
        var client = new class_310();
        if (menu) openMenu(client);
        return client;
    }

    private static void openMenu(class_310 client) {
        var menu = new class_1703();
        for (int i = 0; i < 36; i++) menu.field_7761.add(new class_1735(client.field_1724.inventory, i, class_1799.EMPTY));
        menu.field_7761.add(new class_1735(new Object(), 53, new class_1799(class_1802.field_8581)));
        client.field_1724.field_7512 = menu;
        client.field_1755 = new class_465(menu);
    }

    private static void refill(class_310 client) {
        for (var slot : client.field_1724.field_7512.field_7761)
            if (slot.field_7871 == client.field_1724.inventory) slot.stack = new class_1799(new class_1792("dirt"));
    }

    private static void repeatedSales() throws Exception {
        var client = client(true);
        var gui = client.field_1755;
        refill(client);
        int lastConfirms = 0;
        boolean rested = false;
        for (int i = 0; i < 20000 && lastConfirms < 25; i++) {
            AutoSellLogic.onTick(client);
            if (phase().equals("SLEEPING") && get("completedSaleHandler") == null && (int)get("waitTicks") > 0)
                rested |= AutoSellLogic.canYieldForHome();
            if (client.field_1761.confirms > lastConfirms) { lastConfirms = client.field_1761.confirms; refill(client); }
        }
        check(lastConfirms == 25 && client.field_1761.moves == 25 * 36, "25 full batches transfer and confirm on one menu");
        check(client.field_1755 == gui && client.field_1724.closes == 0, "sales and long rests preserve GUI identity");
        check(client.field_1724.field_3944.commands == 0, "owned menu never sends /sell again");
        check(rested, "resting menu can yield for home");
    }

    private static void idleMenu() {
        var client = client(true);
        var gui = client.field_1755;
        for (int i = 0; i < 1000; i++) AutoSellLogic.onTick(client);
        check(client.field_1761.confirms == 0 && client.field_1761.moves == 0, "idle scans never confirm an empty sale");
        check(client.field_1755 == gui && client.field_1724.closes == 0 && client.field_1724.field_3944.commands == 0,
            "idle menu neither flickers nor sends commands");
    }

    private static void delayedMenu() throws Exception {
        var client = client(false);
        for (int i = 0; i < 1000; i++) AutoSellLogic.onTick(client);
        check(client.field_1724.field_3944.commands == 1 && phase().equals("WAITING_GUI"), "fast ticks cannot duplicate pending /sell");
        openMenu(client); refill(client);
        for (int i = 0; i < 300 && client.field_1761.confirms == 0; i++) AutoSellLogic.onTick(client);
        check(client.field_1761.confirms == 1 && client.field_1724.field_3944.commands == 1, "late response resumes a sale without another command");
        client.field_1755 = null;
        for (int i = 0; i < 1000; i++) AutoSellLogic.onTick(client);
        check(client.field_1724.field_3944.commands == 1, "server-closing the menu still observes reopen cooldown");
    }

    private static void handoff() throws Exception {
        var client = client(true); refill(client);
        for (int i = 0; i < 100 && !phase().equals("WAITING_MOVE"); i++) AutoSellLogic.onTick(client);
        check(phase().equals("WAITING_MOVE") && !AutoSellLogic.canYieldForHome(), "home waits during an item batch");
        int clicks = client.field_1761.moves;
        AutoSellLogic.tickRelease();
        check(!AutoSellLogic.canYieldForHome(), "stop preserves move synchronization delay");
        for (int i = 0; i < 10 && !AutoSellLogic.canYieldForHome(); i++) AutoSellLogic.tickRelease();
        check(AutoSellLogic.canYieldForHome() && client.field_1761.moves == clicks && client.field_1761.confirms == 0,
            "stop drains outstanding moves without new clicks");

        client = client(true); refill(client);
        for (int i = 0; i < 300 && client.field_1761.confirms == 0; i++) AutoSellLogic.onTick(client);
        check(!AutoSellLogic.canYieldForHome(), "home waits for confirmation response delay");
        var menu = client.field_1724.field_7512;
        menu.cursor = new class_1799(new class_1792("diamond"));
        for (int i = 0; i < 50; i++) AutoSellLogic.onTick(client);
        check(!AutoSellLogic.canYieldForHome() && client.field_1724.closes == 0, "cursor-held item prevents confirmation handoff");
        menu.cursor = class_1799.EMPTY;
        for (int i = 0; i < 50 && !AutoSellLogic.canYieldForHome(); i++) AutoSellLogic.onTick(client);
        check(AutoSellLogic.canYieldForHome(), "home can release the settled menu before another batch");
    }

    private static void claim(class_310 client) {
        SideIntegration.saleCommandSent();
        SideIntegration.refresh(client);
        SideIntegration.beforeSellTick(client);
    }

    private static void coordinatorRelease() throws Exception {
        var client = client(true); claim(client); refill(client);
        for (int i = 0; i < 100 && !phase().equals("WAITING_MOVE"); i++) AutoSellLogic.onTick(client);
        SideIntegration.yieldForHome(client);
        check(client.field_1724.closes == 0 && SideIntegration.busy(), "due home retains busy item batch");
        for (int i = 0; i < 300 && !AutoSellLogic.canYieldForHome(); i++) AutoSellLogic.onTick(client);
        SideIntegration.yieldForHome(client);
        check(client.field_1724.closes == 1 && !SideIntegration.busy(), "settled sale closes once and releases home interlock");

        client = client(true); claim(client); refill(client);
        ConfigManager.delay_move = 5;
        for (int i = 0; i < 100 && !phase().equals("WAITING_MOVE"); i++) AutoSellLogic.onTick(client);
        int moves = client.field_1761.moves;
        SideIntegration.reconcile(client, false, false);
        check(client.field_1724.closes == 0 && !SideIntegration.screenAllowed(client), "master off defers unsafe close and prevents further engine work");
        SideIntegration.tickRelease(client); SideIntegration.reconcile(client, true, true);
        check(client.field_1724.closes == 0 && !AutoSellLogic.canYieldForHome(), "quick re-enable cannot skip pending move synchronization");
        for (int i = 0; i < 10; i++) { SideIntegration.tickRelease(client); SideIntegration.reconcile(client, true, true); }
        check(client.field_1724.closes == 1 && client.field_1761.moves == moves && client.field_1761.confirms == 0,
            "deferred stop closes once without moving or confirming more items");
        ConfigManager.delay_move = 2;

        client = client(true); claim(client);
        client.field_1724.field_7512.cursor = new class_1799(new class_1792("diamond"));
        SideIntegration.cancel(client);
        for (int i = 0; i < 50; i++) { SideIntegration.tickRelease(client); SideIntegration.reconcile(client, false, false); }
        check(client.field_1724.closes == 0 && SideIntegration.busy(), "cursor-held item blocks cancellation indefinitely");
        client.field_1724.field_7512.cursor = class_1799.EMPTY;
        SideIntegration.tickRelease(client);
        SideIntegration.reconcile(client, false, false);
        check(client.field_1724.closes == 1 && !SideIntegration.busy(), "empty cursor permits deferred close");

        client = client(true); claim(client);
        var foreign = new class_1703();
        client.field_1755 = new class_465(foreign);
        client.field_1724.field_7512 = foreign;
        SideIntegration.cancel(client);
        check(client.field_1724.closes == 0 && client.field_1755 != null, "cancellation never closes a foreign GUI with the same title");
    }

    private static void bountyHandoff() throws Exception {
        var client = client(true); claim(client); refill(client);
        for (int i = 0; i < 100 && !phase().equals("WAITING_MOVE"); i++) AutoSellLogic.onTick(client);
        check(!SideIntegration.acquireForBounty(client) && client.field_1724.closes == 0, "bounty cannot preempt an active item batch");
        for (int i = 0; i < 300 && !AutoSellLogic.canYieldForHome(); i++) AutoSellLogic.onTick(client);
        check(SideIntegration.acquireForBounty(client) && client.field_1724.closes == 1, "bounty acquires only after the same safe sale handoff as home");
        client.field_1755 = new class_465(new class_1703());
        check(!SideIntegration.acquireForBounty(client) && client.field_1724.closes == 1, "bounty acquisition does not close a foreign menu");
    }
    private static Object get(String name) throws Exception { Field f = AutoSellLogic.class.getDeclaredField(name); f.setAccessible(true); return f.get(null); }
    private static String phase() throws Exception { return get("currentState").toString(); }
    private static void check(boolean condition, String message) { checks++; if (!condition) throw new AssertionError(message); }
}
